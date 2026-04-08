package com.campusbloom.backend.repository;

import com.campusbloom.backend.model.AppUser;
import com.campusbloom.backend.model.AppUserRole;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AppUserRepository extends JpaRepository<AppUser, Long> {

    boolean existsByEmailIgnoreCase(String email);

    boolean existsByRollNumberIgnoreCase(String rollNumber);

    boolean existsByAdminIdIgnoreCase(String adminId);

    Optional<AppUser> findByRoleAndEmailIgnoreCase(AppUserRole role, String email);

    Optional<AppUser> findByRoleAndRollNumberIgnoreCase(AppUserRole role, String rollNumber);

    Optional<AppUser> findByRoleAndAdminIdIgnoreCase(AppUserRole role, String adminId);
}
