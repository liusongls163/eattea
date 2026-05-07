# 🍵 eattea — 金融监管知识平台

面向金融机构监管报送场景的知识管理平台，提供**监管制度文档检索**和**金融业务知识库**两大核心模块。

## 痛点解决

- 跨部门监管发文分散，缺乏统一管理和检索
- 实施/研发人员不了解票据、同业、债券等金融业务知识
- 文档格式多样（PDF/Word/Excel），内容无法被搜索

## 功能

### 监管制度文档管理
- 上传 PDF / Word / Excel，自动提取文本内容
- MySQL 8 ngram 中文全文索引，支持语义化搜索
- 按部门、分类、发布日期多维度筛选

### 金融业务知识库
- 预置 35 条金融词条（票据、同业、债券、衍生品、监管指标、综合）
- 按分类浏览，支持增删改查
- 与文档库联合全文检索

### 首页统一搜索
- 输入关键词，同时搜索文档和知识库
- 搜索结果展示匹配片段、分类、来源部门

## 技术栈

| 层级 | 技术 |
|------|------|
| 后端 | Spring Boot 3.2 + MyBatis 3 + MySQL 8 |
| 前端 | Vue 3 + Element Plus + Vite |
| 搜索 | MySQL FULLTEXT + ngram parser |
| 文档解析 | Apache PDFBox 3 / Apache POI 5 |

## 快速开始

### 1. 初始化数据库

```sql
CREATE DATABASE eattea CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE eattea;
source backend/src/main/resources/init.sql;
```

修改 `backend/src/main/resources/application.yml` 中的数据库连接信息。

### 2. 启动后端

```bash
cd backend
mvn spring-boot:run
```

后端启动后访问 http://localhost:8088

### 3. 启动前端

```bash
cd frontend
npm install
npm run dev
```

前端启动后访问 http://localhost:3000

### 4. 部署到内网服务器

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
│       │   ├── config/CorsConfig.java
│       │   ├── controller/
│       │   │   ├── DocumentController.java
│       │   │   └── KnowledgeController.java
│       │   ├── dto/SearchResult.java
│       │   ├── entity/
│       │   │   ├── Document.java
│       │   │   ├── KnowledgeEntry.java
│       │   │   └── Category.java
│       │   ├── mapper/
│       │   │   ├── DocumentMapper.java
│       │   │   ├── KnowledgeEntryMapper.java
│       │   │   └── CategoryMapper.java
│       │   └── service/
│       │       ├── DocumentService.java
│       │       └── KnowledgeService.java
│       └── resources/
│           ├── application.yml
│           ├── init.sql
│           └── mapper/
│               ├── DocumentMapper.xml
│               ├── KnowledgeEntryMapper.xml
│               └── CategoryMapper.xml
└── frontend/
    ├── package.json
    ├── vite.config.js
    └── src/
        ├── App.vue
        ├── main.js
        ├── api/index.js
        ├── router/index.js
        └── views/
            ├── Home.vue
            ├── DocumentList.vue
            ├── DocumentDetail.vue
            └── KnowledgeBase.vue
```

## 数据库表

| 表名 | 说明 |
|------|------|
| `eattea_document` | 监管文档，含 ngram 全文索引 |
| `eattea_knowledge` | 金融知识词条，含 ngram 全文索引 |
| `eattea_category` | 分类标签 |

## 竞赛交付物

本项目作为「AI 提升工作效率」竞赛参赛作品，包含以下数字化资产：

1. **代码** — Spring Boot + Vue 3 完整 Web 应用
2. **数据库** — MySQL 8 全文索引方案（无外部依赖）
3. **知识数据** — 预置 35 条金融业务词条
4. **最佳实践** — 参考下文「使用指南」和「扩展思路」

### 使用指南

1. 管理员上传监管发文 PDF/Word/Excel → 系统自动提取文本 → 建立全文索引
2. 团队成员在首页输入业务关键词 → 即时返回文档匹配片段和知识词条
3. 知识库持续累积金融术语，新人上手不再反复提问

### 扩展思路

- 接入大模型 API，基于检索结果做 RAG 问答
- 文档解析阶段引入 AI 自动分类和标签推荐
- 对接企业 SSO 登录，记录各部门使用数据，量化效率提升
