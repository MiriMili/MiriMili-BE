package org.example.mirimilibe.ranking.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.mirimilibe.comment.domain.Comment;
import org.example.mirimilibe.comment.domain.ReactionType;
import org.example.mirimilibe.comment.repository.CommentLikeRepository;
import org.example.mirimilibe.comment.repository.CommentRepository;
import org.example.mirimilibe.global.CommonPageResponse;
import org.example.mirimilibe.member.domain.MilitaryInfo;
import org.example.mirimilibe.member.repository.MilitaryInfoRepository;
import org.example.mirimilibe.notification.service.NotificationService;
import org.example.mirimilibe.post.domain.Post;
import org.example.mirimilibe.post.repository.PostLikeRepository;
import org.example.mirimilibe.post.repository.PostRepository;
import org.example.mirimilibe.post.repository.ScrapedPostRepository;
import org.example.mirimilibe.ranking.dto.BestAnswerResponse;
import org.example.mirimilibe.ranking.dto.HotQuestionResponse;
import org.example.mirimilibe.ranking.dto.PopularContentResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class RankingService {

    private final RankingCalculationService calculationService;
    private final CommentRepository commentRepository;
    private final PostRepository postRepository;
    private final NotificationService notificationService;
    private final CommentLikeRepository commentLikeRepository;
    private final PostLikeRepository postLikeRepository;
    private final ScrapedPostRepository scrapedPostRepository;
    private final MilitaryInfoRepository militaryInfoRepository;

    @Transactional
    public void updateBestAnswersForPost(Long postId) {
        List<Comment> comments = commentRepository.findAllByPostId(postId);

        if (comments.size() <= 1) {
            log.debug("질문 {}에 답변이 1개 이하이므로 베스트 답변을 선정하지 않습니다.", postId);
            return;
        }

        // 기존 베스트 답변 해제
        comments.forEach(Comment::unmarkAsBestAnswer);

        Optional<Comment> bestAnswer = comments.stream()
                .map(comment -> new CommentScore(comment, calculationService.calculateBestAnswerScore(comment.getId())))
                .filter(cs -> cs.score > 0) // 0 이하인 경우 베스트 답변 선정 안함
                .max((a, b) -> {
                    int scoreCompare = Double.compare(a.score, b.score);
                    if (scoreCompare != 0) return scoreCompare;
                    // 동점인 경우 최신 답변 선택
                    return a.comment.getCreatedAt().compareTo(b.comment.getCreatedAt());
                })
                .map(cs -> cs.comment);

        if (bestAnswer.isPresent()) {
            Comment best = bestAnswer.get();
            Comment previousBest = comments.stream()
                    .filter(c -> c.getIsBestAnswer())
                    .findFirst()
                    .orElse(null);

            best.markAsBestAnswer();
            log.info("질문 {}의 베스트 답변으로 답변 {}이 선정되었습니다.", postId, best.getId());

            // 새로운 베스트 답변인 경우 알림 발송
            if (previousBest == null || !previousBest.getId().equals(best.getId())) {
                Post post = postRepository.findById(postId).orElse(null);
                if (post != null) {
                    notificationService.createBestAnswerNotification(
                            best.getWriter().getId(),
                            post.getTitle(),
                            postId
                    );
                }
            }
        }
    }

    @Transactional
    public void updateHotQuestions() {
        List<Post> allPosts = postRepository.findAll();

        // 기존 HOT 질문 해제
        allPosts.forEach(Post::unmarkAsHotQuestion);

        List<Post> hotQuestions = allPosts.stream()
                .map(post -> new PostScore(post, calculationService.calculateHotQuestionScore(
                        post.getId(),
                        post.getLastActivityAt() != null ? post.getLastActivityAt() : post.getCreatedAt()
                )))
                .sorted((a, b) -> Double.compare(b.score, a.score)) // 높은 점수 순
                .limit(3) // 상위 3개
                .map(ps -> ps.post)
                .toList();

        hotQuestions.forEach(post -> {
            boolean wasHot = post.getIsHotQuestion();
            post.markAsHotQuestion();

            // 새로 HOT 질문이 된 경우 알림 발송
            if (!wasHot) {
                notificationService.createHotQuestionNotification(
                        post.getWriter().getId(),
                        post.getTitle(),
                        post.getId()
                );
            }
        });

        log.info("HOT 질문 {}개가 선정되었습니다: {}",
                hotQuestions.size(),
                hotQuestions.stream().map(Post::getId).toList());
    }

    @Transactional
    public void updateLastActivity(Long postId) {
        postRepository.findById(postId).ifPresent(post -> {
            post.updateLastActivity();
            log.debug("질문 {}의 마지막 활동 시간이 업데이트되었습니다.", postId);
        });
    }

    @Transactional(readOnly = true)
    public CommonPageResponse<HotQuestionResponse> getHotQuestions(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Post> hotPosts = postRepository.findHotQuestions(pageable);

        Page<HotQuestionResponse> responsePage = hotPosts.map(post -> {
            Long likeCount = postLikeRepository.countByPostIdAndType(post.getId(), ReactionType.LIKE);
            Long commentCount = commentRepository.countByPostId(post.getId());
            Long scrapCount = scrapedPostRepository.countByPostId(post.getId());
            Double hotScore = calculationService.calculateHotQuestionScore(
                    post.getId(),
                    post.getLastActivityAt() != null ? post.getLastActivityAt() : post.getCreatedAt()
            );

            return HotQuestionResponse.builder()
                    .postId(post.getId())
                    .title(post.getTitle())
                    .body(post.getBody())
                    .writerNickname(post.getWriter().getNickname())
                    .createdAt(post.getCreatedAt())
                    .viewCount(post.getViewCount())
                    .likeCount(likeCount)
                    .commentCount(commentCount)
                    .scrapCount(scrapCount)
                    .hotScore(hotScore)
                    .build();
        });

        return CommonPageResponse.of(responsePage);
    }

    @Transactional(readOnly = true)
    public CommonPageResponse<BestAnswerResponse> getBestAnswers(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Comment> bestComments = commentRepository.findBestAnswers(pageable);

        Page<BestAnswerResponse> responsePage = bestComments.map(comment -> {
            Long likeCount = commentLikeRepository.countByCommentIdAndType(comment.getId(), ReactionType.LIKE);
            Long dislikeCount = commentLikeRepository.countByCommentIdAndType(comment.getId(), ReactionType.DISLIKE);

            Post post = comment.getPost();
            Long specialtyId = militaryInfoRepository.findByMemberId(comment.getWriter().getId())
                    .map(MilitaryInfo::getSpecialty)
                    .map(s -> s.getId())
                    .orElse(null);

            return BestAnswerResponse.builder()
                    .commentId(comment.getId())
                    .content(comment.getContent())
                    .imagesUrl(comment.getImagesUrl())
                    .writerNickname(comment.getWriter().getNickname())
                    .createdAt(comment.getCreatedAt())
                    .likeCount(likeCount)
                    .dislikeCount(dislikeCount)
                    .specialtyId(specialtyId)
                    .postId(post.getId())
                    .postTitle(post.getTitle())
                    .postBody(post.getBody())
                    .postCreatedAt(post.getCreatedAt())
                    .build();
        });

        return CommonPageResponse.of(responsePage);
    }

    @Transactional(readOnly = true)
    public CommonPageResponse<PopularContentResponse> getPopularContent(int page, int size) {
        List<PopularContentResponse> allContent = new ArrayList<>();

        // HOT 질문들 가져오기
        List<Post> hotQuestions = postRepository.findByIsHotQuestionTrueOrderByCreatedAtDesc();
        for (Post post : hotQuestions) {
            Double hotScore = calculationService.calculateHotQuestionScore(
                    post.getId(),
                    post.getLastActivityAt() != null ? post.getLastActivityAt() : post.getCreatedAt()
            );

            PopularContentResponse response = PopularContentResponse.builder()
                    .type("HOT_QUESTION")
                    .score(hotScore)
                    .contentId(post.getId())
                    .displayText(post.getTitle()) // 질문은 제목만
                    .writerNickname(post.getWriter().getNickname())
                    .createdAt(post.getCreatedAt())
                    .build();

            allContent.add(response);
        }

        // 베스트 답변들 가져오기
        List<Comment> bestAnswers = commentRepository.findByIsBestAnswerTrueOrderByCreatedAtDesc();
        for (Comment comment : bestAnswers) {
            Double bestScore = calculationService.calculateBestAnswerScore(comment.getId());
            Post post = comment.getPost();

            // 답변 내용 일부만 (100자 제한)
            String contentPreview = comment.getContent().length() > 100
                ? comment.getContent().substring(0, 100) + "..."
                : comment.getContent();

            PopularContentResponse response = PopularContentResponse.builder()
                    .type("BEST_ANSWER")
                    .score(bestScore)
                    .contentId(comment.getId())
                    .displayText(contentPreview) // 답변은 내용 일부만
                    .writerNickname(comment.getWriter().getNickname())
                    .createdAt(comment.getCreatedAt())
                    .originalPostId(post.getId())
                    .originalPostTitle(post.getTitle())
                    .build();

            allContent.add(response);
        }

        // 점수 순으로 정렬 (높은 점수 먼저)
        allContent.sort((a, b) -> Double.compare(b.getScore(), a.getScore()));

        // 페이징 처리
        int start = page * size;
        int end = Math.min(start + size, allContent.size());
        List<PopularContentResponse> pagedContent = allContent.subList(start, end);

        return CommonPageResponse.of(pagedContent, page, size, allContent.size());
    }

    private static class CommentScore {
        final Comment comment;
        final double score;

        CommentScore(Comment comment, double score) {
            this.comment = comment;
            this.score = score;
        }
    }

    private static class PostScore {
        final Post post;
        final double score;

        PostScore(Post post, double score) {
            this.post = post;
            this.score = score;
        }
    }
}