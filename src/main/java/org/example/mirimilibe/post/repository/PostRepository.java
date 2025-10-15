package org.example.mirimilibe.post.repository;

import java.util.List;

import org.example.mirimilibe.common.Enum.MiliType;
import org.example.mirimilibe.common.domain.Specialty;
import org.example.mirimilibe.post.domain.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PostRepository extends JpaRepository<Post, Long> {

	@Query("""
		SELECT DISTINCT p FROM Post p
		JOIN PostCategory pc ON pc.post = p
		WHERE pc.category.id IN :categoryIds
	""")
	Page<Post> findDistinctByCategories(@Param("categoryIds") List<Long> categoryIds, Pageable pageable);

	@Query("""
		SELECT DISTINCT p FROM Post p
		JOIN PostCategory pc ON pc.post = p
		WHERE pc.category.id IN :categoryIds
	""")
	List<Post> findDistinctByCategories(@Param("categoryIds") List<Long> categoryIds);


	Page<Post> findByTitleContainingIgnoreCaseOrBodyContainingIgnoreCase(
		String titleKeyword,
		String bodyKeyword,
		Pageable pageable
	);

	List<Post> findByWriterId(Long memberId);

	@Query("SELECT p FROM Post p WHERE p.isHotQuestion = true ORDER BY p.createdAt DESC")
	Page<Post> findHotQuestions(Pageable pageable);

	List<Post> findByIsHotQuestionTrueOrderByCreatedAtDesc();

	@Query("""
		SELECT DISTINCT p FROM Post p
		LEFT JOIN PostSpecialty ps ON ps.post = p
		WHERE (p.targetMiliType IS NULL OR p.targetMiliType = :miliType)
		OR (ps.specialty IS NULL OR ps.specialty = :specialty)
		ORDER BY p.createdAt DESC
	""")
	List<Post> findAnswerablePosts(@Param("miliType") MiliType miliType,
								   @Param("specialty") Specialty specialty);

	@Query("""
		SELECT p FROM Post p
		LEFT JOIN FETCH p.writer
		WHERE p.id = :postId
	""")
	java.util.Optional<Post> findByIdWithWriter(@Param("postId") Long postId);

	// HOT Score로 정렬된 카테고리별 게시물 조회 (DB에서 정렬+페이징)
	@Query(value = """
		SELECT DISTINCT p.*
		FROM post p
		INNER JOIN post_category pc ON pc.post_id = p.id
		LEFT JOIN (
		    SELECT post_id, COUNT(*) as like_count
		    FROM post_like
		    WHERE type = 'LIKE'
		    GROUP BY post_id
		) pl ON pl.post_id = p.id
		LEFT JOIN (
		    SELECT post_id, COUNT(*) as dislike_count
		    FROM post_like
		    WHERE type = 'DISLIKE'
		    GROUP BY post_id
		) pd ON pd.post_id = p.id
		LEFT JOIN (
		    SELECT post_id, COUNT(*) as comment_count
		    FROM comment
		    GROUP BY post_id
		) c ON c.post_id = p.id
		LEFT JOIN (
		    SELECT post_id, COUNT(*) as scrap_count
		    FROM scraped_post
		    GROUP BY post_id
		) s ON s.post_id = p.id
		WHERE pc.category_id IN :categoryIds
		ORDER BY (
		    COALESCE(pl.like_count, 0) * 5.0 +
		    COALESCE(pd.dislike_count, 0) * -2.0 +
		    COALESCE(p.view_count, 0) * 0.1 +
		    COALESCE(c.comment_count, 0) * 4.0 +
		    COALESCE(s.scrap_count, 0) * 5.0
		) / POW(
		    TIMESTAMPDIFF(HOUR, COALESCE(p.last_activity_at, p.created_at), NOW()) + 1,
		    0.5
		) DESC
		""",
		countQuery = """
		SELECT COUNT(DISTINCT p.id)
		FROM post p
		INNER JOIN post_category pc ON pc.post_id = p.id
		WHERE pc.category_id IN :categoryIds
		""",
		nativeQuery = true)
	Page<Post> findPostsByCategoryWithHotScore(@Param("categoryIds") List<Long> categoryIds, Pageable pageable);

	// 생성일시로 정렬된 카테고리별 게시물 조회 (DB에서 정렬+페이징)
	@Query("""
		SELECT DISTINCT p FROM Post p
		INNER JOIN PostCategory pc ON pc.post = p
		WHERE pc.category.id IN :categoryIds
		ORDER BY p.createdAt DESC
	""")
	Page<Post> findPostsByCategoryOrderByCreatedAt(@Param("categoryIds") List<Long> categoryIds, Pageable pageable);

	// 답변 대기 지수로 정렬된 답변 가능 게시물 조회 (DB에서 정렬+페이징)
	@Query(value = """
		SELECT DISTINCT p.*
		FROM post p
		LEFT JOIN post_specialty ps ON ps.post_id = p.id
		LEFT JOIN (
		    SELECT post_id, COUNT(*) as like_count
		    FROM post_like
		    WHERE type = 'LIKE'
		    GROUP BY post_id
		) pl ON pl.post_id = p.id
		LEFT JOIN (
		    SELECT post_id, COUNT(*) as comment_count
		    FROM comment
		    GROUP BY post_id
		) c ON c.post_id = p.id
		LEFT JOIN (
		    SELECT post_id, COUNT(*) as scrap_count
		    FROM scraped_post
		    GROUP BY post_id
		) s ON s.post_id = p.id
		WHERE (p.target_mili_type IS NULL OR p.target_mili_type = :miliType)
		   OR (ps.specialty_id IS NULL OR ps.specialty_id = :specialtyId)
		ORDER BY (
		    COALESCE(p.view_count, 0) * 0.5 +
		    COALESCE(pl.like_count, 0) * 3.0 +
		    COALESCE(s.scrap_count, 0) * 5.0 +
		    COALESCE(c.comment_count, 0) * -100.0 +
		    (50.0 / (TIMESTAMPDIFF(DAY, p.created_at, NOW()) + 1)) * 50.0
		) DESC
		""",
		countQuery = """
		SELECT COUNT(DISTINCT p.id)
		FROM post p
		LEFT JOIN post_specialty ps ON ps.post_id = p.id
		WHERE (p.target_mili_type IS NULL OR p.target_mili_type = :miliType)
		   OR (ps.specialty_id IS NULL OR ps.specialty_id = :specialtyId)
		""",
		nativeQuery = true)
	Page<Post> findAnswerablePostsWithScore(
		@Param("miliType") String miliType,
		@Param("specialtyId") Long specialtyId,
		Pageable pageable
	);


}


