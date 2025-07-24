package org.example.mirimilibe.post.service;

import java.util.Optional;

import org.example.mirimilibe.comment.domain.ReactionType;
import org.example.mirimilibe.global.error.MemberErrorCode;
import org.example.mirimilibe.global.error.PostErrorCode;
import org.example.mirimilibe.global.exception.MiriMiliException;
import org.example.mirimilibe.member.domain.Member;
import org.example.mirimilibe.member.repository.MemberRepository;
import org.example.mirimilibe.post.domain.Post;
import org.example.mirimilibe.post.domain.PostLike;
import org.example.mirimilibe.post.repository.PostLikeRepository;
import org.example.mirimilibe.post.repository.PostRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PostLikeService {

	private final PostRepository postRepository;
	private final PostLikeRepository postLikeRepository;
	private final MemberRepository memberRepository;

	@Transactional
	public void reactToPost(Long memberId, Long postId, ReactionType type) {
		Post post = postRepository.findById(postId)
			.orElseThrow(() -> new MiriMiliException(PostErrorCode.POST_NOT_FOUND));

		Member member = memberRepository.findById(memberId)
			.orElseThrow(() -> new MiriMiliException(MemberErrorCode.MEMBER_NOT_FOUND));

		Optional<PostLike> existing = postLikeRepository.findByMemberIdAndPostId(memberId, postId);

		if (existing.isPresent()) {
			PostLike like = existing.get();
			if (like.getType() == type) {
				throw new MiriMiliException(PostErrorCode.ALREADY_REACTED);
			}
			like.setType(type); // 상태 변경
		} else {
			postLikeRepository.save(PostLike.builder()
				.member(member)
				.post(post)
				.type(type)
				.build());
		}
	}
}
