package kr.co.gymleco.domain.admin;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AdminUserRepository extends JpaRepository<AdminUser, Long> {

    Optional<AdminUser> findByUsername(String username);
    Optional<AdminUser> findByUsernameAndEnabledTrue(String username);
    boolean existsByUsername(String username);
}
