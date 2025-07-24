package org.example.mirimilibe.comment.service;

import java.nio.file.AccessDeniedException;
import java.util.List;
import java.util.Map;

import org.example.mirimilibe.comment.domain.Comment;
import org.example.mirimilibe.comment.domain.ReactionType;
import org.example.mirimilibe.comment.dto.CommentCreateRequest;
import org.example.mirimilibe.comment.repository.CommentLikeRepository;
import org.example.mirimilibe.comment.repository.CommentRepository;
import org.example.mirimilibe.comment.repository.CommentSummaryResponse;
import org.example.mirimilibe.common.domain.Specialty;
import org.example.mirimilibe.global.error.CommentErrorCode;
import org.example.mirimilibe.global.error.MemberErrorCode;
import org.example.mirimilibe.global.error.PostErrorCode;
import org.example.mirimilibe.global.exception.MiriMiliException;
import org.example.mirimilibe.member.domain.Member;
import org.example.mirimilibe.member.domain.MilitaryInfo;
import org.example.mirimilibe.member.repository.MemberRepository;
import org.example.mirimilibe.member.repository.MilitaryInfoRepository;
import org.example.mirimilibe.post.domain.Post;
import org.example.mirimilibe.post.domain.PostSpecialty;
import org.example.mirimilibe.post.repository.PostRepository;
import org.example.mirimilibe.post.repository.PostSpecialtyRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CommentService {

	private final CommentRepository commentRepository;
	private final PostRepository postRepository;
	private final PostSpecialtyRepository postSpecialtyRepository;
	private final MilitaryInfoRepository militaryInfoRepository;
	private final MemberRepository memberRepository;
	private final CommentLikeRepository commentLikeRepository;
	@Transactional
	public Long createComment(Long memberId, Long postId, CommentCreateRequest req)  {
		Post post = postRepository.findById(postId)
			.orElseThrow(() -> new MiriMiliException(PostErrorCode.POST_NOT_FOUND));

		Member writer = memberRepository.findById(memberId)
			.orElseThrow(() -> new MiriMiliException(MemberErrorCode.MEMBER_NOT_FOUND));

		MilitaryInfo info = militaryInfoRepository.findByMemberId(memberId)
			.orElseThrow(() -> new MiriMiliException(MemberErrorCode.MILITARY_INFO_NOT_FOUND));


		// 1. 군구분 매칭
		boolean matchedMili = post.getTargetMiliType() == null || post.getTargetMiliType() == info.getMiliType();

		// 2. 특기 매칭
		List<Specialty> allowedSpecialties = postSpecialtyRepository.findAllByPostId(postId)
			.stream().map(PostSpecialty::getSpecialty).toList();

		boolean matchedSpecialty = allowedSpecialties.isEmpty() ||
			allowedSpecialties.contains(info.getSpecialty());

		if (!matchedMili && !matchedSpecialty) {
			throw new MiriMiliException(CommentErrorCode.NO_PERMISSION_TO_COMMENT);
		}

		// 답글 저장
		Comment comment = Comment.builder()
			.post(post)
			.writer(writer)
			.content(req.body())
			.imagesUrl(req.imagesUrl())
			.build();

		commentRepository.save(comment);
		return comment.getId();
	}

	@Transactional(readOnly = true)
	public List<CommentSummaryResponse> getCommentsByPost(Long postId) {
		Post post = postRepository.findById(postId)
			.orElseThrow(() -> new MiriMiliException(PostErrorCode.POST_NOT_FOUND));

		List<PostSpecialty> targetSpecialties = postSpecialtyRepository.findAllByPost(post);
		List<Specialty> allowedSpecialties = targetSpecialties.stream()
			.map(PostSpecialty::getSpecialty)
			.toList();

		List<Comment> comments = commentRepository.findAllByPost(post);

		// 좋아요/싫어요 집계용 map<commentId, like/dislike>
		Map<Long, Long> likeMap = commentLikeRepository.countByCommentIdsAndType(
			comments.stream().map(Comment::getId).toList(), ReactionType.LIKE);
		Map<Long, Long> dislikeMap = commentLikeRepository.countByCommentIdsAndType(
			comments.stream().map(Comment::getId).toList(), ReactionType.DISLIKE);

		List<CommentSummaryResponse> responseList = comments.stream()
			.map(comment -> {
				Member writer = comment.getWriter();
				MilitaryInfo info = militaryInfoRepository.findByMemberId(writer.getId())
					.orElse(null); // 일부 사용자는 군정보 없을 수 있음

				boolean matchesTarget = false;
				if (info != null) {
					boolean matchedMili = post.getTargetMiliType() == null || post.getTargetMiliType() == info.getMiliType();
					boolean matchedSpecialty = allowedSpecialties.isEmpty() || allowedSpecialties.contains(info.getSpecialty());
					matchesTarget = matchedMili || matchedSpecialty;
				}

				return new CommentSummaryResponse(
					comment.getId(),
					comment.getContent(),
					comment.getImagesUrl(),
					writer.getNickname(),
					comment.getCreatedAt(),
					likeMap.getOrDefault(comment.getId(), 0L),
					dislikeMap.getOrDefault(comment.getId(), 0L),
					matchesTarget,
					info != null ? info.getSpecialty().getValue() : null,
					info != null ? info.getMiliType() : null,
					info != null ? info.getMiliStatus() : null
				);
			})
			.sorted((a, b) -> {
				// 좋아요가 제일 많은 댓글 하나를 맨 위로
				long likeDiff = b.likeCount() - a.likeCount();
				if (likeDiff != 0) return (int) likeDiff;
				// 같은 좋아요 수이면 작성일 순
				return a.createdAt().compareTo(b.createdAt());
			})
			.toList();

		return responseList;
	}

}

