package org.example.mirimilibe.post.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import org.example.mirimilibe.comment.domain.ReactionType;
import org.example.mirimilibe.comment.repository.CommentRepository;
import org.example.mirimilibe.common.domain.Category;
import org.example.mirimilibe.common.domain.Specialty;
import org.example.mirimilibe.global.CommonPageResponse;
import org.example.mirimilibe.global.error.MemberErrorCode;
import org.example.mirimilibe.global.error.PostErrorCode;
import org.example.mirimilibe.global.exception.MiriMiliException;
import org.example.mirimilibe.member.domain.Member;
import org.example.mirimilibe.member.domain.MilitaryInfo;
import org.example.mirimilibe.member.repository.MemberRepository;
import org.example.mirimilibe.member.repository.MilitaryInfoRepository;
import org.example.mirimilibe.post.domain.Post;
import org.example.mirimilibe.post.domain.PostCategory;
import org.example.mirimilibe.post.domain.PostSpecialty;
import org.example.mirimilibe.post.dto.PostCreateRequest;
import org.example.mirimilibe.post.dto.PostDetailResponse;
import org.example.mirimilibe.post.dto.PostListItemResponse;
import org.example.mirimilibe.post.repository.CategoryRepository;
import org.example.mirimilibe.post.repository.PostCategoryRepository;
import org.example.mirimilibe.post.repository.PostLikeRepository;
import org.example.mirimilibe.post.repository.PostRepository;
import org.example.mirimilibe.post.repository.PostSpecialtyRepository;
import org.example.mirimilibe.post.repository.SpecialtyRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PostService {

	private final PostRepository postRepository;
	private final MemberRepository memberRepository;
	private final CategoryRepository categoryRepository;
	private final SpecialtyRepository specialtyRepository;
	private final PostCategoryRepository postCategoryRepository;
	private final PostSpecialtyRepository postSpecialtyRepository;
	private final CommentRepository commentRepository;
	private final MilitaryInfoRepository militaryInfoRepository;
	private final PostLikeRepository postLikeRepository;

	@Transactional
	public Long createPost(Long memberId, PostCreateRequest req) {
		Member writer = memberRepository.findById(memberId)
			.orElseThrow(() -> new IllegalArgumentException("작성자 없음"));

		Post post = Post.builder()
			.writer(writer)
			.title(req.title())
			.body(req.body())
			.imagesUrl(req.imagesUrl())
			.viewCount(0L)
			.targetMiliType(req.targetMiliType())
			.createdAt(LocalDateTime.now())
			.build();

		postRepository.save(post);

		// 카테고리 연결
		req.categoryIds().forEach(categoryId -> {
			Category category = categoryRepository.findById(categoryId)
				.orElseThrow(() -> new IllegalArgumentException("카테고리 없음"));
			postCategoryRepository.save(new PostCategory(null, post, category));
		});

		// 특기 연결
		req.specialtyIds().forEach(specialtyId -> {
			Specialty specialty = specialtyRepository.findById(specialtyId)
				.orElseThrow(() -> new IllegalArgumentException("특기 없음"));
			postSpecialtyRepository.save(new PostSpecialty(null, post, specialty));
		});

		return post.getId();
	}


	@Transactional
	public PostDetailResponse getPostDetail(Long postId) {
		Post post = postRepository.findById(postId)
			.orElseThrow(() -> new MiriMiliException(PostErrorCode.POST_NOT_FOUND));

		// 조회수 증가
		post.increaseViewCount();

		Member writer = post.getWriter();
		MilitaryInfo militaryInfo = militaryInfoRepository.findByMemberId(writer.getId())
			.orElseThrow(() -> new MiriMiliException(MemberErrorCode.MILITARY_INFO_NOT_FOUND));

		// 카테고리 이름 추출
		List<String> categoryNames = postCategoryRepository.findAllByPost(post).stream()
			.map(pc -> pc.getCategory().getValue())
			.toList();

		// 대상 특기명 추출
		List<String> targetSpecialtyNames = postSpecialtyRepository.findAllByPost(post).stream()
			.map(ps -> ps.getSpecialty().getValue())
			.toList();

		// 댓글 수
		Long commentCount = commentRepository.countByPostId(postId);

		return new PostDetailResponse(
			post.getId(),
			post.getTitle(),
			post.getBody(),
			writer.getNickname(),
			militaryInfo.getSpecialty().getValue(),
			militaryInfo.getMiliStatus(),
			post.getCreatedAt(),
			post.getImagesUrl(),
			categoryNames,
			post.getTargetMiliType(),
			targetSpecialtyNames,
			commentCount,
			post.getViewCount()
		);
	}

	@Transactional(readOnly = true)
	public CommonPageResponse<PostListItemResponse> getPostsByCategory(List<Long> categoryIds, Pageable pageable) {
		Page<Post> postPage = postRepository.findDistinctByCategories(categoryIds, pageable);

		List<Long> postIds = postPage.map(Post::getId).toList();

		Map<Long, Long> commentCounts = commentRepository.countByPostIds(postIds);
		Map<Long, Long> likeCounts = postLikeRepository.countByPostIdsAndType(postIds, ReactionType.LIKE);

		Page<PostListItemResponse> dtoPage = postPage.map(post -> new PostListItemResponse(
			post.getId(),
			post.getTitle(),
			post.getBody(),
			commentCounts.getOrDefault(post.getId(), 0L),
			likeCounts.getOrDefault(post.getId(), 0L),
			post.getViewCount(),
			post.getCreatedAt()
		));

		return CommonPageResponse.of(dtoPage);
	}


}
