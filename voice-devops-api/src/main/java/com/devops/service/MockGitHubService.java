package com.devops.service;

import com.devops.entity.User;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class MockGitHubService {
    
    private static final Logger log = LoggerFactory.getLogger(MockGitHubService.class);
    
    private final AtomicInteger prCounter = new AtomicInteger(100);
    private final Map<String, PullRequest> mockPullRequests = new HashMap<>();
    
    public VoiceCommandProcessor.CommandResponse createPullRequest(Map<String, String> parameters, User user) {
        try {
            String branch = parameters.get("branch");
            if (branch == null) {
                return new VoiceCommandProcessor.CommandResponse("Branch not specified", false);
            }
            
            // Create a mock pull request
            String prId = "PR-" + prCounter.incrementAndGet();
            PullRequest pr = new PullRequest();
            pr.setId(prId);
            pr.setTitle("Feature: " + branch + " branch");
            pr.setSourceBranch(branch);
            pr.setTargetBranch("develop");
            pr.setAuthor(user.getUsername());
            pr.setStatus("OPEN");
            pr.setCreatedAt(LocalDateTime.now());
            pr.setUrl("http://mock-github.company.com/pull/" + prId);
            pr.setDescription("Pull request created via voice command for branch: " + branch);
            
            mockPullRequests.put(prId, pr);
            
            log.info("Pull request created for branch {} by {}", branch, user.getUsername());
            
            return new VoiceCommandProcessor.CommandResponse(
                "Pull request created successfully! PR ID: " + prId + 
                " - Branch: " + branch + " → develop", true);
                
        } catch (Exception e) {
            log.error("Error creating pull request", e);
            return new VoiceCommandProcessor.CommandResponse("Error creating pull request: " + e.getMessage(), false);
        }
    }
    
    public VoiceCommandProcessor.CommandResponse getUserPullRequests(User user) {
        try {
            List<PullRequest> userPRs = mockPullRequests.values().stream()
                .filter(pr -> pr.getAuthor().equals(user.getUsername()))
                .sorted((pr1, pr2) -> pr2.getCreatedAt().compareTo(pr1.getCreatedAt()))
                .toList();
            
            if (userPRs.isEmpty()) {
                return new VoiceCommandProcessor.CommandResponse("No pull requests found for your account.", true);
            }
            
            StringBuilder response = new StringBuilder("Your recent pull requests:\n");
            for (PullRequest pr : userPRs.subList(0, Math.min(5, userPRs.size()))) {
                response.append("• ").append(pr.getId())
                       .append(" - ").append(pr.getTitle())
                       .append(" (").append(pr.getStatus()).append(")\n");
            }
            
            return new VoiceCommandProcessor.CommandResponse(response.toString(), true);
            
        } catch (Exception e) {
            log.error("Error getting user pull requests", e);
            return new VoiceCommandProcessor.CommandResponse("Error getting pull requests: " + e.getMessage(), false);
        }
    }
    
    public VoiceCommandProcessor.CommandResponse mergePullRequest(Map<String, String> parameters, User user) {
        try {
            String prId = parameters.get("prId");
            if (prId == null) {
                return new VoiceCommandProcessor.CommandResponse("Pull request ID not specified", false);
            }
            
            PullRequest pr = mockPullRequests.get(prId);
            if (pr == null) {
                return new VoiceCommandProcessor.CommandResponse("Pull request not found: " + prId, false);
            }
            
            if (!"OPEN".equals(pr.getStatus())) {
                return new VoiceCommandProcessor.CommandResponse("Pull request is not open for merging", false);
            }
            
            // Merge the pull request
            pr.setStatus("MERGED");
            pr.setMergedAt(LocalDateTime.now());
            pr.setMergedBy(user.getUsername());
            
            log.info("Pull request {} merged by {}", prId, user.getUsername());
            
            return new VoiceCommandProcessor.CommandResponse(
                "Pull request " + prId + " merged successfully!", true);
                
        } catch (Exception e) {
            log.error("Error merging pull request", e);
            return new VoiceCommandProcessor.CommandResponse("Error merging pull request: " + e.getMessage(), false);
        }
    }
    
    public VoiceCommandProcessor.CommandResponse getBranchStatus(Map<String, String> parameters, User user) {
        try {
            String branch = parameters.get("branch");
            if (branch == null) {
                return new VoiceCommandProcessor.CommandResponse("Branch not specified", false);
            }
            
            // Mock branch status
            List<PullRequest> branchPRs = mockPullRequests.values().stream()
                .filter(pr -> pr.getSourceBranch().equals(branch))
                .toList();
            
            if (branchPRs.isEmpty()) {
                return new VoiceCommandProcessor.CommandResponse(
                    "Branch " + branch + " has no open pull requests.", true);
            }
            
            StringBuilder response = new StringBuilder("Branch " + branch + " status:\n");
            for (PullRequest pr : branchPRs) {
                response.append("• ").append(pr.getId())
                       .append(" - ").append(pr.getStatus())
                       .append(" (created by ").append(pr.getAuthor()).append(")\n");
            }
            
            return new VoiceCommandProcessor.CommandResponse(response.toString(), true);
            
        } catch (Exception e) {
            log.error("Error getting branch status", e);
            return new VoiceCommandProcessor.CommandResponse("Error getting branch status: " + e.getMessage(), false);
        }
    }
    
    // Inner class for mock pull requests
    public static class PullRequest {
        private String id;
        private String title;
        private String sourceBranch;
        private String targetBranch;
        private String author;
        private String status;
        private LocalDateTime createdAt;
        private LocalDateTime mergedAt;
        private String mergedBy;
        private String url;
        private String description;
        
        // Getters and setters
        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        
        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        
        public String getSourceBranch() { return sourceBranch; }
        public void setSourceBranch(String sourceBranch) { this.sourceBranch = sourceBranch; }
        
        public String getTargetBranch() { return targetBranch; }
        public void setTargetBranch(String targetBranch) { this.targetBranch = targetBranch; }
        
        public String getAuthor() { return author; }
        public void setAuthor(String author) { this.author = author; }
        
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        
        public LocalDateTime getCreatedAt() { return createdAt; }
        public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
        
        public LocalDateTime getMergedAt() { return mergedAt; }
        public void setMergedAt(LocalDateTime mergedAt) { this.mergedAt = mergedAt; }
        
        public String getMergedBy() { return mergedBy; }
        public void setMergedBy(String mergedBy) { this.mergedBy = mergedBy; }
        
        public String getUrl() { return url; }
        public void setUrl(String url) { this.url = url; }
        
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
    }
} 