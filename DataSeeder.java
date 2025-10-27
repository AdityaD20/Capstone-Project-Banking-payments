package com.aurionpro.app;

import com.aurionpro.app.entity.enums.RoleType;
import com.aurionpro.app.entity.user.Role;
import com.aurionpro.app.entity.user.User;
import com.aurionpro.app.repository.RoleRepository;
import com.aurionpro.app.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository; 
    private final PasswordEncoder passwordEncoder; 

    @Override
    public void run(String... args) throws Exception {
        // --- Seed Roles ---
        if (roleRepository.count() == 0) {
            System.out.println("Seeding roles into the database...");
            for (RoleType roleType : RoleType.values()) {
                roleRepository.save(new Role(roleType));
            }
            System.out.println("Roles seeded successfully.");
        } else {
            System.out.println("Roles already exist. No seeding needed.");
        }

        // --- Seed Bank Admin User ---
        if (userRepository.findByEmail("admin@bank.com").isEmpty()) {
            System.out.println("Creating default bank admin user...");

            Role adminRole = roleRepository.findByName(RoleType.ROLE_BANK_ADMIN)
                    .orElseThrow(() -> new RuntimeException("Error: ROLE_BANK_ADMIN is not found."));

            User adminUser = new User();
            adminUser.setEmail("admin@bank.com");
            adminUser.setPassword(passwordEncoder.encode("admin123"));
            adminUser.setRoles(Set.of(adminRole));
            adminUser.setEnabled(true);

            //
         // --> You are missing: adminUser.setPasswordChangeRequired(false);
         //
            adminUser.setPasswordChangeRequired(false);
            userRepository.save(adminUser);
            System.out.println("Default bank admin user created.");
        } else {
            System.out.println("Bank admin user already exists.");
        }
    }
}