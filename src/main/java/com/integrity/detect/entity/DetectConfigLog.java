package com.integrity.detect.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 检测配置调整留痕（表 detect_config_logs）。
 *
 * <p>对应手册「调整记录、生效时间、操作人全部留痕」：每次阈值调整生成一个新版本，
 * 并在此记录旧/新阈值与操作人，供学期末复盘检测口径。
 */
@Data
@TableName("detect_config_logs")
public class DetectConfigLog {

    @TableId(type = IdType.AUTO)
    private Long logId;

    /** 新版本号 */
    private String version;

    /** 调整前基准版本 */
    private String baseVersion;

    /** 调整前阈值（JSON） */
    private String oldThreshold;

    /** 调整后阈值（JSON） */
    private String newThreshold;

    /** 操作人 ID */
    private Long operatorId;

    /** 操作人姓名（冗余，便于展示） */
    private String operatorName;

    /** 调整说明 */
    private String note;

    private LocalDateTime createdAt;
}
