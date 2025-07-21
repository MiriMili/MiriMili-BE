package org.example.mirimilibe.member.domain;

import java.time.LocalDateTime;

import org.example.mirimilibe.common.Enum.TermType;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "term")
@NoArgsConstructor
@AllArgsConstructor
public class Term {
	@Id
	@GeneratedValue(strategy = jakarta.persistence.GenerationType.IDENTITY)
	private Long id;

	@Enumerated(EnumType.STRING)
	private TermType type;

	private String content;

	private LocalDateTime createdAt;
}