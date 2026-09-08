package kr.co.gymleco.domain.machine;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface MachineOwnerRepository extends JpaRepository<MachineOwner, Long> {
    /** 번호로 업체 찾기 */
    Optional<MachineOwner> findByPhoneBlindIndex(byte[] blindIndex);
    /** 상호로 찾기*/
    @Query("select o from MachineOwner o where lower(o.name) like lower(concat('%', :q, '%')) order by o.name")
    List<MachineOwner> searchByName(@Param("q") String q);
    List<MachineOwner> findAllByOrderByNameAsc();
}
