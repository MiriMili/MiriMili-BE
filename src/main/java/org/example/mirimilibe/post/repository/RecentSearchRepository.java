package org.example.mirimilibe.post.repository;

import java.util.List;
import java.util.Optional;

import org.example.mirimilibe.post.domain.RecentSearch;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

public interface RecentSearchRepository extends JpaRepository<RecentSearch, Long> {

	// 최신 5개
	@Query("""
        select r.query
        from RecentSearch r
        where r.user.id = :userId
        order by r.searchedAt desc
    """)
	List<String> findTop5Keywords(@Param("userId") Long userId, org.springframework.data.domain.Pageable pageable);

	Optional<RecentSearch> findByUser_IdAndQuery(Long userId, String query);

	long countByUser_Id(Long userId);

	// 가장 오래된 1개 삭제 (FIFO 밀어내기)
	@Transactional
	@Modifying
	@Query(value = """
        DELETE FROM user_recent_search
        WHERE id = (
            SELECT id FROM (
                SELECT id
                FROM user_recent_search
                WHERE user_id = :userId
                ORDER BY searched_at ASC
                LIMIT 1
            ) t
        )
        """, nativeQuery = true)
	void deleteOldestOne(@Param("userId") Long userId);
}