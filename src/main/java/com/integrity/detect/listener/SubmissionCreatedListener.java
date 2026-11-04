package com.integrity.detect.listener;

import com.integrity.assignment.event.SubmissionCreatedEvent;
import com.integrity.detect.service.DetectService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * 监听「提交已创建」事件并异步触发检测。
 *
 * <p>替代手册的 RocketMQ {@code integrity-submit / SUBMITTED → detect}。
 * P2 换 MQ 时，把本类换成消息消费者即可，业务逻辑不变。
 *
 * <p><b>AFTER_COMMIT 是必需的</b>：提交写库在事务里完成，必须等事务提交后，
 * 另一个线程才读得到该 submission；否则会出现「提交不存在」。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SubmissionCreatedListener {

    private final DetectService detectService;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void onSubmissionCreated(SubmissionCreatedEvent event) {
        try {
            detectService.run(event.submissionId());
        } catch (Exception e) {
            log.error("异步检测失败 submissionId={}", event.submissionId(), e);
        }
    }
}
