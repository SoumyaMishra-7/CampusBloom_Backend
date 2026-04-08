package com.campusbloom.backend.repository;

import com.campusbloom.backend.model.AppUser;
import com.campusbloom.backend.model.AppUserRole;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AppUserRepository extends JpaRepository<AppUser, Long> {

    boolean existsByEmailIgnoreCase(String email);

    boolean existsByRoleAndRollNumberIgnoreCase(AppUserRole role, String rollNumber);

    boolean existsByRoleAndAdminIdIgnoreCase(AppUserRole role, String adminId);

    Optional<AppUser> findByRoleAndEmailIgnoreCase(AppUserRole role, String email);
}
