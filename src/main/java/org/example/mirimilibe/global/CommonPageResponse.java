package org.example.mirimilibe.global;

import java.util.List;

import org.springframework.data.domain.Page;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CommonPageResponse<T> {
	private List<T> content;       // 데이터 리스트
	private int totalPages;        // 전체 페이지 수
	private long totalElements;    // 전체 요소 수
	private int currentPage;       // 현재 페이지 번호
	private int size;              // 한 페이지당 크기
	private boolean hasNext;       // 다음 페이지 존재 여부

	public static <T> CommonPageResponse<T> of(Page<T> page) {
		return CommonPageResponse.<T>builder()
			.content(page.getContent())
			.totalPages(page.getTotalPages())
			.totalElements(page.getTotalElements())
			.currentPage(page.getNumber())
			.size(page.getSize())
			.hasNext(page.hasNext())
			.build();
	}

	public static <T> CommonPageResponse<T> of(List<T> content, int currentPage, int size, long totalElements) {
		int totalPages = (int) Math.ceil((double) totalElements / size);
		boolean hasNext = currentPage < totalPages - 1;

		return CommonPageResponse.<T>builder()
			.content(content)
			.totalPages(totalPages)
			.totalElements(totalElements)
			.currentPage(currentPage)
			.size(size)
			.hasNext(hasNext)
			.build();
	}

}