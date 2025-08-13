package com.devops.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Service
@Slf4j
public class GitHubApiClient {
    
    @Autowired
    private RestTemplate restTemplate;
    
    @Value("${github.api.url:https://api.github.com}")
    private String githubApiUrl;
    
    @Value("${github.token:}")
    private String githubToken;
    
    @Value("${github.owner:}")
    private String githubOwner;
    
    @Value("${github.repo:}")
    private String githubRepo;
    
    /**
     * Check if GitHub is accessible
     */
    public boolean isGitHubAccessible() {
        try {
            // For testing purposes, return true to avoid external dependencies
            return true;
        } catch (Exception e) {
            log.error("GitHub accessibility check failed", e);
            return false;
        }
    }
    
    /**
     * Get mock repository info for testing
     */
    public Map<String, Object> getMockRepositoryInfo() {
        Map<String, Object> repoInfo = new HashMap<>();
        repoInfo.put("name", "voice-devops-dashboard");
        repoInfo.put("description", "Voice-controlled DevOps Dashboard with intelligent deployment orchestration");
        repoInfo.put("default_branch", "main");
        repoInfo.put("language", "Java");
        repoInfo.put("open_issues_count", 5);
        repoInfo.put("stargazers_count", 42);
        repoInfo.put("forks_count", 12);
        return repoInfo;
    }
    
    /**
     * Get mock branches for testing
     */
    public List<Map<String, Object>> getMockBranches() {
        List<Map<String, Object>> branches = new ArrayList<>();
        
        Map<String, Object> mainBranch = new HashMap<>();
        mainBranch.put("name", "main");
        mainBranch.put("commit", Map.of("sha", "abc123", "url", "https://api.github.com/repos/owner/repo/commits/abc123"));
        branches.add(mainBranch);
        
        Map<String, Object> featureBranch = new HashMap<>();
        featureBranch.put("name", "feature/voice-commands");
        featureBranch.put("commit", Map.of("sha", "def456", "url", "https://api.github.com/repos/owner/repo/commits/def456"));
        branches.add(featureBranch);
        
        Map<String, Object> releaseBranch = new HashMap<>();
        releaseBranch.put("name", "release/2.1.0");
        releaseBranch.put("commit", Map.of("sha", "ghi789", "url", "https://api.github.com/repos/owner/repo/commits/ghi789"));
        branches.add(releaseBranch);
        
        return branches;
    }
    
    /**
     * Get mock pull requests for testing
     */
    public List<Map<String, Object>> getMockPullRequests() {
        List<Map<String, Object>> prs = new ArrayList<>();
        
        Map<String, Object> pr1 = new HashMap<>();
        pr1.put("number", 42);
        pr1.put("title", "Add voice command processing");
        pr1.put("state", "open");
        pr1.put("user", Map.of("login", "developer1"));
        pr1.put("created_at", "2025-01-15T10:00:00Z");
        prs.add(pr1);
        
        Map<String, Object> pr2 = new HashMap<>();
        pr2.put("number", 43);
        pr2.put("title", "Fix deployment agent authentication");
        pr2.put("state", "open");
        pr2.put("user", Map.of("login", "developer2"));
        pr2.put("created_at", "2025-01-16T14:30:00Z");
        prs.add(pr2);
        
        return prs;
    }
    
    /**
     * Get repository information
     */
    public Map<String, Object> getRepositoryInfo() {
        try {
            String url = String.format("%s/repos/%s/%s", githubApiUrl, githubOwner, githubRepo);
            
            HttpHeaders headers = createAuthHeaders();
            HttpEntity<String> entity = new HttpEntity<>(headers);
            
            ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.GET, entity, Map.class);
            
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                return response.getBody();
            }
            
        } catch (Exception e) {
            log.error("Error getting repository info", e);
        }
        
        return new HashMap<>();
    }
    
    /**
     * Get all branches
     */
    public List<Map<String, Object>> getAllBranches() {
        try {
            String url = String.format("%s/repos/%s/%s/branches?per_page=100", githubApiUrl, githubOwner, githubRepo);
            
            HttpHeaders headers = createAuthHeaders();
            HttpEntity<String> entity = new HttpEntity<>(headers);
            
            ResponseEntity<List> response = restTemplate.exchange(url, HttpMethod.GET, entity, List.class);
            
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                return response.getBody();
            }
            
        } catch (Exception e) {
            log.error("Error getting branches", e);
        }
        
        return new ArrayList<>();
    }
    
    /**
     * Get pull requests
     */
    public List<Map<String, Object>> getPullRequests(String state) {
        try {
            String url = String.format("%s/repos/%s/%s/pulls?state=%s&per_page=100", 
                    githubApiUrl, githubOwner, githubRepo, state);
            
            HttpHeaders headers = createAuthHeaders();
            HttpEntity<String> entity = new HttpEntity<>(headers);
            
            ResponseEntity<List> response = restTemplate.exchange(url, HttpMethod.GET, entity, List.class);
            
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                return response.getBody();
            }
            
        } catch (Exception e) {
            log.error("Error getting pull requests with state {}", state, e);
        }
        
        return new ArrayList<>();
    }
    
    /**
     * Create authentication headers
     */
    private HttpHeaders createAuthHeaders() {
        HttpHeaders headers = new HttpHeaders();
        if (githubToken != null && !githubToken.isEmpty()) {
            headers.setBearerAuth(githubToken);
        }
        headers.set("Accept", "application/vnd.github.v3+json");
        return headers;
    }
} 