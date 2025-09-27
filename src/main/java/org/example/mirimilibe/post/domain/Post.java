package org.example.mirimilibe.post.domain;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.example.mirimilibe.common.Enum.MiliType;
import org.example.mirimilibe.member.domain.Member;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderColumn;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Builder
public class Post {

	@Id
	@GeneratedValue(strategy = jakarta.persistence.GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "writer_id", nullable = false)
	private Member writer;

	private String title;

	private String body;

	@Builder.Default
	private Long viewCount=0L;

	@Enumerated(EnumType.STRING)
	@Column(name = "target_mili_type", nullable = false)
	private MiliType targetMiliType;

	@ElementCollection
	@Column(name = "image_key", length = 1024)
	@OrderColumn(name = "ord")
	private List<String> imageKeys;

	private LocalDateTime createdAt;

	@OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
	@Builder.Default
	private List<PostCategory> postCategories = new ArrayList<>();

	@Builder.Default
	private Boolean isHotQuestion = false;

	private LocalDateTime lastActivityAt;

	public void increaseViewCount() {
		this.viewCount += 1;
	}

	public void markAsHotQuestion() {
		this.isHotQuestion = true;
	}

	public void unmarkAsHotQuestion() {
		this.isHotQuestion = false;
	}

	public void updateLastActivity() {
		this.lastActivityAt = LocalDateTime.now();
	}
}
