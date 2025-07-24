package org.example.mirimilibe.post.repository;

import org.example.mirimilibe.common.domain.Category;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryRepository extends JpaRepository<Category, Long> {}
