package com.devops.controller;

import com.devops.entity.*;
import com.devops.repository.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/dashboard")
@Slf4j
public class DashboardController {
    
    @Autowired
    private BuildRepository buildRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private VoiceCommandRepository voiceCommandRepository;
    
    @GetMapping("/admin")
    public ResponseEntity<Map<String, Object>> getAdminDashboard() {
        try {
            Map<String, Object> dashboard = new HashMap<>();
            
            // Pending approvals
            List<Build> pendingApprovals = buildRepository.findPendingApprovals();
            dashboard.put("pendingApprovals", pendingApprovals.stream()
                .map(this::buildToMap)
                .collect(Collectors.toList()));
            
            // Recent builds
            List<Build> recentBuilds = buildRepository.findRecentBuilds(LocalDateTime.now().minusDays(7));
            dashboard.put("recentBuilds", recentBuilds.stream()
                .map(this::buildToMap)
                .collect(Collectors.toList()));
            
            // Build statistics
            long successfulBuilds = buildRepository.countSuccessfulBuildsSince(LocalDateTime.now().minusDays(7));
            long failedBuilds = buildRepository.countFailedBuildsSince(LocalDateTime.now().minusDays(7));
            dashboard.put("buildStats", Map.of(
                "successful", successfulBuilds,
                "failed", failedBuilds,
                "total", successfulBuilds + failedBuilds
            ));
            
            // Voice command statistics
            long completedCommands = voiceCommandRepository.countCompletedCommandsSince(LocalDateTime.now().minusDays(7));
            long failedCommands = voiceCommandRepository.countFailedCommandsSince(LocalDateTime.now().minusDays(7));
            dashboard.put("voiceStats", Map.of(
                "completed", completedCommands,
                "failed", failedCommands,
                "total", completedCommands + failedCommands
            ));
            
            // Team activity
            List<User> activeUsers = userRepository.findActiveUsers();
            dashboard.put("activeUsers", activeUsers.size());
            
            return ResponseEntity.ok(dashboard);
            
        } catch (Exception e) {
            log.error("Error getting admin dashboard", e);
            return ResponseEntity.badRequest().build();
        }
    }
    
    @GetMapping("/user/{username}")
    public ResponseEntity<Map<String, Object>> getUserDashboard(@PathVariable String username) {
        try {
            Map<String, Object> dashboard = new HashMap<>();
            
            User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
            
            // User's recent builds
            List<Build> userBuilds = buildRepository.findByTriggeredByOrderByStartedAtDesc(user);
            dashboard.put("myBuilds", userBuilds.stream()
                .map(this::buildToMap)
                .collect(Collectors.toList()));
            
            // User's voice commands
            List<VoiceCommand> userCommands = voiceCommandRepository.findUserRecentCommands(user, LocalDateTime.now().minusDays(7));
            dashboard.put("myCommands", userCommands.stream()
                .map(this::commandToMap)
                .collect(Collectors.toList()));
            
            // User statistics
            long userSuccessfulBuilds = userBuilds.stream()
                .filter(build -> build.getStatus() == Build.BuildStatus.SUCCESS)
                .count();
            long userFailedBuilds = userBuilds.stream()
                .filter(build -> build.getStatus() == Build.BuildStatus.FAILED)
                .count();
            
            dashboard.put("userStats", Map.of(
                "successful", userSuccessfulBuilds,
                "failed", userFailedBuilds,
                "total", userBuilds.size()
            ));
            
            return ResponseEntity.ok(dashboard);
            
        } catch (Exception e) {
            log.error("Error getting user dashboard", e);
            return ResponseEntity.badRequest().build();
        }
    }
    
    @GetMapping("/builds")
    public ResponseEntity<List<Map<String, Object>>> getAllBuilds() {
        try {
            List<Build> builds = buildRepository.findAll();
            List<Map<String, Object>> buildMaps = builds.stream()
                .map(this::buildToMap)
                .collect(Collectors.toList());
            
            return ResponseEntity.ok(buildMaps);
            
        } catch (Exception e) {
            log.error("Error getting all builds", e);
            return ResponseEntity.badRequest().build();
        }
    }
    
    @GetMapping("/builds/{buildId}")
    public ResponseEntity<Map<String, Object>> getBuildDetails(@PathVariable String buildId) {
        try {
            Optional<Build> buildOpt = buildRepository.findByJenkinsBuildId(buildId);
            if (buildOpt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            
            return ResponseEntity.ok(buildToMap(buildOpt.get()));
            
        } catch (Exception e) {
            log.error("Error getting build details", e);
            return ResponseEntity.badRequest().build();
        }
    }
    
    @GetMapping("/commands")
    public ResponseEntity<List<Map<String, Object>>> getRecentCommands() {
        try {
            List<VoiceCommand> commands = voiceCommandRepository.findRecentCommands(LocalDateTime.now().minusDays(7));
            List<Map<String, Object>> commandMaps = commands.stream()
                .map(this::commandToMap)
                .collect(Collectors.toList());
            
            return ResponseEntity.ok(commandMaps);
            
        } catch (Exception e) {
            log.error("Error getting recent commands", e);
            return ResponseEntity.badRequest().build();
        }
    }
    
    @GetMapping("/api-deployments")
    public ResponseEntity<List<Map<String, Object>>> getApiDeployments() {
        try {
            List<Build> apiDeployments = buildRepository.findApiDeployments();
            List<Map<String, Object>> deploymentMaps = apiDeployments.stream()
                .map(this::buildToMap)
                .collect(Collectors.toList());
            
            return ResponseEntity.ok(deploymentMaps);
            
        } catch (Exception e) {
            log.error("Error getting API deployments", e);
            return ResponseEntity.badRequest().build();
        }
    }
    
    @GetMapping("/api-deployments/{buildId}/progress")
    public ResponseEntity<Map<String, Object>> getApiDeploymentProgress(@PathVariable String buildId) {
        try {
            Build build = buildRepository.findByJenkinsBuildId(buildId)
                .orElseThrow(() -> new RuntimeException("Build not found"));
            
            Map<String, Object> progress = new HashMap<>();
            progress.put("buildId", build.getJenkinsBuildId());
            progress.put("apiName", build.getApiName());
            progress.put("status", build.getStatus());
            progress.put("progress", build.getDeploymentProgress());
            progress.put("startedAt", build.getStartedAt());
            progress.put("completedAt", build.getCompletedAt());
            
            return ResponseEntity.ok(progress);
            
        } catch (Exception e) {
            log.error("Error getting API deployment progress", e);
            return ResponseEntity.badRequest().build();
        }
    }
    
    @PostMapping("/builds/{buildId}/approve")
    public ResponseEntity<Map<String, Object>> approveBuild(@PathVariable String buildId, @RequestBody Map<String, String> request) {
        try {
            Build build = buildRepository.findByJenkinsBuildId(buildId)
                .orElseThrow(() -> new RuntimeException("Build not found"));
            
            if (build.getStatus() != Build.BuildStatus.PENDING_APPROVAL) {
                return ResponseEntity.badRequest()
                    .body(Map.of("error", "Build is not pending approval"));
            }
            
            String approvedBy = request.get("approvedBy");
            if (approvedBy == null) {
                approvedBy = "admin";
            }
            
            build.setStatus(Build.BuildStatus.RUNNING);
            build.setApprovedBy(approvedBy);
            build.setApprovedAt(LocalDateTime.now());
            buildRepository.save(build);
            
            log.info("Build {} approved by {}", buildId, approvedBy);
            
            return ResponseEntity.ok(Map.of(
                "message", "Build " + buildId + " approved successfully",
                "buildId", buildId,
                "status", "RUNNING"
            ));
            
        } catch (Exception e) {
            log.error("Error approving build", e);
            return ResponseEntity.badRequest()
                .body(Map.of("error", "Error approving build: " + e.getMessage()));
        }
    }
    
    private Map<String, Object> buildToMap(Build build) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", build.getId());
        map.put("jenkinsBuildId", build.getJenkinsBuildId());
        map.put("jobName", build.getJobName());
        map.put("branchName", build.getBranchName());
        map.put("buildNumber", build.getBuildNumber());
        map.put("status", build.getStatus().toString());
        map.put("buildUrl", build.getBuildUrl());
        map.put("startedAt", build.getStartedAt());
        map.put("completedAt", build.getCompletedAt());
        map.put("durationSeconds", build.getDurationSeconds());
        map.put("environment", build.getEnvironment());
        map.put("requiresApproval", build.getRequiresApproval());
        map.put("approvedBy", build.getApprovedBy());
        map.put("approvedAt", build.getApprovedAt());
        
        if (build.getTriggeredBy() != null) {
            map.put("triggeredBy", build.getTriggeredBy().getUsername());
        }
        
        // Add API-specific fields
        map.put("apiName", build.getApiName());
        map.put("deploymentProgress", build.getDeploymentProgress());
        
        return map;
    }
    
    private Map<String, Object> commandToMap(VoiceCommand command) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", command.getId());
        map.put("originalText", command.getOriginalText());
        map.put("processedText", command.getProcessedText());
        map.put("commandType", command.getCommandType());
        map.put("parameters", command.getParameters());
        map.put("status", command.getStatus().toString());
        map.put("response", command.getResponse());
        map.put("executionTimeMs", command.getExecutionTimeMs());
        map.put("createdAt", command.getCreatedAt());
        map.put("processedAt", command.getProcessedAt());
        map.put("confidenceScore", command.getConfidenceScore());
        
        if (command.getUser() != null) {
            map.put("user", command.getUser().getUsername());
        }
        
        return map;
    }
} 