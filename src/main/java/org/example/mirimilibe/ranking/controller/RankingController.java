package org.example.mirimilibe.ranking.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.example.mirimilibe.global.ApiResponse;
import org.example.mirimilibe.global.CommonPageResponse;
import org.example.mirimilibe.ranking.dto.BestAnswerResponse;
import org.example.mirimilibe.ranking.dto.HotQuestionResponse;
import org.example.mirimilibe.ranking.dto.PopularContentResponse;
import org.example.mirimilibe.ranking.service.RankingService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/ranking")
@RequiredArgsConstructor
@Tag(name = "랭킹", description = "HOT 질문 및 베스트 답변 관련 API")
public class RankingController {

    private final RankingService rankingService;

    @GetMapping("/hot-questions")
    @Operation(
        summary = "HOT 질문 목록 조회",
        description = """
            **🔥 HOT 질문으로 선정된 게시물들을 페이지네이션으로 조회합니다.**

            ## 📊 HOT 질문 선정 기준
            - **HOT 질문 지수**: `{(좋아요×5) + (싫어요×-2) + (조회수×0.1) + (답변수×4) + (스크랩×5)} / (경과시간+1)^0.5`
            - **선정 개수**: 전체 게시물 중 상위 3개만 HOT 질문으로 선정
            - **시간 가중치**: 시간이 지날수록 점수가 감소

            ## 📋 응답 필드
            - `postId`: 게시물 ID
            - `title`: 게시물 제목
            - `body`: 게시물 본문
            - `writerNickname`: 작성자 닉네임
            - `viewCount`: 조회수
            - `likeCount`: 좋아요 수
            - `commentCount`: 답변 수
            - `scrapCount`: 스크랩 수
            - `hotScore`: HOT 질문 지수
            - `createdAt`: 작성시간
            """
    )
    public ApiResponse<CommonPageResponse<HotQuestionResponse>> getHotQuestions(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        CommonPageResponse<HotQuestionResponse> hotQuestions = rankingService.getHotQuestions(page, size);
        return ApiResponse.success(hotQuestions);
    }

    @GetMapping("/best-answers")
    @Operation(
        summary = "베스트 답변 목록 조회",
        description = """
            **⭐ 베스트 답변으로 선정된 댓글들을 페이지네이션으로 조회합니다.**

            ## 🎯 베스트 답변 선정 기준
            - **베스트 답변 지수**: `(답변 좋아요×2) + (답변 싫어요×-1)`
            - **선정 조건**: 게시물에 답변이 2개 이상 있고, 지수가 0보다 큰 경우
            - **선정 방식**: 각 게시물당 최고 점수 답변 1개 선정
            - **동점 처리**: 점수가 같으면 최신 답변 우선

            ## 📋 응답 필드
            - `commentId`: 답변 ID
            - `content`: 답변 내용
            - `imagesUrl`: 답변에 첨부된 이미지 URL 목록
            - `writerNickname`: 답변 작성자 닉네임
            - `likeCount`: 답변 좋아요 수
            - `dislikeCount`: 답변 싫어요 수
            - `specialty`: 답변 작성자 특기
            - `createdAt`: 답변 작성시간

            ## 📝 원본 게시물 정보
            - `postId`: 원본 게시물 ID
            - `postTitle`: 원본 게시물 제목
            - `postBody`: 원본 게시물 본문
            - `postCreatedAt`: 원본 게시물 작성시간
            """
    )
    public ApiResponse<CommonPageResponse<BestAnswerResponse>> getBestAnswers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        CommonPageResponse<BestAnswerResponse> bestAnswers = rankingService.getBestAnswers(page, size);
        return ApiResponse.success(bestAnswers);
    }

    @GetMapping("/popular")
    @Operation(
        summary = "인기 컨텐츠 목록 조회",
        description = """
            **🏆 HOT 질문(게시물)과 베스트 답변(댓글)을 점수 순으로 통합하여 조회합니다.**

            ## 🎯 통합 정렬 방식
            - **HOT 질문 지수**와 **베스트 답변 지수**를 동일한 기준으로 정렬
            - 높은 점수 순으로 HOT 질문과 베스트 답변이 섞여서 나타남
            - 한 번의 API 호출로 인기 있는 모든 컨텐츠를 확인 가능

            ## 📋 응답 필드 (공통)
            - `type`: "HOT_QUESTION" 또는 "BEST_ANSWER"
            - `score`: 계산된 점수 (정렬 기준)
            - `contentId`: 게시물 ID 또는 답변 ID
            - `displayText`: 표시할 텍스트
            - `writerNickname`: 작성자 닉네임
            - `createdAt`: 작성시간

            ## 📝 type별 displayText 내용
            - **HOT_QUESTION**: 게시물 제목 전체
            - **BEST_ANSWER**: 답변 내용 일부 (100자 + "...")

            ## 🔗 답변인 경우 추가 정보
            - `originalPostId`: 원본 게시물 ID
            - `originalPostTitle`: 원본 게시물 제목

            ## 💡 사용 예시
            ```
            GET /ranking/popular?page=0&size=20
            ```

            ## 📱 프론트엔드 활용
            - `type`으로 게시물/답변 구분하여 다른 UI 렌더링
            - `contentId`로 상세 페이지 이동
            - `originalPostId`로 답변의 원본 게시물 이동
            """
    )
    public ApiResponse<CommonPageResponse<PopularContentResponse>> getPopularContent(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        CommonPageResponse<PopularContentResponse> popularContent = rankingService.getPopularContent(page, size);
        return ApiResponse.success(popularContent);
    }
}