package kr.co.gymleco.domain.inquiry;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;

public interface InquiryRepository extends JpaRepository<Inquiry, Long> {
    Page<Inquiry> findByStatusOrderByCreatedAtDesc(InquiryStatus status, Pageable pageable);
    Page<Inquiry> findAllByOrderByCreatedAtDesc(Pageable pageable);
    List<Inquiry> findByPhoneBlindIndexOrderByCreatedAtDesc(byte[] phoneBlindIndex);
    @Query("SELECT i FROM Inquiry i WHERE i.purgeAt < :now ORDER BY i.purgeAt ASC")
    List<Inquiry> findPurgeTargets(@Param("now") Instant now, Pageable pageable);
    long countByStatus(InquiryStatus status);
    long countByCreatedAtAfter(Instant since);
    @Query("SELECT COUNT(i) FROM Inquiry i WHERE i.sourceIp = :ip AND i.createdAt > :since")
    long countRecentByIp(@Param("ip") String ip, @Param("since") Instant since);
}
