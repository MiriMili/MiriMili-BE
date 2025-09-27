package org.example.mirimilibe.ranking.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PopularContentResponse {
    private String type; // "HOT_QUESTION" 또는 "BEST_ANSWER"
    private Double score; // 점수 (정렬용)
    private Long contentId; // 질문 ID 또는 답변 ID
    private String displayText; // 질문 제목 또는 답변 내용 일부
    private String writerNickname;
    private LocalDateTime createdAt;

    // 답변인 경우 원본 질문 정보
    private Long originalPostId; // 답변인 경우만
    private String originalPostTitle; // 답변인 경우만
}