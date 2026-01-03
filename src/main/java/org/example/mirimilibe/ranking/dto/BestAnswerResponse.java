package org.example.mirimilibe.ranking.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BestAnswerResponse {
    private Long commentId;
    private String content;
    private List<String> imagesUrl;
    private String writerNickname;
    private LocalDateTime createdAt;
    private Long likeCount;
    private Long dislikeCount;
    private Long specialtyId;

    // 질문 정보
    private Long postId;
    private String postTitle;
    private String postBody;
    private LocalDateTime postCreatedAt;
}