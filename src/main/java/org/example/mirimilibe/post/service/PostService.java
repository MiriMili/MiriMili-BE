package org.example.mirimilibe.post.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.example.mirimilibe.comment.domain.ReactionType;
import org.example.mirimilibe.comment.repository.CommentRepository;
import org.example.mirimilibe.common.domain.Category;
import org.example.mirimilibe.common.domain.Specialty;
import org.example.mirimilibe.global.CommonPageResponse;
import org.example.mirimilibe.global.auth.service.S3UploadService;
import org.example.mirimilibe.global.error.MemberErrorCode;
import org.example.mirimilibe.global.error.MilitaryInfoErrorCode;
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
import org.example.mirimilibe.post.repository.ScrapedPostRepository;
import org.example.mirimilibe.post.repository.SpecialtyRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

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
	private final ScrapedPostRepository scrapedPostRepository;
	private final S3UploadService s3UploadService;

	@Transactional
	public Long createPost(Long memberId, PostCreateRequest req) {
		Member writer = memberRepository.findById(memberId)
			.orElseThrow(() -> new MiriMiliException(MemberErrorCode.MEMBER_NOT_FOUND));

		List<String> safeKeys = Optional.ofNullable(req.imageKeys()).orElse(List.of());
		if (safeKeys.size() > 5) {
			throw new IllegalArgumentException("이미지는 최대 5장까지만 업로드할 수 있습니다.");
		}

		Post post = Post.builder()
			.writer(writer)
			.title(req.title())
			.body(req.body())
			.imageKeys(safeKeys)
			.viewCount(0L)
			.targetMiliType(req.targetMiliType())
			.createdAt(LocalDateTime.now())
			.build();

		postRepository.save(post);

		var categoryIds   = Optional.ofNullable(req.categoryIds()).orElse(List.of());
		var specialtyIds  = Optional.ofNullable(req.specialtyIds()).orElse(List.of());

		var categories = categoryRepository.findAllById(categoryIds);
		var specialties = specialtyRepository.findAllById(specialtyIds);

		postCategoryRepository.saveAll(
			categories.stream()
				.map(c -> new PostCategory(null, post, c))
				.toList()
		);

		postSpecialtyRepository.saveAll(
			specialties.stream()
				.map(s -> new PostSpecialty(null, post, s))
				.toList()
		);

		return post.getId();
	}

	@Transactional
	public PostDetailResponse getPostDetail(Long postId) {
		// Fetch Join으로 writer까지 한 번에 조회
		Post post = postRepository.findByIdWithWriter(postId)
			.orElseThrow(() -> new MiriMiliException(PostErrorCode.POST_NOT_FOUND));

		// 조회수 증가
		post.increaseViewCount();

		Member writer = post.getWriter();
		// Fetch Join으로 specialty, unit까지 한 번에 조회
		MilitaryInfo militaryInfo = militaryInfoRepository.findByMemberIdWithSpecialty(writer.getId())
			.orElseThrow(() -> new MiriMiliException(MilitaryInfoErrorCode.MILITARY_INFO_NOT_FOUND));

		// Fetch Join으로 category까지 한 번에 조회
		List<String> categoryNames = postCategoryRepository.findAllByPostWithCategory(post).stream()
			.map(pc -> pc.getCategory().getValue())
			.toList();

		// Fetch Join으로 specialty까지 한 번에 조회
		List<String> targetSpecialtyNames = postSpecialtyRepository.findAllByPostWithSpecialty(post).stream()
			.map(ps -> ps.getSpecialty().getValue())
			.toList();

		// 댓글 수
		Long commentCount = commentRepository.countByPostId(postId);

		List<String> imageKeys = post.getImageKeys();
		List<String> imageUrls = imageKeys == null || imageKeys.isEmpty()
			? List.of()
			: imageKeys.stream().map(s3UploadService::presignGet).toList();

		return new PostDetailResponse(
			post.getId(),
			post.getTitle(),
			post.getBody(),
			writer.getNickname(),
			militaryInfo.getSpecialty() != null ? militaryInfo.getSpecialty().getValue() : null,
			militaryInfo.getMiliStatus(),
			post.getCreatedAt(),
			imageUrls,
			categoryNames,
			post.getTargetMiliType(),
			targetSpecialtyNames,
			commentCount,
			post.getViewCount()
		);
	}

	@Transactional(readOnly = true)
	public CommonPageResponse<PostListItemResponse> getPostsByCategory(List<Long> categoryIds, int page, int size, String sortBy) {
		List<Post> allPosts = postRepository.findDistinctByCategories(categoryIds);

		// HOT 질문 지수로 정렬하는 경우
		if ("hotScore".equals(sortBy)) {
			// HOT 질문 지수 계산하여 정렬
			allPosts = allPosts.stream()
				.sorted((a, b) -> {
					double scoreA = calculateHotQuestionScore(a.getId(),
						a.getLastActivityAt() != null ? a.getLastActivityAt() : a.getCreatedAt());
					double scoreB = calculateHotQuestionScore(b.getId(),
						b.getLastActivityAt() != null ? b.getLastActivityAt() : b.getCreatedAt());
					return Double.compare(scoreB, scoreA); // 내림차순
				})
				.toList();
		} else {
			// 기본 정렬 (생성일시 내림차순)
			allPosts = allPosts.stream()
				.sorted((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()))
				.toList();
		}

		// 페이징 처리
		int start = page * size;
		int end = Math.min(start + size, allPosts.size());
		List<Post> pagedPosts = allPosts.subList(start, end);

		List<Long> postIds = pagedPosts.stream().map(Post::getId).toList();
		Map<Long, Long> commentCounts = commentRepository.countByPostIds(postIds);
		Map<Long, Long> likeCounts = postLikeRepository.countByPostIdsAndType(postIds, ReactionType.LIKE);

		List<PostListItemResponse> dtoList = pagedPosts.stream()
			.map(post -> new PostListItemResponse(
				post.getId(),
				post.getTitle(),
				post.getBody(),
				commentCounts.getOrDefault(post.getId(), 0L),
				likeCounts.getOrDefault(post.getId(), 0L),
				post.getViewCount(),
				post.getCreatedAt()
			))
			.toList();

		return CommonPageResponse.of(dtoList, page, size, allPosts.size());
	}

	private double calculateHotQuestionScore(Long postId, LocalDateTime lastActivityAt) {
		// RankingCalculationService와 동일한 로직 사용
		Long likes = postLikeRepository.countByPostIdAndType(postId, ReactionType.LIKE);
		Long dislikes = postLikeRepository.countByPostIdAndType(postId, ReactionType.DISLIKE);
		Long views = postRepository.findById(postId).map(Post::getViewCount).orElse(0L);
		Long answers = commentRepository.countByPostId(postId);
		Long scraps = scrapedPostRepository.countByPostId(postId);

		double baseScore = (likes * 5.0) + (dislikes * -2.0) + (views * 0.1) + (answers * 4.0) + (scraps * 5.0);

		long hoursElapsed = java.time.Duration.between(lastActivityAt, LocalDateTime.now()).toHours();
		double timeDecay = Math.pow(hoursElapsed + 1, 0.5);

		return baseScore / timeDecay;
	}

	@Transactional(readOnly = true)
	public CommonPageResponse<PostListItemResponse> searchPosts(String keyword, Pageable pageable) {

		if (!StringUtils.hasText(keyword)) {
			return CommonPageResponse.of(Page.empty(pageable));
		}

		Page<Post> postPage = postRepository
			.findByTitleContainingIgnoreCaseOrBodyContainingIgnoreCase(keyword, keyword, pageable);

		var postIds = postPage.stream().map(Post::getId).collect(Collectors.toList());
		var commentCounts = commentRepository.countByPostIds(postIds);
		var likeCounts = postLikeRepository.countByPostIdsAndType(postIds, ReactionType.LIKE);

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

	@Transactional(readOnly = true)
	public CommonPageResponse<PostListItemResponse> getAnswerablePosts(Long memberId, int page, int size) {
		MilitaryInfo memberInfo = militaryInfoRepository.findByMemberId(memberId)
			.orElseThrow(() -> new MiriMiliException(MilitaryInfoErrorCode.MILITARY_INFO_NOT_FOUND));

		List<Post> allAnswerablePosts = postRepository.findAnswerablePosts(
			memberInfo.getMiliType(),
			memberInfo.getSpecialty()
		);

		// 답변 대기 지수로 정렬 (높은 순서대로)
		allAnswerablePosts = allAnswerablePosts.stream()
			.sorted((a, b) -> {
				double scoreA = calculateWaitingAnswerScore(a.getId(), a.getCreatedAt());
				double scoreB = calculateWaitingAnswerScore(b.getId(), b.getCreatedAt());
				return Double.compare(scoreB, scoreA); // 내림차순
			})
			.toList();

		// 페이징 처리
		int start = page * size;
		int end = Math.min(start + size, allAnswerablePosts.size());
		List<Post> pagedPosts = allAnswerablePosts.subList(start, end);

		List<Long> postIds = pagedPosts.stream().map(Post::getId).toList();
		Map<Long, Long> commentCounts = commentRepository.countByPostIds(postIds);
		Map<Long, Long> likeCounts = postLikeRepository.countByPostIdsAndType(postIds, ReactionType.LIKE);

		List<PostListItemResponse> dtoList = pagedPosts.stream()
			.map(post -> new PostListItemResponse(
				post.getId(),
				post.getTitle(),
				post.getBody(),
				commentCounts.getOrDefault(post.getId(), 0L),
				likeCounts.getOrDefault(post.getId(), 0L),
				post.getViewCount(),
				post.getCreatedAt()
			))
			.toList();

		return CommonPageResponse.of(dtoList, page, size, allAnswerablePosts.size());
	}

	private double calculateWaitingAnswerScore(Long postId, LocalDateTime createdAt) {
		Long viewCount = postRepository.findById(postId).map(Post::getViewCount).orElse(0L);
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

	private double calculateFreshnessBonus(LocalDateTime createdAt) {
		long daysElapsed = java.time.temporal.ChronoUnit.DAYS.between(createdAt, LocalDateTime.now());
		return 50.0 / (daysElapsed + 1);
	}
}
