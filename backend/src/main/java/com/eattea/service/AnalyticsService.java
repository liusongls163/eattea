package com.eattea.service;

import com.eattea.dto.*;
import com.eattea.entity.*;
import com.eattea.mapper.*;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class AnalyticsService {

    private final TaskMapper taskMapper;
    private final TaskDependencyMapper dependencyMapper;

    public AnalyticsService(TaskMapper taskMapper, TaskDependencyMapper dependencyMapper) {
        this.taskMapper = taskMapper;
        this.dependencyMapper = dependencyMapper;
    }

    /**
     * 燃尽图数据
     * 理想线：从开始到结束，每天完成等量任务数
     * 实际线：按完成日期累计的任务数
     */
    public BurnChartData burnChartData(Long projectId) {
        List<Task> tasks = taskMapper.selectByProject(projectId);
        if (tasks.isEmpty()) {
            BurnChartData empty = new BurnChartData();
            empty.setLabels(List.of());
            empty.setIdealLine(List.of());
            empty.setActualLine(List.of());
            return empty;
        }

        int total = tasks.size();

        // 找日期范围
        LocalDate minDate = tasks.stream()
                .map(Task::getStartDate).filter(Objects::nonNull)
                .min(Comparator.naturalOrder()).orElse(LocalDate.now().minusMonths(1));
        LocalDate maxDate = tasks.stream()
                .map(Task::getDueDate).filter(Objects::nonNull)
                .max(Comparator.naturalOrder()).orElse(LocalDate.now().plusMonths(1));
        LocalDate today = LocalDate.now();
        if (minDate.isAfter(today)) minDate = today;
        if (maxDate.isBefore(today)) maxDate = today.plusWeeks(1);

        long totalDays = ChronoUnit.DAYS.between(minDate, maxDate) + 1;
        if (totalDays < 2) totalDays = 2;

        // 标签列表
        List<String> labels = new ArrayList<>();
        List<Double> idealLine = new ArrayList<>();
        for (int i = 0; i < totalDays; i++) {
            LocalDate d = minDate.plusDays(i);
            labels.add(d.toString());
            idealLine.add(Math.round((total - (total * i / (double)(totalDays - 1))) * 10.0) / 10.0);
        }
        // 确保理想线最后一个点为0
        idealLine.set(idealLine.size() - 1, 0.0);

        // 实际线：按日期累计已完成任务数
        Map<LocalDate, Integer> doneByDate = new TreeMap<>();
        for (Task t : tasks) {
            if ("done".equals(t.getStatus()) && t.getUpdateTime() != null) {
                LocalDate doneDate = t.getUpdateTime().toLocalDate();
                doneByDate.merge(doneDate, 1, Integer::sum);
            }
        }

        List<Double> actualLine = new ArrayList<>();
        int cumDone = 0;
        for (int i = 0; i < totalDays; i++) {
            LocalDate d = minDate.plusDays(i);
            Integer done = doneByDate.get(d);
            if (done != null) cumDone += done;
            actualLine.add((double) Math.max(0, total - cumDone));
        }

        BurnChartData data = new BurnChartData();
        data.setLabels(labels);
        data.setIdealLine(idealLine);
        data.setActualLine(actualLine);
        return data;
    }

    /**
     * 延期预测：对进行中和待办的任务，基于当前进度速率预测是否能按时完成
     */
    public List<DelayPrediction> predictDelays(Long projectId) {
        List<Task> tasks = taskMapper.selectByProjectWithMember(projectId);
        LocalDate today = LocalDate.now();
        List<DelayPrediction> predictions = new ArrayList<>();

        for (Task t : tasks) {
            if ("done".equals(t.getStatus()) || "blocked".equals(t.getStatus())) continue;
            if (t.getDueDate() == null) continue;

            long daysRemaining = ChronoUnit.DAYS.between(today, t.getDueDate());
            if (daysRemaining <= 0) {
                // 已经逾期
                DelayPrediction dp = new DelayPrediction();
                dp.setTaskId(t.getId());
                dp.setTaskTitle(t.getTitle());
                dp.setAssigneeName(t.getAssigneeName());
                dp.setCurrentStatus(t.getStatus());
                dp.setCurrentProgress(t.getProgress() != null ? t.getProgress() : 0);
                dp.setDailyVelocity(0);
                dp.setDaysRemaining((int) daysRemaining);
                dp.setPredictedFinalProgress(t.getProgress() != null ? t.getProgress() : 0);
                dp.setRiskLevel("high");
                dp.setSuggestion("已逾期！距截止日" + (-daysRemaining) + "天，需立即协调资源加速或申请延期");
                predictions.add(dp);
                continue;
            }

            int progress = t.getProgress() != null ? t.getProgress() : 0;

            // 如果还未开始
            if (t.getStartDate() == null || t.getStartDate().isAfter(today)) {
                // 预估每天需要的进度
                double neededVelocity = (100.0 - progress) / daysRemaining;
                DelayPrediction dp = new DelayPrediction();
                dp.setTaskId(t.getId());
                dp.setTaskTitle(t.getTitle());
                dp.setAssigneeName(t.getAssigneeName());
                dp.setCurrentStatus(t.getStatus());
                dp.setCurrentProgress(progress);
                dp.setDailyVelocity(0);
                dp.setDaysRemaining((int) daysRemaining);
                dp.setPredictedFinalProgress(progress);

                if (neededVelocity > 5) { // 每天需要超过5%的进度，基本不可能
                    dp.setRiskLevel("high");
                    dp.setSuggestion("尚未启动且截止日临近，建议立即启动或重新排期");
                } else if (neededVelocity > 2.5) {
                    dp.setRiskLevel("medium");
                    dp.setSuggestion("启动偏晚，需保证每天推进" + String.format("%.1f", neededVelocity) + "%");
                } else {
                    dp.setRiskLevel("low");
                    dp.setSuggestion("时间充裕，按计划启动即可");
                }
                predictions.add(dp);
                continue;
            }

            // 已经在进行中
            long activeDays = ChronoUnit.DAYS.between(t.getStartDate(), today) + 1;
            if (activeDays < 1) activeDays = 1;
            double dailyVelocity = (double) progress / activeDays;
            double predictedFinal = progress + dailyVelocity * daysRemaining;
            predictedFinal = Math.min(predictedFinal, 100);

            DelayPrediction dp = new DelayPrediction();
            dp.setTaskId(t.getId());
            dp.setTaskTitle(t.getTitle());
            dp.setAssigneeName(t.getAssigneeName());
            dp.setCurrentStatus(t.getStatus());
            dp.setCurrentProgress(progress);
            dp.setDailyVelocity(Math.round(dailyVelocity * 10.0) / 10.0);
            dp.setDaysRemaining((int) daysRemaining);
            dp.setPredictedFinalProgress(Math.round(predictedFinal * 10.0) / 10.0);

            if (predictedFinal < 85) {
                dp.setRiskLevel("high");
                dp.setSuggestion("按当前速度无法在截止日前完成，建议增配人手或缩小范围");
            } else if (predictedFinal < 95) {
                dp.setRiskLevel("medium");
                dp.setSuggestion("需适当提速，确保关键路径不被阻塞");
            } else {
                dp.setRiskLevel("low");
                dp.setSuggestion("进度正常，保持即可");
            }
            predictions.add(dp);
        }

        // 按风险排序
        predictions.sort((a, b) -> {
            int orderA = "high".equals(a.getRiskLevel()) ? 0 : "medium".equals(a.getRiskLevel()) ? 1 : 2;
            int orderB = "high".equals(b.getRiskLevel()) ? 0 : "medium".equals(b.getRiskLevel()) ? 1 : 2;
            return orderA - orderB;
        });

        return predictions;
    }

    /**
     * 阻塞影响分析：找到被阻塞的任务，递归查找所有受影响的后续任务
     */
    public List<ImpactAnalysis> analyzeBlockedImpact(Long projectId) {
        List<Task> allTasks = taskMapper.selectByProjectWithMember(projectId);
        Map<Long, Task> taskMap = allTasks.stream().collect(Collectors.toMap(Task::getId, t -> t));

        List<Task> blockedTasks = allTasks.stream()
                .filter(t -> "blocked".equals(t.getStatus()))
                .toList();

        List<ImpactAnalysis> results = new ArrayList<>();
        for (Task blocked : blockedTasks) {
            ImpactAnalysis ia = new ImpactAnalysis();
            ia.setBlockedTaskId(blocked.getId());
            ia.setBlockedTaskTitle(blocked.getTitle());
            ia.setBlockedTaskStatus("blocked");
            ia.setReason("任务被阻塞：可能涉及外部依赖、资源不足或技术障碍");

            // 递归查找受影响任务
            Set<Long> visited = new HashSet<>();
            visited.add(blocked.getId());
            List<ImpactAnalysis.AffectedTask> affected = new ArrayList<>();
            findAffected(blocked.getId(), taskMap, 1, affected, visited);
            ia.setAffectedTasks(affected);
            ia.setTotalAffected(affected.size());
            ia.setTotalEstimatedHours(affected.stream()
                    .mapToInt(a -> {
                        Task t = taskMap.get(a.getTaskId());
                        return t != null && t.getEstimatedHours() != null ? t.getEstimatedHours().intValue() : 0;
                    }).sum());
            ia.setRiskSummary(String.format("阻塞影响 %d 个后续任务，涉及约 %d 小时工作量",
                    affected.size(), ia.getTotalEstimatedHours()));
            results.add(ia);
        }

        return results;
    }

    private void findAffected(Long taskId, Map<Long, Task> taskMap, int depth,
                              List<ImpactAnalysis.AffectedTask> result, Set<Long> visited) {
        List<TaskDependency> deps = dependencyMapper.selectByDependsOn(taskId);
        for (TaskDependency dep : deps) {
            Long affectedId = dep.getTaskId();
            if (visited.contains(affectedId)) continue;
            visited.add(affectedId);

            Task t = taskMap.get(affectedId);
            if (t == null) continue;

            ImpactAnalysis.AffectedTask at = new ImpactAnalysis.AffectedTask();
            at.setTaskId(t.getId());
            at.setTitle(t.getTitle());
            at.setStatus(t.getStatus());
            at.setAssigneeName(t.getAssigneeName());
            at.setDistance(depth);
            result.add(at);

            // 递归
            findAffected(affectedId, taskMap, depth + 1, result, visited);
        }
    }

    /**
     * 工时偏差预警：实际工时 vs 预估工时偏差率
     */
    public List<HourDeviation> getHourDeviations(Long projectId) {
        List<Task> tasks = taskMapper.selectByProjectWithMember(projectId);
        List<HourDeviation> result = new ArrayList<>();

        for (Task t : tasks) {
            if (t.getEstimatedHours() == null || t.getActualHours() == null) continue;
            BigDecimal est = t.getEstimatedHours();
            BigDecimal act = t.getActualHours();
            if (est.compareTo(BigDecimal.ZERO) == 0) continue;

            BigDecimal diff = act.subtract(est);
            double deviationPct = diff.divide(est, 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100)).doubleValue();

            String level;
            if (Math.abs(deviationPct) > 50) level = "high";
            else if (Math.abs(deviationPct) > 30) level = "medium";
            else level = "normal";

            HourDeviation hd = new HourDeviation();
            hd.setTaskId(t.getId());
            hd.setTaskTitle(t.getTitle());
            hd.setAssigneeName(t.getAssigneeName());
            hd.setEstimatedHours(est);
            hd.setActualHours(act);
            hd.setDeviationPct(Math.round(deviationPct * 10.0) / 10.0);
            hd.setLevel(level);
            result.add(hd);
        }
        // 按偏差绝对值降序
        result.sort((a, b) -> Double.compare(Math.abs(b.getDeviationPct()), Math.abs(a.getDeviationPct())));
        return result;
    }
}
