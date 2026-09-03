package kr.co.gymleco.publicapi;

import kr.co.gymleco.service.admin.SettingAdminService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/public")
public class PublicSettingController {
    private final SettingAdminService service;
    public PublicSettingController(SettingAdminService service){
        this.service = service;
    }
    @GetMapping("/settings")
    public Map<String, String> settings(){
        return service.publicOnly();
    }
}
