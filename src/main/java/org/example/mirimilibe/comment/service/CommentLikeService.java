package org.example.mirimilibe.comment.service;

import java.util.Optional;

import org.example.mirimilibe.comment.domain.Comment;
import org.example.mirimilibe.comment.domain.CommentLike;
import org.example.mirimilibe.comment.domain.ReactionType;
import org.example.mirimilibe.comment.repository.CommentLikeRepository;
import org.example.mirimilibe.comment.repository.CommentRepository;
import org.example.mirimilibe.global.error.CommentErrorCode;
import org.example.mirimilibe.global.error.MemberErrorCode;
import org.example.mirimilibe.global.exception.MiriMiliException;
import org.example.mirimilibe.member.domain.Member;
import org.example.mirimilibe.member.repository.MemberRepository;
import org.example.mirimilibe.notification.service.NotificationService;
import org.example.mirimilibe.ranking.service.RankingService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CommentLikeService {

	private final CommentLikeRepository commentLikeRepository;
	private final CommentRepository commentRepository;
	private final MemberRepository memberRepository;
	private final NotificationService notificationService;
	private final RankingService rankingService;

	@Transactional
	public void reactToComment(Long memberId, Long commentId, ReactionType type) {
		Comment comment = commentRepository.findById(commentId)
			.orElseThrow(() -> new MiriMiliException(CommentErrorCode.COMMENT_NOT_FOUND));

		Member member = memberRepository.findById(memberId)
			.orElseThrow(() -> new MiriMiliException(MemberErrorCode.MEMBER_NOT_FOUND));

		Optional<CommentLike> existing = commentLikeRepository.findByMemberIdAndCommentId(memberId, commentId);

		boolean isNewLike = false;
		if (existing.isPresent()) {
			CommentLike like = existing.get();
			if (like.getType() == type) {
				// 같은 타입으로 이미 눌렀음 → 무시 or 에러
				throw new MiriMiliException(CommentErrorCode.ALREADY_REACTED);
			} else {
				// 다른 타입으로 변경
				like.setType(type);
			}
		} else {
			// 최초 좋아요
			CommentLike newLike = CommentLike.builder()
				.member(member)
				.comment(comment)
				.type(type)
				.build();
			commentLikeRepository.save(newLike);
			isNewLike = true;
		}

		// 답변 좋아요 알림 처리 (답변 작성자가 아닌 경우에만)
		if (type == ReactionType.LIKE && !comment.getWriter().getId().equals(memberId)) {
			// 현재 좋아요 수 계산
			Long currentLikeCount = commentLikeRepository.countByCommentIdAndType(commentId, ReactionType.LIKE);

			// 첫 번째 좋아요 알림
			if (isNewLike && currentLikeCount == 1) {
				notificationService.createAnswerFirstLikeNotification(comment.getWriter().getId(), comment.getPost().getId());
			}
			// 마일스톤 좋아요 알림 (5, 10, 20, 30, 40...)
			else if (isNewLike && (currentLikeCount % 10 == 0 || currentLikeCount == 5)) {
				notificationService.createAnswerMilestoneLikeNotification(
					comment.getWriter().getId(),
					currentLikeCount.intValue(),
					comment.getPost().getId()
				);
			}
		}

		// 마지막 활동 시간 업데이트 및 베스트 답변 재계산
		Long postId = comment.getPost().getId();
		rankingService.updateLastActivity(postId);
		rankingService.updateBestAnswersForPost(postId);
	}
}
