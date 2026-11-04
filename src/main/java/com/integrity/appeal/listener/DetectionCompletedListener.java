package com.integrity.appeal.listener;

import com.integrity.appeal.service.AppealService;
import com.integrity.detect.event.DetectionCompletedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * 检测完成 → 高关注自动开申诉通道。
 *
 * <p>对应手册 integrity-flag / HIGH_RISK → appeal。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DetectionCompletedListener {

    private final AppealService appealService;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void onDetectionCompleted(DetectionCompletedEvent event) {
        try {
            appealService.autoDraft(event.submissionId(), event.assignmentId(), event.riskLevel());
        } catch (Exception e) {
            log.error("自动建申诉草稿失败 submissionId={}", event.submissionId(), e);
        }
    }
}
