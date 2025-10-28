package org.example.mirimilibe.notification.repository;

import org.example.mirimilibe.notification.domain.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    Page<Notification> findByMemberIdOrderByCreatedAtDesc(Long memberId, Pageable pageable);

    Long countByMemberIdAndIsReadFalse(Long memberId);

    Page<Notification> findByMemberIdAndIsReadFalseOrderByCreatedAtDesc(Long memberId, Pageable pageable);
}