package org.example.mirimilibe.notification.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.example.mirimilibe.global.ApiResponse;
import org.example.mirimilibe.global.CommonPageResponse;
import org.example.mirimilibe.global.auth.dto.JwtMemberDetail;
import org.example.mirimilibe.global.success.CommonSuccessCode;
import org.example.mirimilibe.notification.dto.NotificationCountResponse;
import org.example.mirimilibe.notification.dto.NotificationResponse;
import org.example.mirimilibe.notification.service.NotificationService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/notifications")
@RequiredArgsConstructor
@Tag(name = "알림", description = "알림 관련 API")
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping
    @Operation(summary = "알림 목록 조회", description = "사용자의 알림 목록을 페이지네이션으로 조회합니다.")
    public ApiResponse<CommonPageResponse<NotificationResponse>> getNotifications(
            @AuthenticationPrincipal JwtMemberDetail memberDetail,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        CommonPageResponse<NotificationResponse> notifications =
                notificationService.getNotifications(memberDetail.getMemberId(), page, size);

        return ApiResponse.success(notifications);
    }

    @GetMapping("/count")
    @Operation(summary = "읽지 않은 알림 개수 조회", description = "사용자의 읽지 않은 알림 개수를 조회합니다.")
    public ApiResponse<NotificationCountResponse> getUnreadCount(
            @AuthenticationPrincipal JwtMemberDetail memberDetail) {

        NotificationCountResponse count = notificationService.getUnreadCount(memberDetail.getMemberId());
        return ApiResponse.success(count);
    }

    @PatchMapping("/{notificationId}/read")
    @Operation(summary = "알림 읽음 처리", description = "특정 알림을 읽음 처리합니다.")
    public ApiResponse<Void> markAsRead(
            @PathVariable Long notificationId,
            @AuthenticationPrincipal JwtMemberDetail memberDetail) {

        notificationService.markAsRead(notificationId, memberDetail.getMemberId());
        return ApiResponse.successWithNoData();
    }

    @PatchMapping("/read-all")
    @Operation(summary = "모든 알림 읽음 처리", description = "사용자의 모든 알림을 읽음 처리합니다.")
    public ApiResponse<Void> markAllAsRead(
            @AuthenticationPrincipal JwtMemberDetail memberDetail) {

        notificationService.markAllAsRead(memberDetail.getMemberId());
        return ApiResponse.successWithNoData();
    }
}