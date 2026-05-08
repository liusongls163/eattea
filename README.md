# 🚀 PM 驾驶舱 — AI 驱动的项目管理提效工具

面向项目经理的项目管理驾驶舱，通过 AI 实现**项目健康度自动诊断**、**周报/月报/复盘一键生成**和**人力负载智能分析**，帮助 PM 从重复劳动中解放，聚焦决策。

## 核心功能

### 1. 项目仪表盘
- 多项目卡片视图，绿/黄/红健康度一目了然
- 完成率、逾期数、阻塞数实时展示
- 点击进入项目详情

### 2. AI 健康诊断
- 一键触发 AI 分析当前项目状态
- 输出：风险摘要 + 改进建议 + 健康等级
- 诊断记录自动存档，可追溯

### 3. AI 报告生成
- 周报：本周进展 + 风险 + 下周计划
- 月报：月度总结 + 关键指标
- 复盘：项目结束后自动生成复盘文档
- 报告自动保存，支持历史查看

### 4. 任务管理
- 完整的 CRUD 操作
- 支持 Excel 批量导入任务
- 进度追踪 + 逾期自动标记

### 5. 人力负载分析
- 按成员统计任务数、完成率、预估工时
- 可视化负载条，快速识别过载/闲置

### 6. 燃尽图
- SVG 渲染，实际完成 vs 计划完成两条线对比
- 今日线标注，一眼判断进度偏差
- 纯数据驱动，无需外部图表库

### 7. 延期预测
- 基于日均推进速率，预测每项任务最终完成度
- 高/中/低三级风险标注 + 具体建议措施
- 已逾期任务自动标红

### 8. 阻塞影响分析
- 递归查找阻塞任务的所有直接 + 间接依赖
- 列出受影响任务、负责人、依赖深度
- 统计受影响工时总量

### 9. 工时偏差预警
- 实际工时 vs 预估工时偏差率计算
- 超 50% → 严重 / 30-50% → 偏高 / < 30% → 正常
- 帮 PM 识别「估不准」的任务类型

### 10. 里程碑管理
- 时间轴可视化展示
- pending / achieved / missed 三态
- 支持增删改

### 11. 干系人管理
- 记录姓名、角色、部门、影响力、联系方式、关注点
- 按影响力排序

## 技术栈

| 层级 | 技术 |
|------|------|
| 后端 | Spring Boot 3.2 + MyBatis 3 + MySQL 8 |
| 前端 | Vue 3 + Element Plus + Vite |
| AI | 支持 OpenAI 兼容 API（可接入 DeepSeek 等） |
| Excel | Apache POI 5 |

## 快速开始

### 1. 初始化数据库

```sql
CREATE DATABASE pm_cockpit CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE pm_cockpit;
source backend/src/main/resources/init.sql;
```

修改 `backend/src/main/resources/application.yml` 中的数据库连接信息。

### 2. 配置 AI（可选）

```yaml
pm:
  ai:
    enabled: true
    endpoint: https://api.openai.com/v1/chat/completions
    api-key: sk-your-key-here
    model: gpt-4o-mini
```

支持任何 OpenAI 兼容的 API 端点（包括 DeepSeek、国产大模型等）。
未启用 AI 时，系统使用规则引擎进行健康度判定。

### 3. 启动后端

```bash
cd backend
mvn spring-boot:run
```

后端启动后访问 http://localhost:8088

### 4. 启动前端

```bash
cd frontend
npm install
npm run dev
```

前端启动后访问 http://localhost:3000

### 5. 部署

```bash
# 打包后端
cd backend
mvn clean package -DskipTests
# target/eattea-backend-1.0.0.jar

# 打包前端
cd frontend
npm run build
# dist/ 目录部署到 nginx
```

## 项目结构

```
eattea/
├── backend/
│   ├── pom.xml
│   └── src/main/
│       ├── java/com/eattea/
│       │   ├── EatteaApplication.java
│       │   ├── config/
│       │   │   ├── CorsConfig.java
│       │   │   └── AppConfig.java
│       │   ├── controller/
│       │   │   ├── ProjectController.java
│       │   │   ├── TaskController.java
│       │   │   ├── MemberController.java
│       │   │   └── AIController.java
│       │   ├── dto/
│       │   │   ├── AIRequest.java
│       │   │   ├── AIResponse.java
│       │   │   └── ProjectStats.java
│       │   ├── entity/
│       │   │   ├── Project.java
│       │   │   ├── Task.java
│       │   │   ├── Member.java
│       │   │   ├── HealthCheck.java
│       │   │   └── Report.java
│       │   ├── mapper/
│       │   │   ├── ProjectMapper.java
│       │   │   ├── TaskMapper.java
│       │   │   ├── MemberMapper.java
│       │   │   ├── HealthCheckMapper.java
│       │   │   └── ReportMapper.java
│       │   └── service/
│       │       ├── ProjectService.java
│       │       ├── TaskService.java
│       │       ├── MemberService.java
│       │       └── AIService.java
│       └── resources/
│           ├── application.yml
│           ├── init.sql
│           └── mapper/
│               ├── ProjectMapper.xml
│               ├── TaskMapper.xml
│               ├── MemberMapper.xml
│               ├── HealthCheckMapper.xml
│               └── ReportMapper.xml
└── frontend/
    ├── package.json
    ├── vite.config.js
    └── src/
        ├── App.vue
        ├── main.js
        ├── api/index.js
        ├── router/index.js
        └── views/
            ├── Dashboard.vue
            ├── ProjectDetail.vue
            └── Members.vue
```

## 数据库表

| 表名 | 说明 |
|------|------|
| `pm_project` | 项目信息，含健康度字段 |
| `pm_member` | 团队成员 |
| `pm_task` | 项目任务，含状态/进度/工时 |
| `pm_health_check` | AI 健康诊断记录 |
| `pm_report` | AI 生成报告（周报/月报/复盘） |
| `pm_task_dependency` | 任务依赖关系 |
| `pm_milestone` | 项目里程碑 |
| `pm_stakeholder` | 干系人管理 |
| `pm_risk` | 风险登记（概率×影响=等级） |

## 竞赛交付物

本项目作为「AI 提升工作效率」竞赛参赛作品，包含：

1. **完整代码** — Spring Boot + Vue 3 前后端分离应用
2. **AI Prompt 资产** — 健康诊断 Prompt、周报生成 Prompt、复盘 Prompt
3. **预置数据** — 3 个项目 + 24 条示例任务 + 7 名成员
4. **使用指南** — 参考下文

### 使用指南

1. **创建项目** → 仪表盘点击「新建项目」，填入项目信息
2. **管理任务** → 进入项目，新增/编辑任务；支持 Excel 批量导入
3. **AI 健康诊断** → 点击「AI 健康诊断」，AI 自动分析并给出风险摘要和改进建议
4. **生成报告** → 点击「AI 生成报告」，选择周报/月报/复盘类型，AI 自动生成
5. **查看负载** → 项目详情页底部，实时展示每个成员的任务负载

### 效率提升量化

| 场景 | 传统方式 | PM 驾驶舱 | 预估节省 |
|------|----------|-----------|----------|
| 项目状态摸排 | 逐个任务翻看，~30分钟 | 仪表盘一目了然 + AI 诊断 | 25分钟/次 |
| 周报编写 | 手动收集信息、排版，~40分钟 | AI 一键生成，人工微调 | 30分钟/周 |
| 风险发现 | 依赖例会或被动上报 | AI 主动标注逾期/阻塞 | 提前1-3天 |
| 复盘文档 | 手写，~3小时 | AI 生成初稿，人工补充 | 2小时/项目 |

### 扩展思路

- 对接 Jira/TAPD API，自动同步任务数据
- 接入企业微信/钉钉，健康预警主动推送
- 工时数据积累后，训练工时预估模型
- 增加 Prompt 模板市场，团队成员共享优质 Prompt
