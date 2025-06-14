package com.tcrs.tcrs_backend.config;

import com.tcrs.tcrs_backend.entity.Role;
import com.tcrs.tcrs_backend.entity.User;
import com.tcrs.tcrs_backend.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(DataInitializer.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        createAdminUser();
        upgradeUserToAdmin();
    }

    private void createAdminUser() {
        if (userRepository.findByEmail("admin@tcrs.com").isPresent()) {
            logger.info("Admin user already exists");
            return;
        }

        User adminUser = new User();
        adminUser.setFirstName("System");
        adminUser.setLastName("Admin");
        adminUser.setEmail("admin@tcrs.com");
        adminUser.setPhone("9999999999");
        adminUser.setPassword(passwordEncoder.encode("Admin@123"));
        adminUser.setRoles(Set.of(Role.ADMIN));
        adminUser.setIsActive(true);
        adminUser.setEmailVerified(true);
        adminUser.setPhoneVerified(true);

        userRepository.save(adminUser);
        logger.info("✅ Admin user created - Email: admin@tcrs.com, Password: Admin@123");
    }

    private void upgradeUserToAdmin() {
        // Upgrade your user to admin
        userRepository.findByEmail("ommanoj88@example.com").ifPresent(user -> {
            user.getRoles().clear();
            user.getRoles().add(Role.ADMIN);
            userRepository.save(user);
            logger.info("✅ Upgraded user {} to ADMIN role", user.getEmail());
        });
    }
}