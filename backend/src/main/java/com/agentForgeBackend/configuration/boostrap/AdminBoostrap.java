package com.agentForgeBackend.configuration.boostrap;

import com.agentForgeBackend.models.hq.admin.AdminForm;
import com.agentForgeBackend.models.hq.admin.AdminMapper;
import com.agentForgeBackend.models.hq.admin.AdminRepository;
import com.agentForgeBackend.shared.models.baseUser.UserRoles;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
public class AdminBoostrap implements CommandLineRunner {

    private final AdminRepository adminRepository;
    private final AdminMapper adminMapper;
    private final Logger logger = LoggerFactory.getLogger(AdminBoostrap.class);
    private final PasswordEncoder passwordEncoder;

    public AdminBoostrap(
            AdminRepository adminRepository,
            AdminMapper adminMapper,
            PasswordEncoder passwordEncoder
    ){
        this.adminRepository = adminRepository;
        this.adminMapper = adminMapper;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) throws Exception {
        long adminCount = adminRepository.count();
        logger.info("Admin count: " + adminCount);
        if (adminCount == 0){
            logger.info("Creating first admin");
            AdminForm adminForm = new AdminForm();
            adminForm.setEmail("admin@init.com");
            adminForm.setUsername("admin");
            adminForm.setPassword(passwordEncoder.encode("test"));
            adminForm.setRoles(Set.of(UserRoles.ADMIN.toString()));
            logger.info("Admin form created, inserting");
            adminRepository.save(adminMapper.toEntity(adminForm));
            logger.info("Admin inserted");
        }
        else {
            logger.info("Admins already exist");
        }
    }
}
