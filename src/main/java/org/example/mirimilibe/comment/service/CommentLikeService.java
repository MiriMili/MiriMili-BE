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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CommentLikeService {

	private final CommentLikeRepository commentLikeRepository;
	private final CommentRepository commentRepository;
	private final MemberRepository memberRepository;

	@Transactional
	public void reactToComment(Long memberId, Long commentId, ReactionType type) {
		Comment comment = commentRepository.findById(commentId)
			.orElseThrow(() -> new MiriMiliException(CommentErrorCode.COMMENT_NOT_FOUND));

		Member member = memberRepository.findById(memberId)
			.orElseThrow(() -> new MiriMiliException(MemberErrorCode.MEMBER_NOT_FOUND));

		Optional<CommentLike> existing = commentLikeRepository.findByMemberIdAndCommentId(memberId, commentId);

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
		}
	}
}
