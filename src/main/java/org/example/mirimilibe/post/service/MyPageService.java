package org.example.mirimilibe.post.service;

import java.util.List;

import org.example.mirimilibe.comment.domain.Comment;
import org.example.mirimilibe.comment.dto.MyAnswerResponse;
import org.example.mirimilibe.comment.repository.CommentRepository;
import org.example.mirimilibe.global.CommonPageResponse;
import org.example.mirimilibe.member.repository.MemberRepository;
import org.example.mirimilibe.post.domain.Post;
import org.example.mirimilibe.post.dto.PostListItemResponse;
import org.example.mirimilibe.post.repository.PostLikeRepository;
import org.example.mirimilibe.post.repository.PostRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MyPageService {

	private final MemberRepository memberRepository;
	private final PostRepository postRepository;
	private final CommentRepository commentRepository;
	private final PostLikeRepository postLikeRepository;

	@Transactional(readOnly = true)
	public List<PostListItemResponse> getMyPosts(Long memberId) {
		List<Post> posts = postRepository.findByWriterId(memberId);
		return posts.stream()
			.map(post -> new PostListItemResponse(
				post.getId(),
				post.getTitle(),
				post.getBody(),
				commentRepository.countByPostId(post.getId()),
				postLikeRepository.countLikesByPostId(post.getId()),
				post.getViewCount(),
				post.getCreatedAt()
			))
			.toList();
	}

	@Transactional(readOnly = true)
	public CommonPageResponse<PostListItemResponse> getMyPosts(Long memberId, int page, int size) {
		PageRequest pageable = PageRequest.of(page, size);
		Page<Post> postsPage = postRepository.findByWriterId(memberId, pageable);

		Page<PostListItemResponse> responsePage = postsPage.map(post -> new PostListItemResponse(
			post.getId(),
			post.getTitle(),
			post.getBody(),
			commentRepository.countByPostId(post.getId()),
			postLikeRepository.countLikesByPostId(post.getId()),
			post.getViewCount(),
			post.getCreatedAt()
		));

		return CommonPageResponse.of(responsePage);
	}

	@Transactional(readOnly = true)
	public List<MyAnswerResponse> getMyAnswers(Long memberId) {
		List<Comment> comments = commentRepository.findAllByWriterId(memberId);
		return comments.stream()
			.map(c -> new MyAnswerResponse(
				c.getPost().getId(),
				c.getPost().getTitle(),
				c.getContent()
			)).toList();
	}

	@Transactional(readOnly = true)
	public CommonPageResponse<MyAnswerResponse> getMyAnswers(Long memberId, int page, int size) {
		PageRequest pageable = PageRequest.of(page, size);
		Page<Comment> commentsPage = commentRepository.findAllByWriterId(memberId, pageable);

		Page<MyAnswerResponse> responsePage = commentsPage.map(c -> new MyAnswerResponse(
			c.getPost().getId(),
			c.getPost().getTitle(),
			c.getContent()
		));

		return CommonPageResponse.of(responsePage);
	}
}