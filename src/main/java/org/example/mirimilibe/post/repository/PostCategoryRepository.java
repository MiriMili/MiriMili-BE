package org.example.mirimilibe.post.repository;

import java.util.List;

import org.example.mirimilibe.post.domain.Post;
import org.example.mirimilibe.post.domain.PostCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PostCategoryRepository extends JpaRepository<PostCategory, Long> {
	List<PostCategory> findAllByPost(Post post);

	@Query("""
		SELECT pc FROM PostCategory pc
		JOIN FETCH pc.category
		WHERE pc.post = :post
	""")
	List<PostCategory> findAllByPostWithCategory(@Param("post") Post post);

}
