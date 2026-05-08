package com.eattea.service;

import com.eattea.entity.Task;
import com.eattea.mapper.TaskMapper;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

@Service
public class TaskService {

    private final TaskMapper taskMapper;

    public TaskService(TaskMapper taskMapper) {
        this.taskMapper = taskMapper;
    }

    public List<Task> listByProject(Long projectId) {
        return taskMapper.selectByProjectWithMember(projectId);
    }

    public Task getTask(Long id) {
        return taskMapper.selectById(id);
    }

    public Task create(Task task) {
        if (task.getStatus() == null) task.setStatus("todo");
        if (task.getPriority() == null) task.setPriority("normal");
        if (task.getProgress() == null) task.setProgress(0);
        taskMapper.insert(task);
        return task;
    }

    public void update(Task task) {
        taskMapper.update(task);
    }

    public void delete(Long id) {
        taskMapper.deleteById(id);
    }

    /**
     * 从 Excel 导入任务
     * 列：项目ID、标题、描述、负责人ID、状态、优先级、预估工时、开始日期、截止日期、标签
     */
    public int importFromExcel(MultipartFile file, Long projectId) throws Exception {
        List<Task> tasks = new ArrayList<>();
        DateTimeFormatter dateFmt = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        try (Workbook wb = new XSSFWorkbook(file.getInputStream())) {
            Sheet sheet = wb.getSheetAt(0);
            for (int i = 1; i <= sheet.getLastRowNum(); i++) { // skip header
                Row row = sheet.getRow(i);
                if (row == null) continue;

                String title = getCellString(row, 0);
                if (title == null || title.isEmpty()) break;

                Task task = new Task();
                task.setProjectId(projectId);
                task.setTitle(title);
                task.setDescription(getCellString(row, 1));
                task.setAssigneeId(getCellLong(row, 2));
                task.setStatus(getCellString(row, 3));
                task.setPriority(getCellString(row, 4));
                task.setEstimatedHours(getCellDecimal(row, 5));
                task.setStartDate(getCellDate(row, 6, dateFmt));
                task.setDueDate(getCellDate(row, 7, dateFmt));
                task.setTags(getCellString(row, 8));

                if (task.getStatus() == null) task.setStatus("todo");
                if (task.getPriority() == null) task.setPriority("normal");
                task.setProgress(0);

                tasks.add(task);
            }
        }

        int count = 0;
        for (Task t : tasks) {
            taskMapper.insert(t);
            count++;
        }
        return count;
    }

    /**
     * 获取成员任务负载数据（用于负载热力图）
     */
    public List<Map<String, Object>> getMemberLoad(Long projectId) {
        List<Task> tasks = taskMapper.selectByProjectWithMember(projectId);
        Map<Long, Map<String, Object>> loadMap = new HashMap<>();

        for (Task t : tasks) {
            if (t.getAssigneeId() == null) continue;
            loadMap.computeIfAbsent(t.getAssigneeId(), k -> {
                Map<String, Object> m = new HashMap<>();
                m.put("assigneeId", k);
                m.put("assigneeName", t.getAssigneeName() != null ? t.getAssigneeName() : "未知");
                m.put("taskCount", 0);
                m.put("doneCount", 0);
                m.put("blockedCount", 0);
                m.put("estimatedHours", BigDecimal.ZERO);
                m.put("actualHours", BigDecimal.ZERO);
                return m;
            });

            Map<String, Object> item = loadMap.get(t.getAssigneeId());
            item.put("taskCount", (int) item.get("taskCount") + 1);
            if ("done".equals(t.getStatus())) item.put("doneCount", (int) item.get("doneCount") + 1);
            if ("blocked".equals(t.getStatus())) item.put("blockedCount", (int) item.get("blockedCount") + 1);
            if (t.getEstimatedHours() != null)
                item.put("estimatedHours", ((BigDecimal) item.get("estimatedHours")).add(t.getEstimatedHours()));
            if (t.getActualHours() != null)
                item.put("actualHours", ((BigDecimal) item.get("actualHours")).add(t.getActualHours()));
        }

        return new ArrayList<>(loadMap.values());
    }

    // ---- 工具方法 ----

    private String getCellString(Row row, int col) {
        Cell cell = row.getCell(col);
        if (cell == null) return null;
        return switch (cell.getCellType()) {
            case STRING -> cell.getStringCellValue().trim();
            case NUMERIC -> String.valueOf((long) cell.getNumericCellValue());
            default -> null;
        };
    }

    private Long getCellLong(Row row, int col) {
        Cell cell = row.getCell(col);
        if (cell == null) return null;
        return switch (cell.getCellType()) {
            case NUMERIC -> (long) cell.getNumericCellValue();
            case STRING -> {
                try { yield Long.valueOf(cell.getStringCellValue().trim()); }
                catch (NumberFormatException e) { yield null; }
            }
            default -> null;
        };
    }

    private BigDecimal getCellDecimal(Row row, int col) {
        Cell cell = row.getCell(col);
        if (cell == null) return null;
        return switch (cell.getCellType()) {
            case NUMERIC -> BigDecimal.valueOf(cell.getNumericCellValue());
            case STRING -> {
                try { yield new BigDecimal(cell.getStringCellValue().trim()); }
                catch (NumberFormatException e) { yield null; }
            }
            default -> null;
        };
    }

    private LocalDate getCellDate(Row row, int col, DateTimeFormatter fmt) {
        Cell cell = row.getCell(col);
        if (cell == null) return null;
        return switch (cell.getCellType()) {
            case STRING -> {
                try { yield LocalDate.parse(cell.getStringCellValue().trim(), fmt); }
                catch (Exception e) { yield null; }
            }
            case NUMERIC -> {
                try { yield cell.getLocalDateTimeCellValue().toLocalDate(); }
                catch (Exception e) { yield null; }
            }
            default -> null;
        };
    }
}
