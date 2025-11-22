package org.example.mirimilibe.post.service;

import java.util.Optional;

import org.example.mirimilibe.comment.domain.ReactionType;
import org.example.mirimilibe.global.error.MemberErrorCode;
import org.example.mirimilibe.global.error.PostErrorCode;
import org.example.mirimilibe.global.exception.MiriMiliException;
import org.example.mirimilibe.member.domain.Member;
import org.example.mirimilibe.member.repository.MemberRepository;
import org.example.mirimilibe.notification.service.NotificationService;
import org.example.mirimilibe.post.domain.Post;
import org.example.mirimilibe.post.domain.PostLike;
import org.example.mirimilibe.post.repository.PostLikeRepository;
import org.example.mirimilibe.post.repository.PostRepository;
import org.example.mirimilibe.ranking.service.RankingService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PostLikeService {

	private final PostRepository postRepository;
	private final PostLikeRepository postLikeRepository;
	private final MemberRepository memberRepository;
	private final NotificationService notificationService;
	private final RankingService rankingService;

	@Transactional
	public void reactToPost(Long memberId, Long postId, ReactionType type) {
		Post post = postRepository.findById(postId)
			.orElseThrow(() -> new MiriMiliException(PostErrorCode.POST_NOT_FOUND));

		Member member = memberRepository.findById(memberId)
			.orElseThrow(() -> new MiriMiliException(MemberErrorCode.MEMBER_NOT_FOUND));

		Optional<PostLike> existing = postLikeRepository.findByMemberIdAndPostId(memberId, postId);

		boolean isNewLike = false;
		if (existing.isPresent()) {
			PostLike like = existing.get();
			if (like.getType() == type) {
				// 같은 타입으로 다시 누르면 취소 (삭제)
				postLikeRepository.delete(like);
				// 마지막 활동 시간 업데이트
				rankingService.updateLastActivity(postId);
				return; // 알림 처리 없이 종료
			}
			like.setType(type); // 상태 변경
		} else {
			postLikeRepository.save(PostLike.builder()
				.member(member)
				.post(post)
				.type(type)
				.build());
			isNewLike = true;
		}

		// 좋아요 알림 처리 (질문 작성자가 아닌 경우에만)
		if (type == ReactionType.LIKE && !post.getWriter().getId().equals(memberId)) {
			// 현재 좋아요 수 계산
			Long currentLikeCount = postLikeRepository.countByPostIdAndType(postId, ReactionType.LIKE);

			// 첫 번째 좋아요 알림
			if (isNewLike && currentLikeCount == 1) {
				notificationService.createQuestionFirstLikeNotification(post.getWriter().getId(), postId);
			}
			// 마일스톤 좋아요 알림 (5, 10, 20, 30, 40...)
			else if (isNewLike && currentLikeCount % 10 == 0 ||
					(currentLikeCount == 5 && isNewLike)) {
				notificationService.createQuestionMilestoneLikeNotification(
					post.getWriter().getId(),
					currentLikeCount.intValue(),
					postId
				);
			}
		}

		// 마지막 활동 시간 업데이트
		rankingService.updateLastActivity(postId);
	}
}
