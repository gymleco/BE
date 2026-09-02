package kr.co.gymleco.domain.banner;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;

public interface BannerRepository extends JpaRepository<Banner, Long> {

    /**
     * 지금 노출할 배너.
     *
     * 기간을 비워 둘 수 있으므로 NULL 을 "제한 없음" 으로 읽는다.
     * 시작일만 있으면 그 뒤로 계속, 종료일만 있으면 그 전까지.
     */
    @Query("""
        SELECT b FROM Banner b
        WHERE b.visible = true
          AND b.position = :position
          AND (b.startsAt IS NULL OR b.startsAt <= :now)
          AND (b.endsAt   IS NULL OR b.endsAt   >  :now)
        ORDER BY b.sortOrder ASC, b.id ASC
        """)
    List<Banner> findLive(@Param("position") BannerPosition position,
                          @Param("now") Instant now);

    List<Banner> findAllByOrderByPositionAscSortOrderAscIdAsc();
}
