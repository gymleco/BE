package kr.co.gymleco.domain.audit;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;

public interface AdminAuditLogRepository extends JpaRepository<AdminAuditLog, Long> {
    Page<AdminAuditLog> findAllByOrderByCreatedAtDesc(Pageable pageable);
    Page<AdminAuditLog> findByActionOrderByCreatedAtDesc(AuditAction action, Pageable pageable);
    long countByActionAndCreatedAtAfter(AuditAction action, Instant since);
}
