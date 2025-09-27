package org.example.mirimilibe.notification.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.mirimilibe.notification.domain.NotificationType;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationCreateRequest {
    private Long memberId;
    private NotificationType type;
    private String title;
    private String message;
    private String targetUrl;
    private Long targetId;
}