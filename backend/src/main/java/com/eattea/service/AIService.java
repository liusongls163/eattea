package com.eattea.service;

import com.eattea.dto.AIRequest;
import com.eattea.dto.AIResponse;
import com.eattea.dto.ProjectStats;
import com.eattea.entity.HealthCheck;
import com.eattea.entity.Project;
import com.eattea.entity.Report;
import com.eattea.entity.Task;
import com.eattea.mapper.HealthCheckMapper;
import com.eattea.mapper.ReportMapper;
import com.eattea.mapper.TaskMapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * AI 服务：对接 LLM API，实现项目健康诊断和报告生成
 *
 * 支持 OpenAI 兼容的 API 端点。
 * 配置中设置 LLM 的 API Key 和 endpoint。
 */
@Service
public class AIService {

    private static final Logger log = LoggerFactory.getLogger(AIService.class);
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final HealthCheckMapper healthCheckMapper;
    private final ReportMapper reportMapper;
    private final TaskMapper taskMapper;
    private final ProjectService projectService;

    @Value("${pm.ai.endpoint:https://api.openai.com/v1/chat/completions}")
    private String aiEndpoint;

    @Value("${pm.ai.api-key:}")
    private String apiKey;

    @Value("${pm.ai.model:gpt-4o-mini}")
    private String model;

    @Value("${pm.ai.enabled:true}")
    private boolean aiEnabled;

    public AIService(RestTemplate restTemplate, ObjectMapper objectMapper,
                     HealthCheckMapper healthCheckMapper, ReportMapper reportMapper,
                     TaskMapper taskMapper, ProjectService projectService) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
        this.healthCheckMapper = healthCheckMapper;
        this.reportMapper = reportMapper;
        this.taskMapper = taskMapper;
        this.projectService = projectService;
    }

    /**
     * AI 项目健康诊断
     */
    public AIResponse runHealthCheck(Long projectId) {
        Project project = projectService.getProject(projectId);
        if (project == null) return fail("项目不存在");

        ProjectStats stats = projectService.getProjectStats(projectId);
        List<Task> tasks = taskMapper.selectByProjectWithMember(projectId);

        // 构建上下文
        StringBuilder ctx = new StringBuilder();
        ctx.append("项目名称：").append(project.getName()).append("\n");
        ctx.append("项目描述：").append(project.getDescription()).append("\n");
        ctx.append("进度：").append(stats.getCompletionPct()).append("%\n");
        ctx.append("任务总数：").append(stats.getTotalTasks())
           .append("（已完成").append(stats.getDoneTasks())
           .append(", 进行中").append(stats.getInProgressTasks())
           .append(", 待办").append(stats.getTodoTasks())
           .append(", 阻塞").append(stats.getBlockedTasks())
           .append(", 逾期").append(stats.getOverdueTasks()).append("）\n");

        ctx.append("\n任务明细：\n");
        for (Task t : tasks) {
            ctx.append("- [").append(t.getStatus()).append("] ").append(t.getTitle());
            if (t.getAssigneeName() != null) ctx.append("（负责人：").append(t.getAssigneeName()).append("）");
            ctx.append("，截止：").append(t.getDueDate()).append("，进度：").append(t.getProgress()).append("%");
            if ("blocked".equals(t.getStatus())) ctx.append(" ⚠阻塞");
            ctx.append("\n");
        }

        String prompt = buildHealthCheckPrompt();
        String result = callLLM(prompt, ctx.toString());

        if (result == null) return fail("AI 调用失败");

        // 解析 AI 返回
        String health = extractHealth(result);
        String riskSummary = extractSection(result, "风险摘要");
        String suggestions = extractSection(result, "建议");

        // 保存诊断记录
        HealthCheck hc = new HealthCheck();
        hc.setProjectId(projectId);
        hc.setHealthStatus(health);
        hc.setOverdueTasks(stats.getOverdueTasks());
        hc.setBlockedTasks(stats.getBlockedTasks());
        hc.setRiskSummary(riskSummary);
        hc.setSuggestions(suggestions);

        try {
            hc.setMemberLoad(objectMapper.writeValueAsString(getMemberLoadSummary(tasks)));
        } catch (JsonProcessingException e) {
            hc.setMemberLoad("[]");
        }

        healthCheckMapper.insert(hc);

        // 更新项目健康度
        if (health != null) {
            projectService.updateHealth(projectId, health);
        }

        AIResponse resp = new AIResponse();
        resp.setSuccess(true);
        resp.setContent(result);
        resp.setHealthStatus(health);
        return resp;
    }

    /**
     * AI 生成报告（周报/月报/复盘）
     */
    public AIResponse generateReport(Long projectId, String type) {
        Project project = projectService.getProject(projectId);
        if (project == null) return fail("项目不存在");

        ProjectStats stats = projectService.getProjectStats(projectId);
        List<Task> tasks = taskMapper.selectByProjectWithMember(projectId);

        StringBuilder ctx = new StringBuilder();
        ctx.append("项目名称：").append(project.getName()).append("\n");
        ctx.append("项目描述：").append(project.getDescription()).append("\n");
        ctx.append("总体进度：").append(stats.getCompletionPct()).append("%\n\n");

        ctx.append("=== 已完成 ===\n");
        for (Task t : tasks) {
            if ("done".equals(t.getStatus()))
                ctx.append("- ").append(t.getTitle()).append("（").append(t.getAssigneeName()).append("）\n");
        }
        ctx.append("\n=== 进行中 ===\n");
        for (Task t : tasks) {
            if ("in_progress".equals(t.getStatus()))
                ctx.append("- ").append(t.getTitle()).append(" 进度").append(t.getProgress()).append("%（").append(t.getAssigneeName()).append("）\n");
        }
        ctx.append("\n=== 阻塞/延期 ===\n");
        for (Task t : tasks) {
            if ("blocked".equals(t.getStatus()))
                ctx.append("- ").append(t.getTitle()).append("（").append(t.getAssigneeName()).append("）\n");
        }
        ctx.append("\n=== 待办 ===\n");
        for (Task t : tasks) {
            if ("todo".equals(t.getStatus()))
                ctx.append("- ").append(t.getTitle()).append(" 截止").append(t.getDueDate()).append("（").append(t.getAssigneeName()).append("）\n");
        }

        String prompt = buildReportPrompt(type);
        String result = callLLM(prompt, ctx.toString());

        if (result == null) return fail("AI 调用失败");

        // 保存报告
        String title = switch (type) {
            case "weekly" -> project.getName() + " 周报";
            case "monthly" -> project.getName() + " 月报";
            case "review" -> project.getName() + " 项目复盘";
            default -> project.getName() + " 报告";
        };

        Report report = new Report();
        report.setProjectId(projectId);
        report.setType(type);
        report.setTitle(title);
        report.setContent(result);
        report.setPeriodStart(LocalDate.now().minusWeeks(1));
        report.setPeriodEnd(LocalDate.now());
        reportMapper.insert(report);

        AIResponse resp = new AIResponse();
        resp.setSuccess(true);
        resp.setContent(result);
        return resp;
    }

    // ---- 内部方法 ----

    private String buildHealthCheckPrompt() {
        return """
            你是一位资深项目经理。请根据以下项目数据，进行健康度诊断。

            请严格按以下格式输出（JSON）：

            {
              "health": "green|yellow|red",
              "风险摘要": "不超过200字，列出最关键的1-3个风险点",
              "建议": "不超过300字，给出具体可操作的改进建议"
            }

            判断标准：
            - green: 项目整体健康，基本按计划推进
            - yellow: 存在一定风险，需关注
            - red: 存在严重问题，需立即干预

            请基于数据做出判断，不要编造不存在的风险。
            """;
    }

    private String buildReportPrompt(String type) {
        String typeLabel = switch (type) {
            case "weekly" -> "周报";
            case "monthly" -> "月报";
            case "review" -> "项目复盘报告";
            default -> "报告";
        };

        return String.format("""
            你是一位资深项目经理。请根据以下项目数据，生成一份专业的%s。

            格式要求：
            1. 项目概述（2-3句）
            2. 本周/本月进展（已完成事项）
            3. 风险与问题（阻塞项、延期项）
            4. 下周/下月计划
            5. 需要协调的事项

            语言简洁专业，突出关键信息，避免空泛的套话。
            如果数据中有明确的风险信号（阻塞、逾期），必须重点提及。
            """, typeLabel);
    }

    private String callLLM(String systemPrompt, String userContent) {
        if (!aiEnabled) {
            log.info("AI disabled, using rule-based fallback");
            return "AI 功能未启用。项目统计已通过规则引擎计算。";
        }

        try {
            Map<String, Object> body = new HashMap<>();
            body.put("model", model);

            List<Map<String, String>> messages = List.of(
                Map.of("role", "system", "content", systemPrompt),
                Map.of("role", "user", "content", userContent)
            );
            body.put("messages", messages);
            body.put("temperature", 0.3);
            body.put("max_tokens", 2000);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            if (apiKey != null && !apiKey.isEmpty()) {
                headers.setBearerAuth(apiKey);
            }

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);
            ResponseEntity<Map> response = restTemplate.postForEntity(aiEndpoint, request, Map.class);

            if (response.getBody() != null) {
                List<Map<String, Object>> choices = (List<Map<String, Object>>) response.getBody().get("choices");
                if (choices != null && !choices.isEmpty()) {
                    Map<String, Object> message = (Map<String, Object>) choices.get(0).get("message");
                    return (String) message.get("content");
                }
            }
        } catch (Exception e) {
            log.error("LLM call failed", e);
        }
        return null;
    }

    private String extractHealth(String aiOutput) {
        if (aiOutput == null) return null;
        try {
            int start = aiOutput.indexOf("{");
            int end = aiOutput.lastIndexOf("}") + 1;
            if (start >= 0 && end > start) {
                Map<String, Object> map = objectMapper.readValue(aiOutput.substring(start, end), Map.class);
                Object h = map.get("health");
                if (h != null) {
                    String health = h.toString().toLowerCase();
                    if (health.equals("green") || health.equals("yellow") || health.equals("red")) {
                        return health;
                    }
                }
            }
        } catch (Exception e) {
            log.warn("Failed to parse AI health output", e);
        }
        // 从纯文本中提取
        if (aiOutput.contains("red") || aiOutput.contains("红色")) return "red";
        if (aiOutput.contains("yellow") || aiOutput.contains("黄色")) return "yellow";
        if (aiOutput.contains("green") || aiOutput.contains("绿色")) return "green";
        return null;
    }

    private String extractSection(String text, String section) {
        if (text == null) return "";
        // 先尝试从 JSON 中提取
        try {
            int start = text.indexOf("{");
            int end = text.lastIndexOf("}") + 1;
            if (start >= 0 && end > start) {
                Map<String, Object> map = objectMapper.readValue(text.substring(start, end), Map.class);
                Object val = map.get(section);
                if (val != null) return val.toString();
            }
        } catch (Exception e) { /* fallback */ }
        // 从文本中按标题提取
        String[] markers = {section + "：", section + ":", "**" + section + "**", "## " + section};
        for (String marker : markers) {
            int idx = text.indexOf(marker);
            if (idx >= 0) {
                String after = text.substring(idx + marker.length());
                // 截取到下一个标题或 JSON 结束
                int nextTitle = -1;
                for (String m : markers) {
                    int pos = after.indexOf(m);
                    if (pos > 0 && (nextTitle < 0 || pos < nextTitle)) nextTitle = pos;
                }
                return nextTitle > 0 ? after.substring(0, nextTitle).trim() : after.trim();
            }
        }
        return "";
    }

    private List<Map<String, Object>> getMemberLoadSummary(List<Task> tasks) {
        Map<Long, Map<String, Object>> memberMap = new HashMap<>();
        for (Task t : tasks) {
            if (t.getAssigneeId() == null) continue;
            memberMap.computeIfAbsent(t.getAssigneeId(), k -> {
                Map<String, Object> m = new HashMap<>();
                m.put("name", t.getAssigneeName());
                m.put("taskCount", 0);
                m.put("activeCount", 0);
                return m;
            });
            Map<String, Object> item = memberMap.get(t.getAssigneeId());
            item.put("taskCount", (int) item.get("taskCount") + 1);
            if ("in_progress".equals(t.getStatus()) || "todo".equals(t.getStatus())) {
                item.put("activeCount", (int) item.get("activeCount") + 1);
            }
        }
        return new ArrayList<>(memberMap.values());
    }

    private AIResponse fail(String msg) {
        AIResponse r = new AIResponse();
        r.setSuccess(false);
        r.setError(msg);
        return r;
    }

    /**
     * 获取各项目的历史报告
     */
    public List<Report> getReports(Long projectId, String type) {
        if (type != null && !type.isEmpty()) {
            return reportMapper.selectByType(projectId, type);
        }
        return reportMapper.selectByProject(projectId);
    }

    public Report getReport(Long id) {
        return reportMapper.selectById(id);
    }

    public void deleteReport(Long id) {
        reportMapper.deleteById(id);
    }

    /**
     * 获取最近一次健康诊断
     */
    public HealthCheck getLatestHealthCheck(Long projectId) {
        return healthCheckMapper.selectLatest(projectId);
    }
}
