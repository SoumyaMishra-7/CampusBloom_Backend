package com.campusbloom.backend.config;

import com.campusbloom.backend.model.AppUser;
import com.campusbloom.backend.model.AppUserRole;
import com.campusbloom.backend.repository.AppUserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class DemoUserInitializer {

    @Bean
    CommandLineRunner seedDemoUsers(AppUserRepository appUserRepository, PasswordEncoder passwordEncoder) {
        return args -> {
            seedStudentDemo(appUserRepository, passwordEncoder);
            seedAdminDemo(appUserRepository, passwordEncoder);
        };
    }

    private void seedStudentDemo(AppUserRepository appUserRepository, PasswordEncoder passwordEncoder) {
        if (appUserRepository.findByRoleAndEmailIgnoreCase(AppUserRole.STUDENT, "student@demo.campusbloom.com").isPresent()) {
            return;
        }

        AppUser user = new AppUser();
        user.setRole(AppUserRole.STUDENT);
        user.setFullName("CampusBloom Student Demo");
        user.setEmail("student@demo.campusbloom.com");
        user.setRollNumber("cb-stu-001");
        user.setDepartment("Computer Science");
        user.setPasswordHash(passwordEncoder.encode("student123"));
        appUserRepository.save(user);
    }

    private void seedAdminDemo(AppUserRepository appUserRepository, PasswordEncoder passwordEncoder) {
        if (appUserRepository.findByRoleAndEmailIgnoreCase(AppUserRole.ADMIN, "admin@demo.campusbloom.com").isPresent()) {
            return;
        }

        AppUser user = new AppUser();
        user.setRole(AppUserRole.ADMIN);
        user.setFullName("CampusBloom Admin Demo");
        user.setEmail("admin@demo.campusbloom.com");
        user.setAdminId("cb-adm-001");
        user.setInstitutionName("CampusBloom Demo Institution");
        user.setPasswordHash(passwordEncoder.encode("admin123"));
        appUserRepository.save(user);
    }
}
