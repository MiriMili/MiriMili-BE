package org.example.mirimilibe.post.repository;

import java.util.List;
import java.util.Optional;

import org.example.mirimilibe.member.domain.Member;
import org.example.mirimilibe.post.domain.Post;
import org.example.mirimilibe.post.domain.ScrapedPost;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ScrapedPostRepository extends JpaRepository<ScrapedPost, Long> {
	Optional<ScrapedPost> findByMemberAndPost(Member member, Post post);
	List<ScrapedPost> findAllByMember(Member member);
	Page<ScrapedPost> findAllByMember(Member member, Pageable pageable);

	@Query("SELECT COUNT(sp) FROM ScrapedPost sp WHERE sp.post.id = :postId")
	Long countByPostId(@Param("postId") Long postId);
}


