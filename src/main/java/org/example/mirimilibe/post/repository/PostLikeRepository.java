package org.example.mirimilibe.post.repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.example.mirimilibe.comment.domain.ReactionType;
import org.example.mirimilibe.post.domain.PostLike;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PostLikeRepository extends JpaRepository<PostLike, Long> {
	Optional<PostLike> findByMemberIdAndPostId(Long memberId, Long postId);

	@Query("""
		SELECT pl.post.id, COUNT(pl)
		FROM PostLike pl
		WHERE pl.post.id IN :postIds AND pl.type = :type
		GROUP BY pl.post.id
	""")
	List<Object[]> countRawByPostIdsAndType(@Param("postIds") List<Long> postIds, @Param("type") ReactionType type);

	default Map<Long, Long> countByPostIdsAndType(List<Long> postIds, ReactionType type) {
		List<Object[]> result = countRawByPostIdsAndType(postIds, type);
		Map<Long, Long> map = new HashMap<>();
		for (Object[] row : result) {
			map.put((Long) row[0], (Long) row[1]);
		}
		return map;
	}
}