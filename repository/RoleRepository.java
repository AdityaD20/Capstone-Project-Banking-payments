package com.aurionpro.app.repository;

import com.aurionpro.app.entity.enums.RoleType;
import com.aurionpro.app.entity.user.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {
    Optional<Role> findByName(RoleType name);
}