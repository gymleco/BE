package kr.co.gymleco.domain.machine;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface MachineUnitRepository extends JpaRepository<MachineUnit, Long> {

    @EntityGraph(attributePaths = "product")
    Optional<MachineUnit> findBySerialNorm(String serialNorm);
    /** 관리 화면 목록 — 제품을 함께 읽어 N+1 을 막는다 */
    @EntityGraph(attributePaths = "product")
    Page<MachineUnit> findAllByOrderByIdDesc(Pageable pageable);
    /** 「이 업체가 산 기구 전부」 — 사후 관리의 기본 조회 */
    @EntityGraph(attributePaths = "product")
    List<MachineUnit> findByOwnerIdOrderByIdDesc(Long ownerId);
    /** 「이번 달 보증 만료」 — 점검 영업 목록 */
    @EntityGraph(attributePaths = "product")
    List<MachineUnit> findByWarrantyUntilBetweenOrderByWarrantyUntilAsc(LocalDate from, LocalDate to);
    /** 일괄 등록 전에 이미 있는 번호를 걸러낸다 */
    @Query("select u.serialNorm from MachineUnit u where u.serialNorm in :norms")
    List<String> findExistingSerialNorms(@Param("norms") List<String> norms);

    boolean existsBySerialNorm(String serialNorm);
}
