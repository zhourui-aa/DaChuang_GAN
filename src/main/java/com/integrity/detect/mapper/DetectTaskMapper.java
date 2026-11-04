package com.integrity.detect.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.integrity.detect.entity.DetectTask;

/**
 * detect_tasks · DetectTask 数据访问层（MyBatis-Plus BaseMapper）。
 *
 * <p>检测任务状态机；工作台轮询读取其状态。
 */
public interface DetectTaskMapper extends BaseMapper<DetectTask> {
}
