package org.example.mirimilibe.post.service;

import java.time.LocalDateTime;
import java.util.List;

import org.example.mirimilibe.member.domain.Member;
import org.example.mirimilibe.member.repository.MemberRepository;
import org.example.mirimilibe.post.domain.RecentSearch;
import org.example.mirimilibe.post.dto.RecentDtos;
import org.example.mirimilibe.post.repository.RecentSearchRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RecentSearchService {

	private final RecentSearchRepository recentRepo;
	private final MemberRepository memberRepo;

	@Transactional
	public void add(Long userId, String rawQ) {
		if (userId == null || rawQ == null) return;
		String q = rawQ.trim();
		if (q.length() < 2) return;
		if (q.length() > 100) q = q.substring(0, 100);

		var now = LocalDateTime.now();

		// 1) 이미 있는 검색어면 timestamp 갱신
		var existing = recentRepo.findByUser_IdAndQuery(userId, q);
		if (existing.isPresent()) {
			existing.get().touch(now);
			return;
		}

		// 2) 없으면, 개수 확인 후 5개 꽉 찼으면 가장 오래된 것 1개 삭제 (FIFO)
		long count = recentRepo.countByUser_Id(userId);
		if (count >= 5) {
			recentRepo.deleteOldestOne(userId);
		}

		// 3) 새로 추가
		Member user = memberRepo.getReferenceById(userId);
		recentRepo.save(RecentSearch.builder()
			.user(user)
			.query(q)
			.searchedAt(now)
			.build());
	}

	@Transactional(readOnly = true)
	public RecentDtos.RecentRes top5(Long userId) {
		if (userId == null) return new RecentDtos.RecentRes(List.of());
		var list = recentRepo.findTop5Keywords(userId, PageRequest.of(0, 5));
		return new RecentDtos.RecentRes(list);
	}
}