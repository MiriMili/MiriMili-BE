package org.example.mirimilibe.post.domain;

import java.time.LocalDateTime;

import org.example.mirimilibe.member.domain.Member;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "user_recent_search",
	uniqueConstraints = @UniqueConstraint(name = "uq_user_query", columnNames = {"user_id", "query"}))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class RecentSearch {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "user_id", nullable = false)
	private Member user;

	@Column(name = "query", nullable = false, length = 100)
	private String query;

	@Column(name = "searched_at", nullable = false)
	private LocalDateTime searchedAt;

	public void touch(LocalDateTime now) {
		this.searchedAt = now;
	}
}
