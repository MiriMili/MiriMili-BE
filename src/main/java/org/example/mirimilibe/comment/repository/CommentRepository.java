package org.example.mirimilibe.comment.repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.example.mirimilibe.comment.domain.Comment;
import org.example.mirimilibe.comment.dto.CommentCreateRequest;
import org.example.mirimilibe.post.domain.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CommentRepository extends JpaRepository<Comment,Long> {

	List<Comment> findAllByWriterId(Long memberId);
	Page<Comment> findAllByWriterId(Long memberId, Pageable pageable);

	Long countByPostId(Long postId);

	List<Comment> findAllByPost(Post post);



	@Query("""
		SELECT c.post.id, COUNT(c)
		FROM Comment c
		WHERE c.post.id IN :postIds
		GROUP BY c.post.id
	""")
	List<Object[]> countRawByPostIds(@Param("postIds") List<Long> postIds);

	@Query("SELECT DISTINCT c.writer.id FROM Comment c WHERE c.post = :post")
	List<Long> findDistinctWriterIdsByPost(@Param("post") Post post);

	List<Comment> findAllByPostId(Long postId);

	@Query("SELECT c FROM Comment c WHERE c.isBestAnswer = true ORDER BY c.createdAt DESC")
	Page<Comment> findBestAnswers(Pageable pageable);

	List<Comment> findByIsBestAnswerTrueOrderByCreatedAtDesc();

	@Query("""
		SELECT c FROM Comment c
		JOIN FETCH c.writer
		WHERE c.post = :post
	""")
	List<Comment> findAllByPostWithWriter(@Param("post") Post post);

	default Map<Long, Long> countByPostIds(List<Long> postIds) {
		List<Object[]> result = countRawByPostIds(postIds);
		Map<Long, Long> map = new HashMap<>();
		for (Object[] row : result) {
			map.put((Long) row[0], (Long) row[1]);
		}
		return map;
	}
}
