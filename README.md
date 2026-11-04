# 学鉴 · 课程作业 AI 生成内容检测与学术诚信工具（P0）

依据《AI作业诚信检测_技术手册》落地的 P0 版本：**模块化单体后端 + Vue3 教师端**。

## 技术栈

| 层 | 选型 |
|---|---|
| 后端 | JDK **21** · Spring Boot 3.2 · MyBatis-Plus · MySQL 8 · Spring Security + JWT · Knife4j |
| 检测 | 纯统计特征引擎（jieba 分词 + 6 特征），无 LLM |
| 前端 | Vue3 · Vite · Pinia · Vue Router · Element Plus · ECharts |
| 未引入 | Redis / RocketMQ / ES / MinIO / Nacos / Sentinel / Seata（P1/P2 按手册分期补齐） |

## 目录

```
gan-dachang/
├── pom.xml                    后端
├── Makefile                   make run / db-reset / health …
├── src/main/java/com/integrity/
│   ├── common/                Result / 分页 / 异常 / 缓存抽象 / 脱敏 / 健康检查
│   ├── auth/                  登录 / JWT / 四级权限
│   ├── assignment/            课程·作业·提交 / docx·txt·pdf 解析
│   ├── detect/                配置(DSL) / 任务状态机 / 检测引擎(6 特征)
│   ├── evidence/              特征·证据（可溯源）
│   ├── report/                结果·看板
│   ├── appeal/                申诉工单状态机
│   └── governance/            数据集 / 审计
├── src/main/resources/db/     schema.sql（15 表）· data.sql（种子）
├── docs/后端需求分析.md
└── integrity-ui/              前端（Vue3）
    └── src/{api,store,router,layout,components,views}
```

## 运行

**前置**：JDK 21、Maven、MySQL 8（本地 root 无密码）、Node 18+。

```bash
# 1) 建库
mysql -uroot -e "CREATE DATABASE IF NOT EXISTS integrity DEFAULT CHARACTER SET utf8mb4;"

# 2) 启动后端（注意用 JDK 21，默认 JDK 26 会让 Lombok 失效）
make run            # http://localhost:8080/doc.html

# 3) 启动前端
cd integrity-ui && npm install && npm run dev   # http://localhost:5173
```

演示账号：`lijianguo / 123456`

常用：`make health` 探活 · `make db-reset` 重建库（启动时自动灌 schema+data）· `make test` 单测。

## P0 已实现

- 教师端 4 页：教师工作台 / 检测详情 / 申诉处理台 / 课程统计看板 + 登录
- 提交（docx/txt/pdf）→ 解析清洗 → SHA-256 去重 → 异步检测 → 可解释报告
- 6 特征：困惑度(近似) · 改写痕迹 · 行文轨迹(甲) · 句长突发度 · 词汇丰富度 · 结构规律性
- 证据 100% 可溯源（`begin_pos/end_pos` 与原文一致才落库）
- 阈值版本化 + `/api/detect/replay` 回放
- 高关注自动开申诉通道 · 四级权限 · 脱敏 · 审计留痕

## P0 明确记录的近似（见 `detect_configs.feature_dsl.notes`）

- **困惑度**：字符二元条件熵近似，未经语料标定 → P1 换本地模型 logprob
- **行文轨迹**：相邻句实词 Jaccard 平滑度近似（甲）→ P1 换 bge-small-zh embedding

## P1 / P2（手册分期）

- **P1**：本地 LLM（Ollama + LangGraph + Guardrails + Agent Eval）、ES 8（看板聚合 + 相似检索）、MinIO
- **P2**：Nacos/Sentinel/Seata/RocketMQ/XXL-JOB/K8s/SkyWalking 全量、数据集构建与评测回归
