package com.devops.controller;

import com.devops.entity.User;
import com.devops.service.DeploymentAgentService;
import com.devops.service.JenkinsApiClient;
import com.devops.service.GitHubApiClient;
import com.devops.service.VoiceCommandProcessor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/deployment-agent")
@Slf4j
public class DeploymentAgentController {
    
    @Autowired
    private DeploymentAgentService deploymentAgentService;
    
    @Autowired
    private JenkinsApiClient jenkinsApiClient;
    
    @Autowired
    private GitHubApiClient gitHubApiClient;
    
    /**
     * Intelligent deployment orchestration endpoint
     */
    @PostMapping("/orchestrate")
    public ResponseEntity<Map<String, Object>> orchestrateDeployment(
            @RequestBody Map<String, String> request,
            Authentication authentication) {
        
        try {
            String username = "admin"; // Default for testing
            if (authentication != null && authentication.getPrincipal() instanceof User) {
                User user = (User) authentication.getPrincipal();
                username = user.getUsername();
            }
            
            log.info("Deployment orchestration requested by {} with parameters: {}", 
                    username, request);
            
            // For testing, create a mock response
            Map<String, Object> result = Map.of(
                    "success", true,
                    "message", "Deployment orchestration initiated successfully",
                    "user", username,
                    "timestamp", System.currentTimeMillis()
            );
            
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            log.error("Error in deployment orchestration", e);
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Deployment orchestration failed: " + e.getMessage(),
                    "timestamp", System.currentTimeMillis()
            ));
        }
    }
    
    /**
     * Get deployment status
     */
    @GetMapping("/status/{deploymentId}")
    public ResponseEntity<Map<String, Object>> getDeploymentStatus(
            @PathVariable String deploymentId,
            Authentication authentication) {
        
        try {
            String username = "admin"; // Default for testing
            if (authentication != null && authentication.getPrincipal() instanceof User) {
                User user = (User) authentication.getPrincipal();
                username = user.getUsername();
            }
            
            log.info("Deployment status requested by {} for deployment: {}", 
                    username, deploymentId);
            
            // This would typically query the deployment status from the database
            // For now, return a mock response
            Map<String, Object> status = Map.of(
                    "deploymentId", deploymentId,
                    "status", "IN_PROGRESS",
                    "progress", 75,
                    "message", "Deployment is in progress",
                    "timestamp", System.currentTimeMillis()
            );
            
            return ResponseEntity.ok(status);
            
        } catch (Exception e) {
            log.error("Error getting deployment status", e);
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Error getting deployment status: " + e.getMessage()
            ));
        }
    }
    
    /**
     * Get Jenkins information
     */
    @GetMapping("/jenkins/info")
    public ResponseEntity<Map<String, Object>> getJenkinsInfo(Authentication authentication) {
        try {
            String username = "admin"; // Default for testing
            if (authentication != null && authentication.getPrincipal() instanceof User) {
                User user = (User) authentication.getPrincipal();
                username = user.getUsername();
            }
            
            log.info("Jenkins info requested by {}", username);
            
            var serverInfo = jenkinsApiClient.getMockServerInfo();
            var jobs = jenkinsApiClient.getMockJobs();
            var queue = jenkinsApiClient.getMockBuildQueue();
            
            Map<String, Object> info = Map.of(
                    "server", serverInfo != null ? Map.of(
                            "name", serverInfo.nodeName,
                            "version", serverInfo.version,
                            "description", serverInfo.description
                    ) : Map.of(),
                    "jobs", jobs != null ? jobs.size() : 0,
                    "queueItems", queue != null ? queue.size() : 0,
                    "accessible", jenkinsApiClient.isJenkinsAccessible(),
                    "timestamp", System.currentTimeMillis()
            );
            
            return ResponseEntity.ok(info);
            
        } catch (Exception e) {
            log.error("Error getting Jenkins info", e);
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Error getting Jenkins info: " + e.getMessage()
            ));
        }
    }
    
    /**
     * Get GitHub information
     */
    @GetMapping("/github/info")
    public ResponseEntity<Map<String, Object>> getGitHubInfo(Authentication authentication) {
        try {
            String username = "admin"; // Default for testing
            if (authentication != null && authentication.getPrincipal() instanceof User) {
                User user = (User) authentication.getPrincipal();
                username = user.getUsername();
            }
            
            log.info("GitHub info requested by {}", username);
            
            var repoInfo = gitHubApiClient.getMockRepositoryInfo();
            var branches = gitHubApiClient.getMockBranches();
            var openPRs = gitHubApiClient.getMockPullRequests();
            
            Map<String, Object> info = Map.of(
                    "repository", repoInfo != null ? Map.of(
                            "name", repoInfo.get("name"),
                            "description", repoInfo.get("description"),
                            "defaultBranch", repoInfo.get("default_branch"),
                            "language", repoInfo.get("language"),
                            "openIssues", repoInfo.get("open_issues_count")
                    ) : Map.of(),
                    "branches", branches != null ? branches.size() : 0,
                    "openPullRequests", openPRs != null ? openPRs.size() : 0,
                    "accessible", gitHubApiClient.isGitHubAccessible(),
                    "timestamp", System.currentTimeMillis()
            );
            
            return ResponseEntity.ok(info);
            
        } catch (Exception e) {
            log.error("Error getting GitHub info", e);
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Error getting GitHub info: " + e.getMessage()
            ));
        }
    }
    
    /**
     * Get deployment recommendations
     */
    @PostMapping("/recommendations")
    public ResponseEntity<Map<String, Object>> getDeploymentRecommendations(
            @RequestBody Map<String, String> request,
            Authentication authentication) {
        
        try {
            String username = "admin"; // Default for testing
            if (authentication != null && authentication.getPrincipal() instanceof User) {
                User user = (User) authentication.getPrincipal();
                username = user.getUsername();
            }
            
            String target = request.get("target");
            String branch = request.get("branch");
            
            log.info("Deployment recommendations requested by {} for target: {}, branch: {}", 
                    username, target, branch);
            
            // Analyze the request and provide recommendations
            Map<String, Object> recommendations = analyzeDeploymentRequest(target, branch, username);
            
            return ResponseEntity.ok(recommendations);
            
        } catch (Exception e) {
            log.error("Error getting deployment recommendations", e);
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Error getting recommendations: " + e.getMessage()
            ));
        }
    }
    
    /**
     * Execute deployment with approval
     */
    @PostMapping("/execute")
    public ResponseEntity<Map<String, Object>> executeDeployment(
            @RequestBody Map<String, String> request,
            Authentication authentication) {
        
        try {
            String username = "admin"; // Default for testing
            if (authentication != null && authentication.getPrincipal() instanceof User) {
                User user = (User) authentication.getPrincipal();
                username = user.getUsername();
            }
            
            String deploymentId = request.get("deploymentId");
            String approval = request.get("approval");
            
            log.info("Deployment execution requested by {} for deployment: {} with approval: {}", 
                    username, deploymentId, approval);
            
            if (!"APPROVED".equals(approval)) {
                return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "message", "Deployment approval required"
                ));
            }
            
            // Execute the approved deployment
            Map<String, Object> result = Map.of(
                    "success", true,
                    "message", "Deployment executed successfully",
                    "deploymentId", deploymentId,
                    "approvedBy", username,
                    "timestamp", System.currentTimeMillis()
            );
            
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            log.error("Error executing deployment", e);
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Error executing deployment: " + e.getMessage()
            ));
        }
    }
    
    /**
     * Rollback deployment
     */
    @PostMapping("/rollback")
    public ResponseEntity<Map<String, Object>> rollbackDeployment(
            @RequestBody Map<String, String> request,
            Authentication authentication) {
        
        try {
            String username = "admin"; // Default for testing
            if (authentication != null && authentication.getPrincipal() instanceof User) {
                User user = (User) authentication.getPrincipal();
                username = user.getUsername();
            }
            
            String deploymentId = request.get("deploymentId");
            String reason = request.get("reason");
            
            log.info("Deployment rollback requested by {} for deployment: {} with reason: {}", 
                    username, deploymentId, reason);
            
            // This would typically trigger a rollback process
            Map<String, Object> result = Map.of(
                    "success", true,
                    "message", "Rollback initiated successfully",
                    "deploymentId", deploymentId,
                    "rollbackReason", reason,
                    "initiatedBy", username,
                    "timestamp", System.currentTimeMillis()
            );
            
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            log.error("Error rolling back deployment", e);
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Error rolling back deployment: " + e.getMessage()
            ));
        }
    }
    
    /**
     * Get deployment history
     */
    @GetMapping("/history")
    public ResponseEntity<Map<String, Object>> getDeploymentHistory(
            @RequestParam(defaultValue = "10") int limit,
            @RequestParam(defaultValue = "0") int offset,
            Authentication authentication) {
        
        try {
            String username = "admin"; // Default for testing
            if (authentication != null && authentication.getPrincipal() instanceof User) {
                User user = (User) authentication.getPrincipal();
                username = user.getUsername();
            }
            
            log.info("Deployment history requested by {} with limit: {}, offset: {}", 
                    username, limit, offset);
            
            // This would typically query the deployment history from the database
            // For now, return a mock response
            Map<String, Object> history = Map.of(
                    "deployments", new Object[0], // Empty array for now
                    "total", 0,
                    "limit", limit,
                    "offset", offset,
                    "timestamp", System.currentTimeMillis()
            );
            
            return ResponseEntity.ok(history);
            
        } catch (Exception e) {
            log.error("Error getting deployment history", e);
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Error getting deployment history: " + e.getMessage()
            ));
        }
    }
    
    /**
     * Analyze deployment request and provide recommendations
     */
    private Map<String, Object> analyzeDeploymentRequest(String target, String branch, String username) {
        // This is a simplified analysis - in a real implementation, this would be more sophisticated
        
        Map<String, Object> analysis = Map.of(
                "target", target,
                "branch", branch,
                "recommendations", new String[]{
                    "Ensure all tests pass before deployment",
                    "Verify branch protection rules",
                    "Check for pending pull requests",
                    "Review recent commits for stability"
                },
                "risks", new String[]{
                    "High risk for production deployments without approval",
                    "Feature branches may contain unstable code"
                },
                "suggestedActions", new String[]{
                    "Run full test suite",
                    "Get code review approval",
                    "Verify deployment environment readiness"
                },
                "timestamp", System.currentTimeMillis()
        );
        
        return analysis;
    }
} 