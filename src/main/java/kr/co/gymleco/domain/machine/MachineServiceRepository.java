package kr.co.gymleco.domain.machine;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MachineServiceRepository extends JpaRepository<MachineService, Long> {
    List<MachineService> findByMachineUnitIdOrderByHappenedOnDescIdDesc(Long machineUnitId);
}
