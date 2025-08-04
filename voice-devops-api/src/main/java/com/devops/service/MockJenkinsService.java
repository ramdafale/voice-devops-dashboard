package com.devops.service;

import com.devops.entity.*;
import com.devops.repository.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

@Service
@Slf4j
public class MockJenkinsService {
    
    @Autowired
    private BuildRepository buildRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    private final AtomicInteger buildCounter = new AtomicInteger(1000);
    private final Map<String, Build> mockBuilds = new HashMap<>();
    
    public VoiceCommandProcessor.CommandResponse approveBuild(Map<String, String> parameters, User user) {
        try {
            String buildId = parameters.get("buildId");
            if (buildId == null) {
                return new VoiceCommandProcessor.CommandResponse("Build ID not specified", false);
            }
            
            // Find the build
            Optional<Build> buildOpt = buildRepository.findByJenkinsBuildId(buildId);
            if (buildOpt.isEmpty()) {
                return new VoiceCommandProcessor.CommandResponse("Build not found: " + buildId, false);
            }
            
            Build build = buildOpt.get();
            if (build.getStatus() != Build.BuildStatus.PENDING_APPROVAL) {
                return new VoiceCommandProcessor.CommandResponse("Build is not pending approval", false);
            }
            
            // Approve the build
            build.setStatus(Build.BuildStatus.RUNNING);
            build.setApprovedBy(user.getUsername());
            build.setApprovedAt(LocalDateTime.now());
            buildRepository.save(build);
            
            log.info("Build {} approved by {}", buildId, user.getUsername());
            
            return new VoiceCommandProcessor.CommandResponse(
                "Build " + buildId + " approved successfully. Build is now running.", true);
                
        } catch (Exception e) {
            log.error("Error approving build", e);
            return new VoiceCommandProcessor.CommandResponse("Error approving build: " + e.getMessage(), false);
        }
    }
    
    public VoiceCommandProcessor.CommandResponse deployToProduction(Map<String, String> parameters, User user) {
        try {
            String branch = parameters.get("branch");
            if (branch == null) {
                return new VoiceCommandProcessor.CommandResponse("Branch not specified", false);
            }
            
            // Create a new production build
            Build build = new Build();
            build.setJenkinsBuildId("PROD-" + buildCounter.incrementAndGet());
            build.setJobName("production-deploy");
            build.setBranchName(branch);
            build.setBuildNumber(buildCounter.get());
            build.setStatus(Build.BuildStatus.PENDING_APPROVAL);
            build.setEnvironment("production");
            build.setRequiresApproval(true);
            build.setTriggeredBy(user);
            build.setBuildUrl("http://mock-jenkins.company.com/job/production-deploy/" + build.getBuildNumber());
            build.setStartedAt(LocalDateTime.now());
            
            buildRepository.save(build);
            
            log.info("Production deployment triggered for branch {} by {}", branch, user.getUsername());
            
            return new VoiceCommandProcessor.CommandResponse(
                "Production deployment triggered for branch " + branch + ". Build ID: " + build.getJenkinsBuildId() + 
                ". Awaiting approval.", true);
                
        } catch (Exception e) {
            log.error("Error deploying to production", e);
            return new VoiceCommandProcessor.CommandResponse("Error deploying to production: " + e.getMessage(), false);
        }
    }
    
    public VoiceCommandProcessor.CommandResponse abortBuild(Map<String, String> parameters, User user) {
        try {
            String buildId = parameters.get("buildId");
            if (buildId == null) {
                return new VoiceCommandProcessor.CommandResponse("Build ID not specified", false);
            }
            
            // Find the build
            Optional<Build> buildOpt = buildRepository.findByJenkinsBuildId(buildId);
            if (buildOpt.isEmpty()) {
                return new VoiceCommandProcessor.CommandResponse("Build not found: " + buildId, false);
            }
            
            Build build = buildOpt.get();
            if (build.getStatus() != Build.BuildStatus.RUNNING && build.getStatus() != Build.BuildStatus.QUEUED) {
                return new VoiceCommandProcessor.CommandResponse("Build is not running or queued", false);
            }
            
            // Abort the build
            build.setStatus(Build.BuildStatus.ABORTED);
            build.setCompletedAt(LocalDateTime.now());
            buildRepository.save(build);
            
            log.info("Build {} aborted by {}", buildId, user.getUsername());
            
            return new VoiceCommandProcessor.CommandResponse("Build " + buildId + " aborted successfully.", true);
            
        } catch (Exception e) {
            log.error("Error aborting build", e);
            return new VoiceCommandProcessor.CommandResponse("Error aborting build: " + e.getMessage(), false);
        }
    }
    
    public VoiceCommandProcessor.CommandResponse getPendingApprovals() {
        try {
            List<Build> pendingBuilds = buildRepository.findPendingApprovals();
            
            if (pendingBuilds.isEmpty()) {
                return new VoiceCommandProcessor.CommandResponse("No pending approvals found.", true);
            }
            
            StringBuilder response = new StringBuilder("Pending approvals:\n");
            for (Build build : pendingBuilds) {
                response.append("• Build ").append(build.getJenkinsBuildId())
                       .append(" - ").append(build.getBranchName())
                       .append(" (triggered by ").append(build.getTriggeredBy().getUsername()).append(")\n");
            }
            
            return new VoiceCommandProcessor.CommandResponse(response.toString(), true);
            
        } catch (Exception e) {
            log.error("Error getting pending approvals", e);
            return new VoiceCommandProcessor.CommandResponse("Error getting pending approvals: " + e.getMessage(), false);
        }
    }
    
    public VoiceCommandProcessor.CommandResponse buildBranch(Map<String, String> parameters, User user) {
        try {
            String branch = parameters.get("branch");
            if (branch == null) {
                return new VoiceCommandProcessor.CommandResponse("Branch not specified", false);
            }
            
            // Create a new build
            Build build = new Build();
            build.setJenkinsBuildId("BUILD-" + buildCounter.incrementAndGet());
            build.setJobName("feature-build");
            build.setBranchName(branch);
            build.setBuildNumber(buildCounter.get());
            build.setStatus(Build.BuildStatus.RUNNING);
            build.setEnvironment("development");
            build.setRequiresApproval(false);
            build.setTriggeredBy(user);
            build.setBuildUrl("http://mock-jenkins.company.com/job/feature-build/" + build.getBuildNumber());
            build.setStartedAt(LocalDateTime.now());
            
            buildRepository.save(build);
            
            log.info("Build triggered for branch {} by {}", branch, user.getUsername());
            
            return new VoiceCommandProcessor.CommandResponse(
                "Build triggered for branch " + branch + ". Build ID: " + build.getJenkinsBuildId(), true);
                
        } catch (Exception e) {
            log.error("Error building branch", e);
            return new VoiceCommandProcessor.CommandResponse("Error building branch: " + e.getMessage(), false);
        }
    }
    
    public VoiceCommandProcessor.CommandResponse deployToStaging(Map<String, String> parameters, User user) {
        try {
            String branch = parameters.get("branch");
            if (branch == null) {
                return new VoiceCommandProcessor.CommandResponse("Branch not specified", false);
            }
            
            // Create a staging deployment
            Build build = new Build();
            build.setJenkinsBuildId("STAGE-" + buildCounter.incrementAndGet());
            build.setJobName("staging-deploy");
            build.setBranchName(branch);
            build.setBuildNumber(buildCounter.get());
            build.setStatus(Build.BuildStatus.RUNNING);
            build.setEnvironment("staging");
            build.setRequiresApproval(false);
            build.setTriggeredBy(user);
            build.setBuildUrl("http://mock-jenkins.company.com/job/staging-deploy/" + build.getBuildNumber());
            build.setStartedAt(LocalDateTime.now());
            
            buildRepository.save(build);
            
            log.info("Staging deployment triggered for branch {} by {}", branch, user.getUsername());
            
            return new VoiceCommandProcessor.CommandResponse(
                "Staging deployment triggered for branch " + branch + ". Build ID: " + build.getJenkinsBuildId(), true);
                
        } catch (Exception e) {
            log.error("Error deploying to staging", e);
            return new VoiceCommandProcessor.CommandResponse("Error deploying to staging: " + e.getMessage(), false);
        }
    }
    
    public VoiceCommandProcessor.CommandResponse getUserBuilds(User user) {
        try {
            List<Build> userBuilds = buildRepository.findByTriggeredByOrderByStartedAtDesc(user);
            
            if (userBuilds.isEmpty()) {
                return new VoiceCommandProcessor.CommandResponse("No builds found for your account.", true);
            }
            
            StringBuilder response = new StringBuilder("Your recent builds:\n");
            for (Build build : userBuilds.subList(0, Math.min(5, userBuilds.size()))) {
                response.append("• ").append(build.getJenkinsBuildId())
                       .append(" - ").append(build.getBranchName())
                       .append(" (").append(build.getStatus()).append(")\n");
            }
            
            return new VoiceCommandProcessor.CommandResponse(response.toString(), true);
            
        } catch (Exception e) {
            log.error("Error getting user builds", e);
            return new VoiceCommandProcessor.CommandResponse("Error getting builds: " + e.getMessage(), false);
        }
    }
    
    public VoiceCommandProcessor.CommandResponse getBuildStatus(Map<String, String> parameters, User user) {
        try {
            String buildId = parameters.get("buildId");
            if (buildId == null) {
                return new VoiceCommandProcessor.CommandResponse("Build ID not specified", false);
            }
            
            Optional<Build> buildOpt = buildRepository.findByJenkinsBuildId(buildId);
            if (buildOpt.isEmpty()) {
                return new VoiceCommandProcessor.CommandResponse("Build not found: " + buildId, false);
            }
            
            Build build = buildOpt.get();
            String status = build.getStatus().toString();
            String branch = build.getBranchName();
            String environment = build.getEnvironment();
            
            return new VoiceCommandProcessor.CommandResponse(
                "Build " + buildId + " status: " + status + 
                " (Branch: " + branch + ", Environment: " + environment + ")", true);
                
        } catch (Exception e) {
            log.error("Error getting build status", e);
            return new VoiceCommandProcessor.CommandResponse("Error getting build status: " + e.getMessage(), false);
        }
    }
    
    // Mock method to simulate build completion
    public void simulateBuildCompletion() {
        List<Build> runningBuilds = buildRepository.findByStatus(Build.BuildStatus.RUNNING);
        for (Build build : runningBuilds) {
            // Simulate 80% success rate
            if (Math.random() < 0.8) {
                build.setStatus(Build.BuildStatus.SUCCESS);
            } else {
                build.setStatus(Build.BuildStatus.FAILED);
            }
            build.setCompletedAt(LocalDateTime.now());
            build.setDurationSeconds(300L); // 5 minutes
            buildRepository.save(build);
        }
    }
} 