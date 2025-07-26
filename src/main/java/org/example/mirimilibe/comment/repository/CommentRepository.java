package org.example.mirimilibe.comment.repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.example.mirimilibe.comment.domain.Comment;
import org.example.mirimilibe.comment.dto.CommentCreateRequest;
import org.example.mirimilibe.post.domain.Post;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CommentRepository extends JpaRepository<Comment,Long> {

	List<Comment> findAllByWriterId(Long memberId);

	Long countByPostId(Long postId);

	List<Comment> findAllByPost(Post post);



	@Query("""
		SELECT c.post.id, COUNT(c)
		FROM Comment c
		WHERE c.post.id IN :postIds
		GROUP BY c.post.id
	""")
	List<Object[]> countRawByPostIds(@Param("postIds") List<Long> postIds);

	default Map<Long, Long> countByPostIds(List<Long> postIds) {
		List<Object[]> result = countRawByPostIds(postIds);
		Map<Long, Long> map = new HashMap<>();
		for (Object[] row : result) {
			map.put((Long) row[0], (Long) row[1]);
		}
		return map;
	}
}
