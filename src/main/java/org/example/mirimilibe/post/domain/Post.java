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

	private Long viewCount;

	@Enumerated(EnumType.STRING)
	@Column(name = "target_mili_type", nullable = false)
	private MiliType targetMiliType;

	@ElementCollection
	private List<String> imagesUrl;

	private LocalDateTime createdAt;

	@OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<PostCategory> postCategories = new ArrayList<>();

	public void increaseViewCount() {
		this.viewCount += 1;
	}
}
