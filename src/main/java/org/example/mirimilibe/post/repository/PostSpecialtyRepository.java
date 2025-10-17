package org.example.mirimilibe.post.repository;

import java.util.List;

import org.example.mirimilibe.post.domain.Post;
import org.example.mirimilibe.post.domain.PostSpecialty;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PostSpecialtyRepository extends JpaRepository<PostSpecialty, Long> {

	List<PostSpecialty> findAllByPostId(Long postId);

	List<PostSpecialty> findAllByPost(Post post);

	@Query("""
		SELECT ps FROM PostSpecialty ps
		JOIN FETCH ps.specialty
		WHERE ps.post = :post
	""")
	List<PostSpecialty> findAllByPostWithSpecialty(@Param("post") Post post);


}
