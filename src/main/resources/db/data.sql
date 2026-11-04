-- ============================================================
-- 学鉴 P0 · 种子数据（幂等，可重复执行）
-- 演示账号：lijianguo / 123456（教师）
-- ============================================================

-- ---------- 用户 ----------
INSERT INTO sys_users (user_id, username, password, name, role, college_id, enabled) VALUES
 (1,'lijianguo','$2y$10$7bQ7xDoplrEtFVVVKbjGzuabw.FE8a9JOssKvLEz1tFOmRd4E3ZHS','李建国','TEACHER',1,1),
 (2,'jiaoyanshi','$2y$10$7bQ7xDoplrEtFVVVKbjGzuabw.FE8a9JOssKvLEz1tFOmRd4E3ZHS','王教研室','CHAIR',1,1),
 (3,'xueyuan','$2y$10$7bQ7xDoplrEtFVVVKbjGzuabw.FE8a9JOssKvLEz1tFOmRd4E3ZHS','赵学院','COLLEGE',1,1),
 (4,'admin','$2y$10$7bQ7xDoplrEtFVVVKbjGzuabw.FE8a9JOssKvLEz1tFOmRd4E3ZHS','系统管理员','ADMIN',1,1)
ON DUPLICATE KEY UPDATE name=VALUES(name), role=VALUES(role);

-- ---------- 课程 / 班级 / 学生 ----------
INSERT INTO courses (course_id, course_code, course_name, college_id, teacher_id, term, enabled) VALUES
 (1,'IM101','智能制造技术导论',1,1,'2026-2027-1',1),
 (2,'ME201','机械制图',1,1,'2026-2027-1',1)
ON DUPLICATE KEY UPDATE course_name=VALUES(course_name);

INSERT INTO classes (class_id, class_name, college_id, major, grade) VALUES
 (1,'智能制造 2301 班',1,'智能制造技术',2023),
 (2,'智能制造 2302 班',1,'智能制造技术',2023),
 (3,'智能制造 2303 班',1,'智能制造技术',2023)
ON DUPLICATE KEY UPDATE class_name=VALUES(class_name);

INSERT INTO students (student_id, student_no, name_mask, real_name, class_id) VALUES
 (1,'20230412','张*','张文博',1),
 (2,'20230415','王*','王雪',3),
 (3,'20230421','孙*','孙悦',3),
 (4,'20230418','陈*','陈思远',2),
 (5,'20230410','刘*','刘一鸣',1),
 (6,'20230425','赵*','赵天宇',2)
ON DUPLICATE KEY UPDATE name_mask=VALUES(name_mask);

-- 额外演示学生（专供样例上传验证，不代表真实名单）
INSERT INTO students (student_id, student_no, name_mask, real_name, class_id) VALUES
 (7,'20230430','林*','林佳',1),
 (8,'20230431','黄*','黄磊',2),
 (9,'20230432','徐*','徐婷',3),
 (10,'20230433','何*','何俊',1),
 (11,'20230434','马*','马晓',2)
ON DUPLICATE KEY UPDATE name_mask=VALUES(name_mask);

-- ---------- 作业 ----------
INSERT INTO assignments (assignment_id, course_id, title, require_words, allow_appeal, deadline) VALUES
 (1,1,'人工智能在智能制造中的应用与思考',800,1,'2026-10-15 23:59:59'),
 (2,2,'机械制图大作业 · 减速器装配图',500,1,'2026-10-20 23:59:59'),
 (3,1,'《智能制造技术导论》读书报告',1200,1,'2026-10-25 23:59:59')
ON DUPLICATE KEY UPDATE title=VALUES(title);

-- ---------- 检测配置 v1（特征 DSL，权重和=1） ----------
INSERT INTO detect_configs (config_id, version, feature_dsl, risk_threshold, model_version, frozen) VALUES
 (1,'v1',
  '{"version":"v1","aggregate":"weighted_sum","min_chars":200,"features":[
    {"code":"PERPLEXITY","name":"文本困惑度","method":"stats_gzip_compression_ratio","direction":"inverse","weight":0.25,
     "score_map":{"<=0.55":1.0,"0.55-0.62":0.6,"0.62-0.67":0.3,">0.67":0.0},
     "tag_text_template":"文本压缩比 {value}（困惑度近似），低于人工撰写常见区间，句式可预测性偏高"},
    {"code":"PARAPHRASE_TRACE","name":"改写痕迹","method":"llm_semantic_scan","direction":"direct","weight":0.25,
     "score_map":{"<=0.05":0.0,"0.05-0.12":0.3,"0.12-0.20":0.7,">0.20":1.0},
     "tag_text_template":"检测到 {count} 处改写痕迹，表现为同义替换密集、句意重复度高"},
    {"code":"WRITING_TRAJECTORY","name":"行文轨迹","method":"llm_trajectory_analysis","direction":"direct","weight":0.20,
     "score_map":{"<=0.02":0.0,"0.02-0.08":0.3,"0.08-0.15":0.6,">0.15":0.8},
     "tag_text_template":"行文轨迹过于平直，缺少人工写作常见的修改与回扣痕迹"},
    {"code":"BURSTINESS","name":"句长突发度","method":"stats_sentence_length_variance","direction":"inverse","weight":0.12,
     "score_map":{"<=45":0.8,"45-70":0.4,"70-100":0.15,">100":0.0},
     "tag_text_template":"句长方差 {value}，句子长度分布过于均匀"},
    {"code":"LEXICAL_DIVERSITY","name":"词汇丰富度","method":"stats_type_token_ratio","direction":"inverse","weight":0.10,
     "score_map":{"<=0.72":0.7,"0.72-0.80":0.4,"0.80-0.87":0.15,">0.87":0.0},
     "tag_text_template":"实词类符形符比 {value}，用词丰富度偏低"},
    {"code":"STRUCT_PATTERN","name":"结构规律性","method":"stats_paragraph_template_ratio","direction":"direct","weight":0.08,
     "score_map":{"<=0.3":0.0,"0.3-0.6":0.4,">0.6":0.8},
     "tag_text_template":"{value} 的段落呈模板化展开（开头句式高度一致）"}
  ],"notes":"P0 纯统计特征引擎。6 项 score_map 已按 P0 统计量的实测分布重新标定（与手册中面向真实模型的初始值不同）；P1 接入本地模型后需再次重标定。"}',
  '{"low":0.35,"mid":0.6,"high":0.8}',
  'stats-v0.1', 1)
ON DUPLICATE KEY UPDATE feature_dsl=VALUES(feature_dsl), risk_threshold=VALUES(risk_threshold);
