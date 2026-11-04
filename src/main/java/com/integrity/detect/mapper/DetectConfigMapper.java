package com.integrity.detect.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.integrity.detect.entity.DetectConfig;

/**
 * detect_configs · DetectConfig 数据访问层（MyBatis-Plus BaseMapper）。
 *
 * <p>检测配置版本；DSL 与阈值读取的入口，支持按版本回放。
 */
public interface DetectConfigMapper extends BaseMapper<DetectConfig> {
}
