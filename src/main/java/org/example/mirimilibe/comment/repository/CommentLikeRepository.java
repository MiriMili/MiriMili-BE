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

	@Query("SELECT COUNT(cl) FROM CommentLike cl WHERE cl.comment.id = :commentId AND cl.type = :type")
	Long countByCommentIdAndType(@Param("commentId") Long commentId, @Param("type") ReactionType type);

	// CommentLikeRepository
	@Query("""
	SELECT cl.comment.id, COUNT(cl)
	FROM CommentLike cl
	WHERE cl.comment.id IN :commentIds AND cl.type = :type
	GROUP BY cl.comment.id
""")
	List<Object[]> countRawByCommentIdsAndType(@Param("commentIds") List<Long> ids, @Param("type") ReactionType type);

	default Map<Long, Long> countByCommentIdsAndType(List<Long> commentIds, ReactionType type){
		List<Object[]> result = countRawByCommentIdsAndType(commentIds, type);
		Map<Long, Long> map = new java.util.HashMap<>();
		for (Object[] row : result) {
			map.put((Long) row[0], (Long) row[1]);
		}
		return map;
	}

	@Query("""
		SELECT cl.comment.id
		FROM CommentLike cl
		WHERE cl.comment.id IN :commentIds AND cl.member.id = :memberId AND cl.type = :type
	""")
	List<Long> findCommentIdsByMemberIdAndType(@Param("commentIds") List<Long> commentIds,
		@Param("memberId") Long memberId,
		@Param("type") ReactionType type);

}