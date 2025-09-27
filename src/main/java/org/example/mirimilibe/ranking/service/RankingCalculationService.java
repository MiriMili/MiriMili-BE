package org.example.mirimilibe.ranking.service;

import lombok.RequiredArgsConstructor;
import org.example.mirimilibe.comment.domain.ReactionType;
import org.example.mirimilibe.comment.repository.CommentLikeRepository;
import org.example.mirimilibe.comment.repository.CommentRepository;
import org.example.mirimilibe.post.repository.PostLikeRepository;
import org.example.mirimilibe.post.repository.PostRepository;
import org.example.mirimilibe.post.repository.ScrapedPostRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Service
@RequiredArgsConstructor
public class RankingCalculationService {

    private final CommentLikeRepository commentLikeRepository;
    private final PostLikeRepository postLikeRepository;
    private final CommentRepository commentRepository;
    private final ScrapedPostRepository scrapedPostRepository;
    private final PostRepository postRepository;

    public double calculateBestAnswerScore(Long commentId) {
        Long likeCount = commentLikeRepository.countByCommentIdAndType(commentId, ReactionType.LIKE);
        Long dislikeCount = commentLikeRepository.countByCommentIdAndType(commentId, ReactionType.DISLIKE);

        return (likeCount * 2.0) + (dislikeCount * -1.0);
    }

    public double calculateHotQuestionScore(Long postId, LocalDateTime lastActivityAt) {
        Long questionLikes = postLikeRepository.countByPostIdAndType(postId, ReactionType.LIKE);
        Long questionDislikes = postLikeRepository.countByPostIdAndType(postId, ReactionType.DISLIKE);
        Long viewCount = getViewCount(postId);
        Long answerCount = commentRepository.countByPostId(postId);
        Long scrapCount = scrapedPostRepository.countByPostId(postId);

        double score = (questionLikes * 5.0) +
                      (questionDislikes * -2.0) +
                      (viewCount * 0.1) +
                      (answerCount * 4.0) +
                      (scrapCount * 5.0);

        double timeWeight = calculateTimeWeight(lastActivityAt != null ? lastActivityAt : LocalDateTime.now());

        return score / timeWeight;
    }

    public double calculateWaitingAnswerScore(Long postId, LocalDateTime createdAt) {
        Long viewCount = getViewCount(postId);
        Long questionLikes = postLikeRepository.countByPostIdAndType(postId, ReactionType.LIKE);
        Long scrapCount = scrapedPostRepository.countByPostId(postId);
        Long answerCount = commentRepository.countByPostId(postId);

        double freshnessBonus = calculateFreshnessBonus(createdAt);

        return (viewCount * 0.5) +
               (questionLikes * 3.0) +
               (scrapCount * 5.0) +
               (answerCount * -100.0) +
               (freshnessBonus * 50.0);
    }

    private double calculateTimeWeight(LocalDateTime lastActivity) {
        long hoursElapsed = ChronoUnit.HOURS.between(lastActivity, LocalDateTime.now());
        return Math.pow(hoursElapsed + 1, 0.5);
    }

    private double calculateFreshnessBonus(LocalDateTime createdAt) {
        long daysElapsed = ChronoUnit.DAYS.between(createdAt, LocalDateTime.now());
        return 50.0 / (daysElapsed + 1);
    }

    private Long getViewCount(Long postId) {
        return postRepository.findById(postId)
                .map(post -> post.getViewCount())
                .orElse(0L);
    }
}