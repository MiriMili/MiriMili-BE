package org.example.mirimilibe.notification.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.mirimilibe.member.domain.Member;
import org.example.mirimilibe.member.repository.MemberRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationSchedulerService {

    private final NotificationService notificationService;
    private final MemberRepository memberRepository;

    @Scheduled(cron = "0 0 10 * * ?")
    public void sendDailyHotQuestionNotification() {
        log.info("일일 HOT 질문 알림 발송 시작");

        List<Member> allMembers = memberRepository.findAll();

        for (Member member : allMembers) {
            try {
                notificationService.createDailyHotQuestionNotification(member.getId());
            } catch (Exception e) {
                log.error("HOT 질문 알림 발송 실패 - 회원 ID: {}, 오류: {}", member.getId(), e.getMessage());
            }
        }

        log.info("일일 HOT 질문 알림 발송 완료 - 대상 회원 수: {}", allMembers.size());
    }

    @Scheduled(cron = "0 0 18 * * ?")
    public void sendDailyWaitingAnswerNotification() {
        log.info("일일 답변 대기 알림 발송 시작");

        List<Member> allMembers = memberRepository.findAll();

        for (Member member : allMembers) {
            try {
                notificationService.createDailyWaitingAnswerNotification(member.getId(), member.getNickname());
            } catch (Exception e) {
                log.error("답변 대기 알림 발송 실패 - 회원 ID: {}, 오류: {}", member.getId(), e.getMessage());
            }
        }

        log.info("일일 답변 대기 알림 발송 완료 - 대상 회원 수: {}", allMembers.size());
    }
}