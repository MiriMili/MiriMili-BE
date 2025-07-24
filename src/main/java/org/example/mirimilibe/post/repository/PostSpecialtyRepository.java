package org.example.mirimilibe.post.repository;

import java.util.List;

import org.example.mirimilibe.post.domain.Post;
import org.example.mirimilibe.post.domain.PostSpecialty;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostSpecialtyRepository extends JpaRepository<PostSpecialty, Long> {

	List<PostSpecialty> findAllByPostId(Long postId);

	List<PostSpecialty> findAllByPost(Post post);


}
