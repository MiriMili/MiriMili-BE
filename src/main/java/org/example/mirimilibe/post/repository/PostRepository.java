package org.example.mirimilibe.post.repository;

import java.util.List;

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
}


