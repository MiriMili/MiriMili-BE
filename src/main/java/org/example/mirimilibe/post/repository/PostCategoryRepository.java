package org.example.mirimilibe.post.repository;

import java.util.List;

import org.example.mirimilibe.post.domain.Post;
import org.example.mirimilibe.post.domain.PostCategory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostCategoryRepository extends JpaRepository<PostCategory, Long> {
	List<PostCategory> findAllByPost(Post post);

}
