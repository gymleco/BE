package kr.co.gymleco.service.admin;

import kr.co.gymleco.config.GymlecoProperties;
import kr.co.gymleco.domain.admin.AdminRole;
import kr.co.gymleco.domain.admin.AdminUser;
import kr.co.gymleco.domain.admin.AdminUserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class AdminBootstrap implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(AdminBootstrap.class);
    private static final int MIN_PASSWORD_LENGTH = 12;

    private final AdminUserRepository repository;
    private final PasswordEncoder passwordEncoder;
    private final GymlecoProperties.Admin config;

    public AdminBootstrap(AdminUserRepository repository,
                          PasswordEncoder passwordEncoder,
                          GymlecoProperties properties) {
        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
        this.config = properties.admin();
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (config == null || !config.isConfigured()) {
            return;
        }
        if (repository.count() > 0) {
            return;
        }
        if (config.bootstrapPassword().length() < MIN_PASSWORD_LENGTH) {
            throw new IllegalStateException(
                "ADMIN_BOOTSTRAP_PASSWORD 는 최소 " + MIN_PASSWORD_LENGTH + "자여야 합니다.");
        }
        AdminUser admin = AdminUser.create(
            config.bootstrapUsername(),
            passwordEncoder.encode(config.bootstrapPassword()),
            "관리자",
            AdminRole.ADMIN);
        repository.save(admin);
        log.warn("최초 관리자 계정을 생성했습니다: {} — 로그인 후 비밀번호를 변경하고 " + "ADMIN_BOOTSTRAP_* 환경변수를 제거하세요.", config.bootstrapUsername());
    }
}
