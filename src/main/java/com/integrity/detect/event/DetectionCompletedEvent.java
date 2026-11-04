package com.integrity.detect.event;

/**
 * 检测完成事件。
 *
 * <p>对应手册 integrity-flag / HIGH_RISK：高关注结果 → 自动建申诉草稿工单。
 * P0 用 Spring 事件；P2 换 RocketMQ。
 */
public record DetectionCompletedEvent(Long submissionId, Long assignmentId, String riskLevel) {
}
