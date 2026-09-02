package kr.co.gymleco.domain.support;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface NoticeRepository extends JpaRepository<Notice, Long> {
    @Query("""
        SELECT n FROM Notice n
        WHERE n.visible = true
        ORDER BY n.pinned DESC, n.publishedAt DESC NULLS LAST, n.id DESC
        """)
    List<Notice> findPublished();

    Optional<Notice> findByIdAndVisibleTrue(Long id);

    List<Notice> findAllByOrderByPinnedDescIdDesc();
}
