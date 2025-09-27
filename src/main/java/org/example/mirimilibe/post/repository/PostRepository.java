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


}


