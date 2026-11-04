-- ============================================================
-- 学鉴 · 课程作业 AI 内容检测（P0）
-- 依据《技术手册》§3 冻结 v1；本文件 = 14 张手册表 + 1 张补充表 sys_users
-- 手册规则「只加不改」：sys_users 为 P0 新增（手册未列用户表，但 auth 需要）
-- 全部 IF NOT EXISTS，可重复执行
-- ============================================================

-- ---------- 0. 用户（P0 新增） ----------
CREATE TABLE IF NOT EXISTS sys_users (
  user_id       BIGINT PRIMARY KEY AUTO_INCREMENT,
  username      VARCHAR(50)  NOT NULL COMMENT '登录名',
  password      VARCHAR(120) NOT NULL COMMENT 'BCrypt 摘要',
  name          VARCHAR(40)            COMMENT '姓名',
  role          VARCHAR(20)  NOT NULL DEFAULT 'TEACHER' COMMENT 'TEACHER/CHAIR/COLLEGE/ADMIN',
  college_id    BIGINT                 COMMENT '所属学院',
  enabled       TINYINT      DEFAULT 1,
  created_at    DATETIME     DEFAULT CURRENT_TIMESTAMP,
  UNIQUE KEY uk_username (username)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统用户';

-- ---------- 3.1 课程与提交 ----------
CREATE TABLE IF NOT EXISTS courses (
  course_id     BIGINT PRIMARY KEY AUTO_INCREMENT,
  course_code   VARCHAR(30)  NOT NULL COMMENT '课程代码',
  course_name   VARCHAR(100)          COMMENT '课程名称',
  college_id    BIGINT       NOT NULL COMMENT '开课学院',
  teacher_id    BIGINT       NOT NULL COMMENT '任课教师',
  term          VARCHAR(20)           COMMENT '学期 2026-2027-1（注意：11 字符，不能设 10）',
  enabled       TINYINT      DEFAULT 1,
  created_at    DATETIME     DEFAULT CURRENT_TIMESTAMP,
  UNIQUE KEY uk_course_code (course_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='课程';

CREATE TABLE IF NOT EXISTS classes (
  class_id      BIGINT PRIMARY KEY AUTO_INCREMENT,
  class_name    VARCHAR(60),
  college_id    BIGINT NOT NULL,
  major         VARCHAR(60),
  grade         SMALLINT,
  created_at    DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='班级';

CREATE TABLE IF NOT EXISTS students (
  student_id    BIGINT PRIMARY KEY AUTO_INCREMENT,
  student_no    VARCHAR(20) NOT NULL COMMENT '学号（列表页脱敏）',
  name_mask     VARCHAR(40)          COMMENT '脱敏姓名（张*）',
  real_name     VARCHAR(40)          COMMENT '真实姓名（仅内部分辨用，不外显）',
  class_id      BIGINT,
  created_at    DATETIME DEFAULT CURRENT_TIMESTAMP,
  UNIQUE KEY uk_student_no (student_no),
  KEY idx_class (class_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='学生';

CREATE TABLE IF NOT EXISTS assignments (
  assignment_id BIGINT PRIMARY KEY AUTO_INCREMENT,
  course_id     BIGINT NOT NULL,
  title         VARCHAR(120),
  require_words INT             COMMENT '要求字数（短文本门槛）',
  allow_appeal  TINYINT DEFAULT 1,
  deadline      DATETIME,
  created_at    DATETIME DEFAULT CURRENT_TIMESTAMP,
  KEY idx_course (course_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='作业';

CREATE TABLE IF NOT EXISTS submissions (
  submission_id BIGINT PRIMARY KEY AUTO_INCREMENT,
  assignment_id BIGINT NOT NULL,
  student_id    BIGINT NOT NULL,
  file_key      VARCHAR(200)          COMMENT '原文对象键（P0=本地路径，P1=MinIO）',
  file_name     VARCHAR(200)          COMMENT '原始文件名',
  raw_text      LONGTEXT              COMMENT '解析后纯文本（去噪、去格式）',
  char_count    INT                   COMMENT '去噪后字符数',
  text_sha256   CHAR(64)              COMMENT '文本指纹（去重）',
  submit_status VARCHAR(20) DEFAULT '已提交' COMMENT '已提交/解析失败/待检测',
  created_at    DATETIME DEFAULT CURRENT_TIMESTAMP,
  UNIQUE KEY uk_assign_student (assignment_id, student_id),
  KEY idx_text_sha (text_sha256)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='作业提交';

-- ---------- 3.2 检测配置与任务 ----------
CREATE TABLE IF NOT EXISTS detect_configs (
  config_id     BIGINT PRIMARY KEY AUTO_INCREMENT,
  version       VARCHAR(20) NOT NULL COMMENT 'v1 / v2',
  feature_dsl   JSON             COMMENT '检测特征 DSL',
  risk_threshold JSON            COMMENT '{"low":0.35,"mid":0.6,"high":0.8}',
  model_version VARCHAR(60)      COMMENT '引擎/模型版本',
  frozen        TINYINT DEFAULT 0 COMMENT '冻结后只加不改',
  created_at    DATETIME DEFAULT CURRENT_TIMESTAMP,
  UNIQUE KEY uk_version (version)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='检测配置（版本化，可回看）';

CREATE TABLE IF NOT EXISTS detect_tasks (
  task_id       BIGINT PRIMARY KEY AUTO_INCREMENT,
  submission_id BIGINT NOT NULL,
  config_id     BIGINT NOT NULL,
  status        VARCHAR(20) DEFAULT '待检测' COMMENT '待检测/检测中/已完成/已降级/失败',
  degraded      TINYINT DEFAULT 0,
  cost_ms       INT,
  retry_count   TINYINT DEFAULT 0,
  error_msg     VARCHAR(300),
  created_at    DATETIME DEFAULT CURRENT_TIMESTAMP,
  finished_at   DATETIME,
  KEY idx_status (status),
  KEY idx_submission (submission_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='检测任务';

-- ---------- 3.3 特征、证据与结果 ----------
CREATE TABLE IF NOT EXISTS detect_features (
  feature_id    BIGINT PRIMARY KEY AUTO_INCREMENT,
  task_id       BIGINT NOT NULL,
  feature_code  VARCHAR(40) NOT NULL COMMENT 'PERPLEXITY/PARAPHRASE_TRACE/...',
  feature_name  VARCHAR(40),
  feature_value DECIMAL(10,4)        COMMENT '原始指标值',
  feature_score DECIMAL(5,4)         COMMENT '归一化风险分 0-1',
  weight        DECIMAL(4,3)         COMMENT '本次生效权重',
  feature_text  VARCHAR(300)         COMMENT '中性自然语言描述',
  source        VARCHAR(20)          COMMENT 'STATS / LLM',
  KEY idx_task (task_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='检测特征明细';

CREATE TABLE IF NOT EXISTS detect_evidence (
  evidence_id   BIGINT PRIMARY KEY AUTO_INCREMENT,
  task_id       BIGINT NOT NULL,
  feature_code  VARCHAR(40),
  begin_pos     INT NOT NULL COMMENT '原文起始字符位',
  end_pos       INT NOT NULL COMMENT '结束位（左闭右开）',
  snippet       VARCHAR(500) COMMENT '证据片段原文',
  reason        VARCHAR(300) COMMENT '何以成为证据（中性描述）',
  KEY idx_task_code (task_id, feature_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='检测证据（可溯源）';

CREATE TABLE IF NOT EXISTS detect_results (
  result_id     BIGINT PRIMARY KEY AUTO_INCREMENT,
  task_id       BIGINT NOT NULL,
  submission_id BIGINT NOT NULL,
  risk_score    DECIMAL(5,4)         COMMENT '综合分 0-1',
  risk_level    VARCHAR(20)          COMMENT '低关注/中关注/高关注',
  confidence    DECIMAL(5,4)         COMMENT '置信度',
  summary       TEXT                 COMMENT '检测说明（含仅供参考声明）',
  review_suggestion VARCHAR(300)     COMMENT '人工复核建议',
  config_version VARCHAR(20)         COMMENT '所用配置版本',
  created_at    DATETIME DEFAULT CURRENT_TIMESTAMP,
  UNIQUE KEY uk_task (task_id),
  KEY idx_submission (submission_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='检测结果';

-- ---------- 3.5 申诉工单与复核 ----------
CREATE TABLE IF NOT EXISTS appeals (
  appeal_id     BIGINT PRIMARY KEY AUTO_INCREMENT,
  submission_id BIGINT NOT NULL,
  student_id    BIGINT NOT NULL,
  trigger_task_id BIGINT,
  statement     TEXT COMMENT '学生陈述',
  attach_keys   JSON COMMENT '佐证附件对象键数组',
  status        VARCHAR(20) DEFAULT '待受理' COMMENT '待受理/复核中/已认定/已驳回/已归档',
  created_at    DATETIME DEFAULT CURRENT_TIMESTAMP,
  KEY idx_submission (submission_id),
  KEY idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='申诉工单';

CREATE TABLE IF NOT EXISTS appeal_reviews (
  review_id     BIGINT PRIMARY KEY AUTO_INCREMENT,
  appeal_id     BIGINT NOT NULL,
  reviewer_id   BIGINT NOT NULL,
  review_conclusion VARCHAR(20) COMMENT '认定为人工撰写/证据不足/维持关注',
  review_note   TEXT COMMENT '复核说明',
  threshold_feedback VARCHAR(30) COMMENT '阈值需调整标记',
  created_at    DATETIME DEFAULT CURRENT_TIMESTAMP,
  KEY idx_appeal (appeal_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='申诉复核';

-- ---------- 3.6 数据集与审计 ----------
CREATE TABLE IF NOT EXISTS datasets (
  dataset_id    BIGINT PRIMARY KEY AUTO_INCREMENT,
  name          VARCHAR(80),
  sample_type   VARCHAR(20) COMMENT 'HUMAN / AI / HOLDOUT',
  sample_count  INT,
  minio_prefix  VARCHAR(200),
  labeled       TINYINT DEFAULT 0,
  consent_note  VARCHAR(200),
  created_at    DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='标注数据集登记';

CREATE TABLE IF NOT EXISTS audit_logs (
  log_id        BIGINT PRIMARY KEY AUTO_INCREMENT,
  operator_id   BIGINT NOT NULL,
  operator_role VARCHAR(20),
  action        VARCHAR(40) COMMENT 'VIEW_DETECT/EXPORT_REPORT/REVIEW_APPEAL',
  target_submission BIGINT,
  ip            VARCHAR(40),
  created_at    DATETIME DEFAULT CURRENT_TIMESTAMP,
  KEY idx_operator (operator_id),
  KEY idx_target (target_submission)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='审计日志';

-- ---------- 配置调整留痕（P0 新增，手册「只加不改」） ----------
CREATE TABLE IF NOT EXISTS detect_config_logs (
  log_id        BIGINT PRIMARY KEY AUTO_INCREMENT,
  version       VARCHAR(20) NOT NULL COMMENT '新生成/生效的版本',
  base_version  VARCHAR(20)          COMMENT '调整前基准版本',
  old_threshold JSON                 COMMENT '调整前阈值',
  new_threshold JSON                 COMMENT '调整后阈值',
  operator_id   BIGINT,
  operator_name VARCHAR(40),
  note          VARCHAR(200),
  created_at    DATETIME DEFAULT CURRENT_TIMESTAMP,
  KEY idx_version (version)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='检测配置调整留痕（阈值可回看）';
