package org.example.mirimilibe.post.service;

import java.util.List;
import java.util.Optional;

import org.example.mirimilibe.comment.domain.ReactionType;
import org.example.mirimilibe.comment.repository.CommentRepository;
import org.example.mirimilibe.global.error.MemberErrorCode;
import org.example.mirimilibe.global.error.PostErrorCode;
import org.example.mirimilibe.global.exception.MiriMiliException;
import org.example.mirimilibe.member.domain.Member;
import org.example.mirimilibe.member.repository.MemberRepository;
import org.example.mirimilibe.post.domain.Post;
import org.example.mirimilibe.post.domain.ScrapedPost;
import org.example.mirimilibe.post.dto.PostListItemResponse;
import org.example.mirimilibe.post.repository.PostLikeRepository;
import org.example.mirimilibe.post.repository.PostRepository;
import org.example.mirimilibe.post.repository.ScrapedPostRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PostScrapService {

	private final MemberRepository memberRepository;
	private final PostRepository postRepository;
	private final ScrapedPostRepository scrapedPostRepository;
	private final CommentRepository commentRepository;
	private final PostLikeRepository postLikeRepository;

	@Transactional
	public void toggleScrap(Long memberId, Long postId){
		Member member = memberRepository.findById(memberId)
			.orElseThrow(() -> new MiriMiliException(MemberErrorCode.MEMBER_NOT_FOUND));

		Post post = postRepository.findById(postId)
			.orElseThrow(() -> new MiriMiliException(PostErrorCode.POST_NOT_FOUND));

		Optional<ScrapedPost> existing = scrapedPostRepository.findByMemberAndPost(member, post);

		if (existing.isPresent()) {
			scrapedPostRepository.delete(existing.get());
		}else{
			scrapedPostRepository.save(new ScrapedPost(null, member, post));
		}

	}

	@Transactional(readOnly = true)
	public List<PostListItemResponse> getMyScrapPosts(Long memberId) {
		Member member = memberRepository.findById(memberId)
			.orElseThrow(() -> new MiriMiliException(MemberErrorCode.MEMBER_NOT_FOUND));

		List<ScrapedPost> scraps = scrapedPostRepository.findAllByMember(member);

		return scraps.stream()
			.map(s -> {
				Post p = s.getPost();
				return new PostListItemResponse(
					p.getId(),
					p.getTitle(),
					p.getBody(),
					commentRepository.countByPostId(p.getId()),
					postLikeRepository.countByPostIdsAndType(List.of(p.getId()), ReactionType.LIKE).getOrDefault(p.getId(), 0L),
					p.getViewCount(),
					p.getCreatedAt()
				);
			}).toList();
	}

}
