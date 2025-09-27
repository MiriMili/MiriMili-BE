package org.example.mirimilibe.notification.service;

import lombok.RequiredArgsConstructor;
import org.example.mirimilibe.global.CommonPageResponse;
import org.example.mirimilibe.global.error.MemberErrorCode;
import org.example.mirimilibe.global.exception.MiriMiliException;
import org.example.mirimilibe.member.domain.Member;
import org.example.mirimilibe.member.repository.MemberRepository;
import org.example.mirimilibe.notification.domain.Notification;
import org.example.mirimilibe.notification.domain.NotificationType;
import org.example.mirimilibe.notification.dto.NotificationCountResponse;
import org.example.mirimilibe.notification.dto.NotificationResponse;
import org.example.mirimilibe.notification.repository.NotificationRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final MemberRepository memberRepository;

    @Transactional
    public void createNotification(Long memberId, NotificationType type, String title, String message, String targetUrl, Long targetId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new MiriMiliException(MemberErrorCode.MEMBER_NOT_FOUND));

        Notification notification = Notification.builder()
                .member(member)
                .type(type)
                .title(title)
                .message(message)
                .targetUrl(targetUrl)
                .targetId(targetId)
                .isRead(false)
                .createdAt(LocalDateTime.now())
                .build();

        notificationRepository.save(notification);
    }

    @Transactional(readOnly = true)
    public CommonPageResponse<NotificationResponse> getNotifications(Long memberId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Notification> notifications = notificationRepository.findByMemberIdOrderByCreatedAtDesc(memberId, pageable);

        Page<NotificationResponse> responsePage = notifications.map(this::convertToResponse);
        return CommonPageResponse.of(responsePage);
    }

    @Transactional(readOnly = true)
    public NotificationCountResponse getUnreadCount(Long memberId) {
        Long count = notificationRepository.countByMemberIdAndIsReadFalse(memberId);
        return NotificationCountResponse.builder()
                .unreadCount(count)
                .build();
    }

    @Transactional
    public void markAsRead(Long notificationId, Long memberId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new RuntimeException("알림을 찾을 수 없습니다."));

        if (!notification.getMember().getId().equals(memberId)) {
            throw new RuntimeException("권한이 없습니다.");
        }

        notification.markAsRead();
    }

    @Transactional
    public void markAllAsRead(Long memberId) {
        Page<Notification> unreadNotifications = notificationRepository.findUnreadByMemberId(memberId, Pageable.unpaged());

        for (Notification notification : unreadNotifications) {
            notification.markAsRead();
        }
    }

    public void createNoticeNotification(Long memberId, String noticeTitle) {
        createNotification(
                memberId,
                NotificationType.NOTICE_NEW,
                "새로운 공지사항이 업로드 되었어요.",
                "클릭하여 " + noticeTitle + "을 확인해보세요.",
                "/notice",
                null
        );
    }

    public void createNewAnswerNotification(Long questionerId, String answererNickname, String questionTitle, Long postId) {
        createNotification(
                questionerId,
                NotificationType.QUESTION_NEW_ANSWER,
                answererNickname + "님이 내 질문에 답변했어요!",
                "클릭하여 내가 작성한 " + questionTitle + "에 대한 새로운 답변 내용을 확인해보세요.",
                "/post/" + postId,
                postId
        );
    }

    public void createAnotherAnswerNotification(Long previousAnswererId, String questionTitle, Long postId) {
        createNotification(
                previousAnswererId,
                NotificationType.QUESTION_ANOTHER_ANSWER,
                "전에 답변했던 질문에 다른 사용자도 답변했어요!",
                "클릭하여 내가 답변한 " + questionTitle + "에 대한 새로운 답변 내용을 확인해보세요.",
                "/post/" + postId,
                postId
        );
    }

    public void createQuestionFirstLikeNotification(Long questionerId, Long postId) {
        createNotification(
                questionerId,
                NotificationType.QUESTION_FIRST_LIKE,
                "누군가가 내 질문을 추천했어요!",
                null,
                "/post/" + postId,
                postId
        );
    }

    public void createQuestionMilestoneLikeNotification(Long questionerId, int likeCount, Long postId) {
        createNotification(
                questionerId,
                NotificationType.QUESTION_MILESTONE_LIKE,
                "내 질문을 " + likeCount + "명이 추천했어요!",
                null,
                "/post/" + postId,
                postId
        );
    }

    public void createAnswerFirstLikeNotification(Long answererId, Long postId) {
        createNotification(
                answererId,
                NotificationType.ANSWER_FIRST_LIKE,
                "누군가가 내 답변을 추천했어요!",
                null,
                "/post/" + postId,
                postId
        );
    }

    public void createAnswerMilestoneLikeNotification(Long answererId, int likeCount, Long postId) {
        createNotification(
                answererId,
                NotificationType.ANSWER_MILESTONE_LIKE,
                "내 답변을 " + likeCount + "명이 추천했어요!",
                null,
                "/post/" + postId,
                postId
        );
    }

    public void createHotQuestionNotification(Long questionerId, String questionTitle, Long postId) {
        createNotification(
                questionerId,
                NotificationType.QUESTION_HOT_SELECTED,
                "내 질문이 HOT 질문으로 선정되었어요!",
                "클릭하여 내가 작성한 " + questionTitle + "을 확인해보세요.",
                "/post/" + postId,
                postId
        );
    }

    public void createBestAnswerNotification(Long answererId, String questionTitle, Long postId) {
        createNotification(
                answererId,
                NotificationType.ANSWER_BEST_SELECTED,
                "내 답변이 베스트 답변으로 선정되었어요!",
                "클릭하여 내가 답변한 " + questionTitle + "을 확인해보세요.",
                "/post/" + postId,
                postId
        );
    }

    public void createDailyHotQuestionNotification(Long memberId) {
        createNotification(
                memberId,
                NotificationType.DAILY_HOT_QUESTION,
                "오늘의 HOT 질문을 확인해보세요!",
                "클릭하여 인기질문과 답변을 확인해보세요.",
                "/questions?sort=likes",
                null
        );
    }

    public void createDailyWaitingAnswerNotification(Long memberId, String memberNickname) {
        createNotification(
                memberId,
                NotificationType.DAILY_WAITING_ANSWER,
                memberNickname + "의 답변을 기다리고 있어요!",
                "클릭하여 답변을 확인하고, " + memberNickname + "님의 지식과 경험을 공유해주세요.",
                "/questions/waiting",
                null
        );
    }

    private NotificationResponse convertToResponse(Notification notification) {
        return NotificationResponse.builder()
                .id(notification.getId())
                .type(notification.getType())
                .title(notification.getTitle())
                .message(notification.getMessage())
                .targetUrl(notification.getTargetUrl())
                .isRead(notification.getIsRead())
                .createdAt(notification.getCreatedAt())
                .build();
    }
}