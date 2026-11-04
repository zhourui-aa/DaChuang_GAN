package com.integrity.assignment.event;

/**
 * 提交创建事件。
 *
 * <p>对应手册的 RocketMQ topic integrity-submit / SUBMITTED：
 * assignment 解析完成 → 通知 detect 触发检测。
 * P0 用 Spring 事件替代 MQ，P2 换 RocketMQ 时只替换发布端与监听端。
 */
public record SubmissionCreatedEvent(Long submissionId) {
}
