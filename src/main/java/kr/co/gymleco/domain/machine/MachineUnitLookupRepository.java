package kr.co.gymleco.domain.machine;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;

public interface MachineUnitLookupRepository extends JpaRepository<MachineUnitLookup, Long> {
    /** 열거(번호 긁어가기) 방지 — 문의 접수의 rate limit 과 같은 방식 */
    long countBySourceIpAndCreatedAtAfter(String sourceIp, Instant since);
    /** 「이 번호가 언제 어디서 조회됐나」 — 복제 판단의 근거 */
    List<MachineUnitLookup> findTop50ByMachineUnitIdOrderByCreatedAtDesc(Long machineUnitId);
    /** 관리 화면의 「최근 조회」 */
    Page<MachineUnitLookup> findAllByOrderByCreatedAtDesc(Pageable pageable);
    /** 번호는 맞는데 모델이 다른 조회 — 가장 강한 위조 신호 */
    List<MachineUnitLookup> findTop50ByResultOrderByCreatedAtDesc(LookupResult result);
}
