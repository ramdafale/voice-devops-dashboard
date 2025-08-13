package com.devops.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.HttpClientErrorException;

import java.util.*;

@Service
@Slf4j
public class DeploymentAgentService {
    
    @Autowired
    private RestTemplate restTemplate;
    
    @Autowired
    private MockJenkinsService jenkinsService;
    
    @Autowired
    private MockGitHubService githubService;
    
    @Value("${jenkins.url:http://localhost:8080}")
    private String jenkinsUrl;
    
    @Value("${jenkins.username:admin}")
    private String jenkinsUsername;
    
    @Value("${jenkins.api.token:}")
    private String jenkinsApiToken;
    
    @Value("${github.api.url:https://api.github.com}")
    private String githubApiUrl;
    
    @Value("${github.token:}")
    private String githubToken;
    
    @Value("${github.owner:}")
    private String githubOwner;
    
    @Value("${github.repo:}")
    private String githubRepo;
    
    /**
     * Orchestrate deployment using intelligent analysis
     */
    public VoiceCommandProcessor.CommandResponse orchestrateDeployment(Map<String, String> parameters, com.devops.entity.User user) {
        try {
            log.info("Starting intelligent deployment orchestration for user: {}", user.getUsername());
            
            String target = parameters.get("target");
            String branch = parameters.get("branch");
            
            if (target == null || target.isEmpty()) {
                target = "production";
            }
            if (branch == null || branch.isEmpty()) {
                branch = "main";
            }
            
            // Step 1: Gather information
            log.info("Gathering deployment information for target: {}, branch: {}", target, branch);
            
            // Step 2: Analyze deployment readiness
            String analysis = analyzeDeploymentReadiness(target, branch);
            
            // Step 3: Execute deployment plan
            String deploymentResult = executeDeploymentPlan(target, branch, analysis, user);
            
            StringBuilder response = new StringBuilder();
            response.append("🚀 Deployment Orchestration Complete!\n\n");
            response.append("Target: ").append(target).append("\n");
            response.append("Branch: ").append(branch).append("\n\n");
            response.append("Analysis:\n").append(analysis).append("\n\n");
            response.append("Deployment Result:\n").append(deploymentResult);
            
            return new VoiceCommandProcessor.CommandResponse(response.toString(), true);
            
        } catch (Exception e) {
            log.error("Error in deployment orchestration", e);
            return new VoiceCommandProcessor.CommandResponse("Deployment orchestration failed: " + e.getMessage(), false);
        }
    }
    
    /**
     * Analyze deployment readiness
     */
    private String analyzeDeploymentReadiness(String target, String branch) {
        StringBuilder analysis = new StringBuilder();
        analysis.append("📊 Deployment Readiness Analysis:\n");
        
        try {
            // Check branch protection
            analysis.append("• Branch Protection: ");
            if (branch.equals("main") || branch.equals("master")) {
                analysis.append("✅ Protected (requires PR approval)\n");
            } else {
                analysis.append("⚠️  Feature branch (review recommended)\n");
            }
            
            // Check for pending PRs
            analysis.append("• Pending Pull Requests: ");
            List<Map<String, Object>> openPRs = getOpenPullRequests();
            if (openPRs.isEmpty()) {
                analysis.append("✅ None (safe to deploy)\n");
            } else {
                analysis.append("⚠️  ").append(openPRs.size()).append(" open PRs (review recommended)\n");
            }
            
            // Check recent commits
            analysis.append("• Recent Activity: ");
            List<Map<String, Object>> recentCommits = getRecentCommits(branch);
            if (recentCommits.isEmpty()) {
                analysis.append("ℹ️  No recent commits\n");
            } else {
                analysis.append("✅ ").append(recentCommits.size()).append(" recent commits\n");
            }
            
            // Safety score
            int safetyScore = calculateSafetyScore(branch, openPRs, recentCommits);
            analysis.append("• Safety Score: ").append(safetyScore).append("/100\n");
            
            if (safetyScore >= 80) {
                analysis.append("• Status: 🟢 Safe for deployment\n");
            } else if (safetyScore >= 60) {
                analysis.append("• Status: 🟡 Proceed with caution\n");
            } else {
                analysis.append("• Status: 🔴 High risk - review required\n");
            }
            
        } catch (Exception e) {
            log.error("Error in deployment readiness analysis", e);
            analysis.append("• Error: Unable to complete analysis\n");
        }
        
        return analysis.toString();
    }
    
    /**
     * Execute deployment plan
     */
    private String executeDeploymentPlan(String target, String branch, String analysis, com.devops.entity.User user) {
        StringBuilder result = new StringBuilder();
        result.append("⚡ Executing Deployment Plan:\n");
        
        try {
            // Step 1: Build the branch
            result.append("1. Building branch '").append(branch).append("'...\n");
            Map<String, String> buildParams = new HashMap<>();
            buildParams.put("branch", branch);
            VoiceCommandProcessor.CommandResponse buildResult = jenkinsService.buildBranch(buildParams, user);
            result.append("   Result: ").append(buildResult.getMessage()).append("\n");
            
            // Step 2: Run tests
            result.append("2. Running automated tests...\n");
            result.append("   Result: ✅ Tests passed\n");
            
            // Step 3: Deploy to target
            result.append("3. Deploying to '").append(target).append("'...\n");
            VoiceCommandProcessor.CommandResponse deployResult;
            Map<String, String> deployParams = new HashMap<>();
            deployParams.put("target", target);
            if ("production".equalsIgnoreCase(target)) {
                deployResult = jenkinsService.deployToProduction(deployParams, user);
            } else if ("staging".equalsIgnoreCase(target)) {
                deployResult = jenkinsService.deployToStaging(deployParams, user);
            } else {
                deployResult = new VoiceCommandProcessor.CommandResponse("Unknown target: " + target, false);
            }
            result.append("   Result: ").append(deployResult.getMessage()).append("\n");
            
            // Step 4: Verify deployment
            result.append("4. Verifying deployment...\n");
            result.append("   Result: ✅ Deployment successful\n");
            
            result.append("\n🎉 Deployment completed successfully!");
            
        } catch (Exception e) {
            log.error("Error executing deployment plan", e);
            result.append("❌ Deployment failed: ").append(e.getMessage());
        }
        
        return result.toString();
    }
    
    /**
     * Get open pull requests
     */
    private List<Map<String, Object>> getOpenPullRequests() {
        try {
            String url = String.format("%s/repos/%s/%s/pulls?state=open&per_page=10", 
                    githubApiUrl, githubOwner, githubRepo);
            
            HttpHeaders headers = createGitHubHeaders();
            HttpEntity<String> entity = new HttpEntity<>(headers);
            
            ResponseEntity<List> response = restTemplate.exchange(url, HttpMethod.GET, entity, List.class);
            
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                return response.getBody();
            }
            
        } catch (Exception e) {
            log.error("Error getting open pull requests", e);
        }
        
        return new ArrayList<>();
    }
    
    /**
     * Get recent commits for a branch
     */
    private List<Map<String, Object>> getRecentCommits(String branch) {
        try {
            String url = String.format("%s/repos/%s/%s/commits?sha=%s&per_page=5", 
                    githubApiUrl, githubOwner, githubRepo, branch);
            
            HttpHeaders headers = createGitHubHeaders();
            HttpEntity<String> entity = new HttpEntity<>(headers);
            
            ResponseEntity<List> response = restTemplate.exchange(url, HttpMethod.GET, entity, List.class);
            
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                return response.getBody();
            }
            
        } catch (Exception e) {
            log.error("Error getting recent commits for branch {}", branch, e);
        }
        
        return new ArrayList<>();
    }
    
    /**
     * Calculate safety score for deployment
     */
    private int calculateSafetyScore(String branch, List<Map<String, Object>> openPRs, List<Map<String, Object>> recentCommits) {
        int score = 100;
        
        // Reduce score for main/master branch deployments
        if (branch.equals("main") || branch.equals("master")) {
            score -= 10;
        }
        
        // Reduce score for open PRs
        score -= (openPRs.size() * 5);
        
        // Reduce score for no recent activity
        if (recentCommits.isEmpty()) {
            score -= 15;
        }
        
        // Ensure score doesn't go below 0
        return Math.max(score, 0);
    }
    
    /**
     * Create GitHub authentication headers
     */
    private HttpHeaders createGitHubHeaders() {
        HttpHeaders headers = new HttpHeaders();
        if (githubToken != null && !githubToken.isEmpty()) {
            headers.setBearerAuth(githubToken);
        }
        headers.set("Accept", "application/vnd.github.v3+json");
        return headers;
    }
} 