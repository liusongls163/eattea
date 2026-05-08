-- ============================================
-- PM 驾驶舱 — 项目管理数据表
-- MySQL 8 中执行:
--   1. CREATE DATABASE pm_cockpit CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
--   2. USE pm_cockpit;
--   3. source init.sql;
-- ============================================

-- 项目表
CREATE TABLE IF NOT EXISTS pm_project (
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    name          VARCHAR(200)  NOT NULL COMMENT '项目名称',
    description   TEXT          COMMENT '项目描述',
    status        VARCHAR(20)   NOT NULL DEFAULT 'active' COMMENT '状态: active/closed/paused',
    health        VARCHAR(20)   COMMENT '健康度: green/yellow/red',
    start_date    DATE          COMMENT '开始日期',
    end_date      DATE          COMMENT '预计结束日期',
    create_time   DATETIME      DEFAULT CURRENT_TIMESTAMP,
    update_time   DATETIME      DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='项目表';

-- 成员表
CREATE TABLE IF NOT EXISTS pm_member (
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    name          VARCHAR(100)  NOT NULL COMMENT '成员姓名',
    role          VARCHAR(50)   COMMENT '角色：PM/开发/测试/实施',
    email         VARCHAR(200)  COMMENT '邮箱',
    department    VARCHAR(100)  COMMENT '部门',
    create_time   DATETIME      DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='项目成员';

-- 任务表
CREATE TABLE IF NOT EXISTS pm_task (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    project_id      BIGINT        NOT NULL COMMENT '所属项目',
    title           VARCHAR(500)  NOT NULL COMMENT '任务标题',
    description     TEXT          COMMENT '任务描述',
    assignee_id     BIGINT        COMMENT '负责人ID',
    status          VARCHAR(20)   NOT NULL DEFAULT 'todo' COMMENT '状态: todo/in_progress/done/blocked',
    priority        VARCHAR(10)   DEFAULT 'normal' COMMENT '优先级: high/normal/low',
    estimated_hours DECIMAL(8,2)  COMMENT '预估工时(小时)',
    actual_hours    DECIMAL(8,2)  COMMENT '实际工时(小时)',
    start_date      DATE          COMMENT '开始日期',
    due_date        DATE          COMMENT '截止日期',
    progress        INT           DEFAULT 0 COMMENT '进度百分比 0-100',
    tags            VARCHAR(500)  COMMENT '标签，逗号分隔',
    create_time     DATETIME      DEFAULT CURRENT_TIMESTAMP,
    update_time     DATETIME      DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_project (project_id),
    INDEX idx_assignee (assignee_id),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='项目任务';

-- AI 健康诊断记录表
CREATE TABLE IF NOT EXISTS pm_health_check (
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    project_id    BIGINT        NOT NULL COMMENT '所属项目',
    check_date    DATETIME      DEFAULT CURRENT_TIMESTAMP COMMENT '诊断时间',
    health_status VARCHAR(20)   COMMENT '健康度: green/yellow/red',
    overdue_tasks INT           COMMENT '逾期任务数',
    blocked_tasks INT           COMMENT '阻塞任务数',
    risk_summary  TEXT          COMMENT '风险摘要（AI生成）',
    suggestions   TEXT          COMMENT '改进建议（AI生成）',
    member_load   TEXT          COMMENT '成员负载JSON',
    create_time   DATETIME      DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_project (project_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='项目健康诊断记录';

-- AI 报告表（周报/月报/复盘）
CREATE TABLE IF NOT EXISTS pm_report (
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    project_id    BIGINT        NOT NULL COMMENT '所属项目',
    type          VARCHAR(20)   NOT NULL COMMENT '类型: weekly/monthly/review',
    title         VARCHAR(500)  COMMENT '报告标题',
    content       TEXT          COMMENT '报告正文（AI生成）',
    period_start  DATE          COMMENT '报告周期起始',
    period_end    DATE          COMMENT '报告周期结束',
    create_time   DATETIME      DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_project (project_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI生成的报告';

-- 任务依赖关系表
CREATE TABLE IF NOT EXISTS pm_task_dependency (
    id               BIGINT AUTO_INCREMENT PRIMARY KEY,
    task_id          BIGINT        NOT NULL COMMENT '任务ID',
    depends_on_id    BIGINT        NOT NULL COMMENT '依赖的前置任务ID',
    create_time      DATETIME      DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_task (task_id),
    INDEX idx_depends (depends_on_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='任务依赖关系';

-- 里程碑表
CREATE TABLE IF NOT EXISTS pm_milestone (
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    project_id    BIGINT        NOT NULL COMMENT '所属项目',
    name          VARCHAR(200)  NOT NULL COMMENT '里程碑名称',
    description   TEXT          COMMENT '描述',
    target_date   DATE          COMMENT '目标日期',
    actual_date   DATE          COMMENT '实际完成日期',
    status        VARCHAR(20)   DEFAULT 'pending' COMMENT '状态: pending/achieved/missed',
    create_time   DATETIME      DEFAULT CURRENT_TIMESTAMP,
    update_time   DATETIME      DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_project (project_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='项目里程碑';

-- 风险登记表
CREATE TABLE IF NOT EXISTS pm_risk (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    project_id      BIGINT        NOT NULL COMMENT '所属项目',
    title           VARCHAR(500)  NOT NULL COMMENT '风险描述',
    category        VARCHAR(50)   COMMENT '风险类别: 技术风险/资源风险/进度风险/外部风险/合规风险',
    probability     VARCHAR(10)   DEFAULT 'medium' COMMENT '发生概率: high/medium/low',
    impact          VARCHAR(10)   DEFAULT 'medium' COMMENT '影响程度: high/medium/low',
    level           VARCHAR(10)   COMMENT '风险等级: high/medium/low（概率×影响）',
    status          VARCHAR(20)   DEFAULT 'open' COMMENT '状态: open/mitigating/closed',
    mitigation      TEXT          COMMENT '应对措施',
    owner_id        BIGINT        COMMENT '责任人ID',
    identified_date DATE          COMMENT '识别日期',
    resolved_date   DATE          COMMENT '解决日期',
    create_time     DATETIME      DEFAULT CURRENT_TIMESTAMP,
    update_time     DATETIME      DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_project (project_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='项目风险登记';

-- 干系人表
CREATE TABLE IF NOT EXISTS pm_stakeholder (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    project_id      BIGINT        NOT NULL COMMENT '所属项目',
    name            VARCHAR(100)  NOT NULL COMMENT '姓名',
    role            VARCHAR(100)  COMMENT '角色/职务',
    department      VARCHAR(200)  COMMENT '所在部门',
    influence       VARCHAR(10)   DEFAULT 'normal' COMMENT '影响力: high/normal/low',
    contact         VARCHAR(200)  COMMENT '联系方式',
    expectations    TEXT          COMMENT '期望/关注点',
    create_time     DATETIME      DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_project (project_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='项目干系人';

-- ============================================
-- 预置示例数据
-- ============================================

-- 示例成员
INSERT INTO pm_member (name, role, email, department) VALUES
('张明', 'PM', 'zhangming@example.com', '项目管理部'),
('李华', '开发', 'lihua@example.com', '研发部'),
('王芳', '开发', 'wangfang@example.com', '研发部'),
('赵强', '测试', 'zhaoqiang@example.com', '质量保障部'),
('刘莉', '实施', 'liuli@example.com', '实施部'),
('陈伟', '开发', 'chenwei@example.com', '研发部'),
('孙丽', 'PM', 'sunli@example.com', '项目管理部');

-- 示例项目
INSERT INTO pm_project (name, description, status, health, start_date, end_date) VALUES
('监管报送平台一期', '建设面向银保监会的EAST5.0数据报送系统，完成数据采集、校验、报送全流程', 'active', 'yellow', '2026-03-01', '2026-08-31'),
('反洗钱系统升级', '配合2026年反洗钱新规，升级大额可疑交易监测模型和报告流程', 'active', 'green', '2026-04-15', '2026-07-30'),
('数据治理平台', '建设全行级数据标准管理、元数据管理、数据质量监控平台', 'active', 'red', '2026-02-01', '2026-09-30');

-- 示例任务（监管报送平台一期）
INSERT INTO pm_task (project_id, title, description, assignee_id, status, priority, estimated_hours, actual_hours, start_date, due_date, progress, tags) VALUES
(1, '需求调研与监管制度梳理', '梳理EAST5.0报送要求，对接业务部门确认字段口径', 1, 'done', 'high', 40, 42, '2026-03-01', '2026-03-20', 100, '需求,调研'),
(1, '数据模型设计', '设计EAST5.0各主题域数据模型，完成逻辑模型和物理模型', 2, 'done', 'high', 60, 58, '2026-03-10', '2026-04-05', 100, '设计,数据模型'),
(1, 'ETL采集模块开发', '开发从核心系统、信贷系统等源系统的数据采集接口', 2, 'in_progress', 'high', 80, 45, '2026-04-01', '2026-06-15', 56, '开发,ETL'),
(1, '数据校验模块开发', '按EAST5.0校验规则开发自动校验引擎', 3, 'in_progress', 'high', 60, 30, '2026-04-10', '2026-06-20', 50, '开发,校验'),
(1, '前端报送页面开发', '实现数据预览、报送任务调度、提交审批等功能页面', 6, 'todo', 'normal', 60, 0, '2026-06-01', '2026-07-15', 0, '前端,页面'),
(1, '联调测试', 'ETL+校验+报送全流程联调', 4, 'todo', 'high', 40, 0, '2026-07-01', '2026-08-10', 0, '测试'),
(1, '上线部署与培训', '生产环境部署，对业务人员培训数据报送操作', 5, 'todo', 'high', 24, 0, '2026-08-15', '2026-08-31', 0, '上线,培训'),
(1, '数据采集接口超时优化', '部分源系统查询超时，需要加缓存层和异步处理', 2, 'blocked', 'normal', 20, 8, '2026-04-20', '2026-04-30', 40, '优化,性能'),
(1, '校验规则覆盖率提升', '当前覆盖65%，需要补充至90%+', 3, 'in_progress', 'high', 30, 10, '2026-05-01', '2026-05-25', 33, '校验,覆盖率');

-- 示例任务（反洗钱系统升级）
INSERT INTO pm_task (project_id, title, description, assignee_id, status, priority, estimated_hours, actual_hours, start_date, due_date, progress, tags) VALUES
(2, '新规条款分析', '分析2026反洗钱新规条款，提炼对系统的变更要求', 1, 'done', 'high', 16, 14, '2026-04-15', '2026-04-25', 100, '需求,法规'),
(2, '大额交易监测模型升级', '根据新规修订大额交易阈值和监测规则', 2, 'in_progress', 'high', 40, 20, '2026-04-22', '2026-05-30', 50, '开发,模型'),
(2, '可疑交易规则更新', '更新可疑交易识别规则，增加新型洗钱模式识别', 3, 'in_progress', 'high', 50, 25, '2026-05-01', '2026-06-15', 50, '开发,规则'),
(2, '报告自动化模板', '改造可疑交易报告的自动生成模板，适配新规格式', 6, 'todo', 'normal', 30, 0, '2026-06-01', '2026-06-30', 0, '前端,模板'),
(2, '回归测试', '全量回归测试，确保新模型不影响现有功能', 4, 'todo', 'high', 40, 0, '2026-07-01', '2026-07-20', 0, '测试'),
(2, '用户验收', '组织业务部门进行UAT验收', 1, 'todo', 'high', 16, 0, '2026-07-20', '2026-07-30', 0, '验收');

-- 示例任务（数据治理平台）
INSERT INTO pm_task (project_id, title, description, assignee_id, status, priority, estimated_hours, actual_hours, start_date, due_date, progress, tags) VALUES
(3, '数据标准调研', '调研各业务系统的数据标准和字典定义', 1, 'done', 'high', 30, 32, '2026-02-01', '2026-02-28', 100, '调研'),
(3, '元数据采集器开发', '开发元数据自动采集器，对接Oracle/MySQL/Hive等', 2, 'in_progress', 'high', 80, 50, '2026-03-01', '2026-05-31', 63, '开发,元数据'),
(3, '数据质量规则引擎', '实现可配置的数据质量检查规则引擎', 3, 'in_progress', 'high', 60, 40, '2026-03-15', '2026-05-15', 67, '开发,质量'),
(3, '前端治理门户', '数据地图、标准浏览、质量报告页面', 6, 'in_progress', 'normal', 50, 20, '2026-04-01', '2026-06-30', 40, '前端'),
(3, '需求方接入接口联调', '征信系统、风控系统接入标准数据服务', 2, 'blocked', 'high', 30, 0, '2026-04-15', '2026-04-30', 0, '接口,联调'),
(3, '数据质量SLA告警开发', '实现定时扫描+异常告警推送', 3, 'blocked', 'high', 30, 5, '2026-04-01', '2026-04-20', 17, '告警,运维'),
(3, '全流程测试', '功能+性能+安全测试', 4, 'todo', 'high', 50, 0, '2026-07-01', '2026-08-15', 0, '测试'),
(3, '用户培训文档', '编写管理员手册和操作视频', 5, 'todo', 'normal', 16, 0, '2026-09-01', '2026-09-15', 0, '培训'),
(3, '性能瓶颈排查', '元数据采集全量扫描耗时过长，需排查优化', 2, 'blocked', 'high', 20, 4, '2026-04-10', '2026-04-25', 20, '性能,排查');

-- ============================================
-- 任务依赖关系（监管报送平台一期）
-- ============================================
-- task_id 依赖 depends_on_id：必须 depends_on_id 完成后 task_id 才能开始
INSERT INTO pm_task_dependency (task_id, depends_on_id) VALUES
(3, 1),  -- ETL开发 依赖 需求调研
(3, 2),  -- ETL开发 依赖 数据模型
(4, 1),  -- 校验开发 依赖 需求调研
(4, 2),  -- 校验开发 依赖 数据模型
(5, 3),  -- 前端页面 依赖 ETL开发
(5, 4),  -- 前端页面 依赖 校验开发
(6, 3),  -- 联调测试 依赖 ETL开发
(6, 4),  -- 联调测试 依赖 校验开发
(9, 3);  -- 校验覆盖率 依赖 ETL开发（需有数据后才能校验）

-- 任务依赖关系（数据治理平台）
INSERT INTO pm_task_dependency (task_id, depends_on_id) VALUES
(18, 16), -- 元数据采集器 依赖 标准调研
(19, 16), -- 质量规则引擎 依赖 标准调研
(20, 18), -- 前端门户 依赖 元数据采集器
(21, 18), -- 接口联调 依赖 元数据采集器
(22, 19), -- SLA告警 依赖 质量规则引擎
(24, 18), -- 全流程测试 依赖 元数据采集器
(24, 19), -- 全流程测试 依赖 质量规则引擎
(25, 18); -- 性能排查 依赖 元数据采集器

-- ============================================
-- 里程碑（监管报送平台一期）
-- ============================================
INSERT INTO pm_milestone (project_id, name, description, target_date, status) VALUES
(1, '需求评审完成', 'EAST5.0需求文档评审通过', '2026-03-20', 'achieved'),
(1, '数据模型评审通过', '各主题域模型评审通过', '2026-04-05', 'achieved'),
(1, 'ETL开发完成', '所有源系统采集接口开发完成', '2026-06-15', 'pending'),
(1, '校验引擎上线', '自动校验引擎开发完成并通过测试', '2026-06-20', 'pending'),
(1, 'SIT测试通过', '全流程系统集成测试通过', '2026-08-10', 'pending'),
(1, '投产上线', '生产环境部署完成', '2026-08-31', 'pending');

-- 里程碑（数据治理平台）
INSERT INTO pm_milestone (project_id, name, description, target_date, status) VALUES
(3, '标准调研完成', '全行数据标准调研输出报告', '2026-02-28', 'achieved'),
(3, '元数据采集器v1', '核心系统元数据自动采集', '2026-05-31', 'pending'),
(3, '质量规则引擎上线', '数据质量检查引擎完成', '2026-05-15', 'pending'),
(3, '治理门户上线', '数据治理前端页面部署', '2026-06-30', 'pending'),
(3, '全流程测试通过', '功能+性能+安全测试通过', '2026-08-15', 'pending'),
(3, '正式上线', '全行推广使用', '2026-09-30', 'pending');

-- ============================================
-- 干系人
-- ============================================
INSERT INTO pm_stakeholder (project_id, name, role, department, influence, contact, expectations) VALUES
(1, '陈总', '部门总监', '风险管理部', 'high', 'chenz@example.com', '按时完成EAST5.0报送，确保数据质量'),
(1, '刘主任', '监管报送负责人', '风险管理部', 'high', 'liuz@example.com', '报送流程自动化，减少手工操作'),
(1, '马工', '业务系统负责人', '科技部', 'normal', 'mag@example.com', '接口稳定，不影响源系统性能'),
(3, '陈总', '部门总监', '数据管理部', 'high', 'chenz@example.com', '全行数据标准统一，质量可视'),
(3, '周博', '数据架构师', '数据管理部', 'high', 'zhoub@example.com', '元数据自动采集覆盖率达95%+'),
(3, '赵经理', '应用系统负责人', '科技部', 'normal', 'zhaoj@example.com', '接入数据服务不影响现有系统运行');

-- ============================================
-- 风险登记
-- ============================================
INSERT INTO pm_risk (project_id, title, category, probability, impact, level, status, mitigation, owner_id, identified_date) VALUES
(1, 'ETL采集接口性能不足导致数据延误', '技术风险', 'high', 'high', 'high', 'mitigating', '增加缓存层+异步采集；与源系统协商调优', 2, '2026-04-15'),
(1, 'EAST5.0校验规则频繁变更', '外部风险', 'medium', 'high', 'high', 'open', '安排专人跟踪银保监会发文；校验引擎设计为规则可配置', 1, '2026-04-20'),
(1, '前端开发人力不足', '资源风险', 'high', 'medium', 'high', 'open', '协调PM争取优先级；考虑外包或调配', 1, '2026-05-01'),
(1, '联调阶段源系统环境不稳定', '技术风险', 'medium', 'medium', 'medium', 'open', '提前搭建Mock环境；与源系统团队协调测试窗口', 4, '2026-05-05'),
(3, '元数据自动采集覆盖率不达95%目标', '技术风险', 'high', 'high', 'high', 'mitigating', '优先支持核心系统；非结构化数据转为手工补录过渡', 2, '2026-03-10'),
(3, '数据质量规则引擎性能瓶颈', '技术风险', 'medium', 'high', 'high', 'open', '规则执行分批异步化；引入列存储加速', 3, '2026-04-01'),
(3, '业务部门配合度低，数据标准推动困难', '外部风险', 'high', 'high', 'high', 'mitigating', '争取高层支持；先以风险管理部为突破口示范', 1, '2026-03-15'),
(3, '全流程测试时间被压缩', '进度风险', 'medium', 'medium', 'medium', 'open', '提前介入测试左移；关键路径优先保障', 4, '2026-05-01');
