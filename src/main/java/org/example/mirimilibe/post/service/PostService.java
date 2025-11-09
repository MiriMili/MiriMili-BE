package org.example.mirimilibe.post.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.example.mirimilibe.comment.domain.ReactionType;
import org.example.mirimilibe.comment.repository.CommentRepository;
import org.example.mirimilibe.common.domain.Specialty;
import org.example.mirimilibe.global.CommonPageResponse;
import org.example.mirimilibe.global.auth.service.S3UploadService;
import org.example.mirimilibe.global.error.CommentErrorCode;
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
import org.example.mirimilibe.post.dto.PostMyInfoRes;
import org.example.mirimilibe.post.repository.CategoryRepository;
import org.example.mirimilibe.post.repository.PostCategoryRepository;
import org.example.mirimilibe.post.repository.PostLikeRepository;
import org.example.mirimilibe.post.repository.PostRepository;
import org.example.mirimilibe.post.repository.PostSpecialtyRepository;
import org.example.mirimilibe.post.repository.SpecialtyRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
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
		// DB에서 정렬+페이징 처리 (메모리 낭비 제거)
		PageRequest pageable = PageRequest.of(page, size);
		Page<Post> postPage;

		if ("hotScore".equals(sortBy)) {
			// HOT Score로 정렬 (DB에서 계산)
			postPage = postRepository.findPostsByCategoryWithHotScore(categoryIds, pageable);
		} else {
			// 생성일시로 정렬 (DB에서 정렬)
			postPage = postRepository.findPostsByCategoryOrderByCreatedAt(categoryIds, pageable);
		}

		// 페이징된 결과만 사용 (10개만 조회)
		List<Long> postIds = postPage.stream().map(Post::getId).toList();
		Map<Long, Long> commentCounts = commentRepository.countByPostIds(postIds);
		Map<Long, Long> likeCounts = postLikeRepository.countByPostIdsAndType(postIds, ReactionType.LIKE);

		List<PostListItemResponse> dtoList = postPage.stream()
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

		return CommonPageResponse.of(dtoList, page, size, (int) postPage.getTotalElements());
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

		// DB에서 답변 대기 지수로 정렬+페이징 (메모리 낭비 제거)
		PageRequest pageable = PageRequest.of(page, size);
		Page<Post> postPage = postRepository.findAnswerablePostsWithScore(
			memberInfo.getMiliType().name(),
			memberInfo.getSpecialty() != null ? memberInfo.getSpecialty().getId() : null,
			pageable
		);

		// 페이징된 결과만 사용 (10개만 조회)
		List<Long> postIds = postPage.stream().map(Post::getId).toList();
		Map<Long, Long> commentCounts = commentRepository.countByPostIds(postIds);
		Map<Long, Long> likeCounts = postLikeRepository.countByPostIdsAndType(postIds, ReactionType.LIKE);

		List<PostListItemResponse> dtoList = postPage.stream()
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

		return CommonPageResponse.of(dtoList, page, size, (int) postPage.getTotalElements());
	}

	public PostMyInfoRes getPostMyInfo(Long memberId, Long postId) {
		Post post = postRepository.findById(postId)
			.orElseThrow(() -> new MiriMiliException(PostErrorCode.POST_NOT_FOUND));

		Member member = memberRepository.findById(memberId)
			.orElseThrow(() -> new MiriMiliException(MemberErrorCode.MEMBER_NOT_FOUND));

		MilitaryInfo info = militaryInfoRepository.findByMemberId(memberId)
			.orElseThrow(() -> new MiriMiliException(MilitaryInfoErrorCode.MILITARY_INFO_NOT_FOUND));

		boolean matchedMili = post.getTargetMiliType() == null || post.getTargetMiliType() == info.getMiliType();

		List<Specialty> allowedSpecialties = postSpecialtyRepository.findAllByPostId(postId)
			.stream().map(PostSpecialty::getSpecialty).toList();

		boolean matchedSpecialty = allowedSpecialties.isEmpty() ||
			allowedSpecialties.contains(info.getSpecialty());

		boolean isAnswerable = matchedMili || matchedSpecialty;

		return new PostMyInfoRes(
			member.getNickname(),
			isAnswerable,
			info.getSpecialty() != null ? info.getSpecialty().getValue() : null,
			info.getMiliType(),
			info.getMiliStatus()
		);

	}
}
