package kr.co.gymleco.domain.used;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface UsedItemRepository extends JpaRepository<UsedItem, Long> {

    @Query("""
        SELECT u FROM UsedItem u
        WHERE u.visible = true
        ORDER BY CASE u.status
                     WHEN kr.co.gymleco.domain.used.UsedStatus.AVAILABLE THEN 0
                     WHEN kr.co.gymleco.domain.used.UsedStatus.RESERVED  THEN 1
                     ELSE 2
                 END ASC,
                 u.sortOrder ASC, u.id ASC
        """)
    List<UsedItem> findVisible();

    @EntityGraph(attributePaths = "images")
    Optional<UsedItem> findBySlugAndVisibleTrue(String slug);

    Optional<UsedItem> findBySlug(String slug);

    boolean existsBySlug(String slug);

    /** 관리자 목록 — 비공개·판매완료 포함 */
    List<UsedItem> findAllByOrderBySortOrderAscIdAsc();
}
