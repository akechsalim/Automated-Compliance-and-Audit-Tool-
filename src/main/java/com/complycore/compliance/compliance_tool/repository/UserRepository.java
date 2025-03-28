package com.complycore.compliance.compliance_tool.repository;

import com.complycore.compliance.compliance_tool.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
}
