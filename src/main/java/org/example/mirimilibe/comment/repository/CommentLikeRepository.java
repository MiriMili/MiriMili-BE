package org.example.mirimilibe.comment.repository;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.example.mirimilibe.comment.domain.CommentLike;
import org.example.mirimilibe.comment.domain.ReactionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CommentLikeRepository extends JpaRepository<CommentLike, Long> {
	Optional<CommentLike> findByMemberIdAndCommentId(Long memberId, Long commentId);

	// CommentLikeRepository
	@Query("""
	SELECT cl.comment.id, COUNT(cl)
	FROM CommentLike cl
	WHERE cl.comment.id IN :commentIds AND cl.type = :type
	GROUP BY cl.comment.id
""")
	Map<Long, Long> countByCommentIdsAndType(@Param("commentIds") List<Long> ids, @Param("type") ReactionType type);
}