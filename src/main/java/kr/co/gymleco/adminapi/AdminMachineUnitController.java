package kr.co.gymleco.adminapi;

import kr.co.gymleco.service.machine.MachineUnitAdminService;
import kr.co.gymleco.service.machine.MachineUnitDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/machine-units")
@PreAuthorize("hasRole('ADMIN')")
public class AdminMachineUnitController {
    private final MachineUnitAdminService service;
    public AdminMachineUnitController(MachineUnitAdminService service){this.service = service;}
    @GetMapping("/by-serial")
    public ResponseEntity<MachineUnitDetail> bySerial(@RequestParam String serial){
        return service.findBySerial(serial).map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }
}
