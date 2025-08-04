package com.devops.service;

import com.devops.entity.Build;
import com.devops.entity.User;
import com.devops.repository.BuildRepository;
import com.devops.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

@Service
@Slf4j
public class DataInitializationService implements CommandLineRunner {
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private BuildRepository buildRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Override
    public void run(String... args) throws Exception {
        log.info("Initializing sample data for Voice DevOps Dashboard...");
        
        // Create sample users
        createSampleUsers();
        
        // Create sample builds
        createSampleBuilds();
        
        log.info("Sample data initialization completed!");
    }
    
    private void createSampleUsers() {
        if (userRepository.count() > 0) {
            log.info("Users already exist, skipping user creation");
            return;
        }
        
        List<User> users = Arrays.asList(
            createUser("admin", "admin@company.com", "Admin User", User.UserRole.ADMIN),
            createUser("manager", "manager@company.com", "Tech Manager", User.UserRole.ADMIN),
            createUser("developer", "developer@company.com", "John Developer", User.UserRole.USER),
            createUser("developer2", "developer2@company.com", "Jane Developer", User.UserRole.USER),
            createUser("senior", "senior@company.com", "Senior Developer", User.UserRole.USER)
        );
        
        userRepository.saveAll(users);
        log.info("Created {} sample users", users.size());
    }
    
    private User createUser(String username, String email, String fullName, User.UserRole role) {
        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setFullName(fullName);
        user.setPassword(passwordEncoder.encode("password123"));
        user.setRole(role);
        user.setIsActive(true);
        user.setCreatedAt(LocalDateTime.now());
        return user;
    }
    
    private void createSampleBuilds() {
        if (buildRepository.count() > 0) {
            log.info("Builds already exist, skipping build creation");
            return;
        }
        
        List<User> users = userRepository.findAll();
        User admin = userRepository.findByUsername("admin").orElse(users.get(0));
        User developer = userRepository.findByUsername("developer").orElse(users.get(2));
        
        List<Build> builds = Arrays.asList(
            // Production builds requiring approval
            createBuild("PROD-1001", "production-deploy", "release-2.1.0", 
                       Build.BuildStatus.PENDING_APPROVAL, "production", true, admin, developer),
            createBuild("PROD-1002", "production-deploy", "release-2.0.5", 
                       Build.BuildStatus.PENDING_APPROVAL, "production", true, admin, developer),
            
            // Running builds
            createBuild("BUILD-1001", "feature-build", "feature-branch", 
                       Build.BuildStatus.RUNNING, "development", false, developer, developer),
            createBuild("STAGE-1001", "staging-deploy", "feature-branch", 
                       Build.BuildStatus.RUNNING, "staging", false, developer, developer),
            
            // Completed builds
            createBuild("BUILD-1000", "feature-build", "bugfix-123", 
                       Build.BuildStatus.SUCCESS, "development", false, developer, developer),
            createBuild("BUILD-999", "feature-build", "feature-auth", 
                       Build.BuildStatus.SUCCESS, "development", false, developer, developer),
            createBuild("BUILD-998", "feature-build", "feature-payment", 
                       Build.BuildStatus.FAILED, "development", false, developer, developer),
            
            // Staging deployments
            createBuild("STAGE-1000", "staging-deploy", "release-2.1.0", 
                       Build.BuildStatus.SUCCESS, "staging", false, admin, admin),
            createBuild("STAGE-999", "staging-deploy", "release-2.0.5", 
                       Build.BuildStatus.SUCCESS, "staging", false, admin, admin)
        );
        
        buildRepository.saveAll(builds);
        log.info("Created {} sample builds", builds.size());
    }
    
    private Build createBuild(String jenkinsBuildId, String jobName, String branchName, 
                            Build.BuildStatus status, String environment, boolean requiresApproval, 
                            User triggeredBy, User approver) {
        Build build = new Build();
        build.setJenkinsBuildId(jenkinsBuildId);
        build.setJobName(jobName);
        build.setBranchName(branchName);
        build.setBuildNumber(Integer.parseInt(jenkinsBuildId.split("-")[1]));
        build.setStatus(status);
        build.setEnvironment(environment);
        build.setRequiresApproval(requiresApproval);
        build.setTriggeredBy(triggeredBy);
        build.setBuildUrl("http://mock-jenkins.company.com/job/" + jobName + "/" + build.getBuildNumber());
        build.setStartedAt(LocalDateTime.now().minusMinutes(30));
        
        if (status == Build.BuildStatus.SUCCESS || status == Build.BuildStatus.FAILED) {
            build.setCompletedAt(LocalDateTime.now().minusMinutes(5));
            build.setDurationSeconds(300L); // 5 minutes
        }
        
        if (status == Build.BuildStatus.SUCCESS && requiresApproval) {
            build.setApprovedBy(approver.getUsername());
            build.setApprovedAt(LocalDateTime.now().minusMinutes(25));
        }
        
        return build;
    }
} 