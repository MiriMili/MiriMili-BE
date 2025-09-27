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
public class HotQuestionResponse {
    private Long postId;
    private String title;
    private String body;
    private String writerNickname;
    private LocalDateTime createdAt;
    private Long viewCount;
    private Long likeCount;
    private Long commentCount;
    private Long scrapCount;
    private Double hotScore;
}