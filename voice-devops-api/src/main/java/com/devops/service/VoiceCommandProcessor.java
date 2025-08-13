package com.devops.service;

import com.devops.entity.*;
import com.devops.repository.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.*;
import java.util.regex.Pattern;

@Service
public class VoiceCommandProcessor {
    
    private static final Logger log = LoggerFactory.getLogger(VoiceCommandProcessor.class);
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private BuildRepository buildRepository;
    
    @Autowired
    private VoiceCommandRepository voiceCommandRepository;
    
    @Autowired
    private MockJenkinsService jenkinsService;
    
    @Autowired
    private MockGitHubService githubService;
    
    @Autowired
    private DeploymentAgentService deploymentAgentService;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    // Command patterns for different roles
    private static final Map<String, List<String>> ADMIN_COMMANDS = Map.of(
        "APPROVE_BUILD", Arrays.asList(
            "approve.*build.*(\\d+)",
            "approve.*build.*(\\w+)",
            "approve.*production.*build",
            "approve.*build.*for.*(\\w+)",
            "approve.*(\\d+)",
            "approve.*(\\w+)",
            "approve.*build"
        ),
        "DEPLOY_PRODUCTION", Arrays.asList(
            "deploy.*(\\w+).*to.*production",
            "deploy.*production.*(\\w+)",
            "release.*(\\w+).*to.*production",
            "deploy.*(\\w+)",
            "production.*deploy.*(\\w+)"
        ),
        "DEPLOY_API", Arrays.asList(
            "deploy.*api.*(\\w+)",
            "deploy.*(\\w+).*api",
            "api.*deploy.*(\\w+)",
            "deploy.*api"
        ),
        "DEPLOY_REWARDS_DETAILS", Arrays.asList(
            "deploy.*rewards.*details",
            "deploy.*rewards.*api",
            "deploy.*rewards.*service",
            "rewards.*deploy",
            "deploy.*rewards"
        ),
        "ABORT_BUILD", Arrays.asList(
            "abort.*build.*(\\d+)",
            "stop.*build.*(\\d+)",
            "cancel.*build.*(\\d+)",
            "abort.*(\\d+)",
            "stop.*build",
            "cancel.*build"
        ),
        "SHOW_APPROVALS", Arrays.asList(
            "show.*pending.*approvals",
            "show.*approvals",
            "list.*approvals",
            "pending.*approvals",
            "approvals"
        ),
        "GENERATE_REPORT", Arrays.asList(
            "generate.*report",
            "create.*report",
            "show.*deployment.*report",
            "deployment.*report",
            "report"
        ),
        "DEPLOYMENT_ORCHESTRATION", Arrays.asList(
            "orchestrate.*deployment.*(\\w+)",
            "smart.*deploy.*(\\w+)",
            "intelligent.*deploy.*(\\w+)",
            "analyze.*and.*deploy.*(\\w+)",
            "orchestrate.*(\\w+)",
            "smart.*deploy",
            "intelligent.*deploy"
        ),
        "DEPLOYMENT_ANALYSIS", Arrays.asList(
            "analyze.*deployment.*(\\w+)",
            "check.*deployment.*readiness.*(\\w+)",
            "deployment.*safety.*check.*(\\w+)",
            "analyze.*(\\w+)",
            "deployment.*analysis",
            "safety.*check"
        )
    );
    
    private static final Map<String, List<String>> USER_COMMANDS = Map.of(
        "BUILD_BRANCH", Arrays.asList(
            "build.*my.*(\\w+).*branch",
            "build.*branch.*(\\w+)",
            "trigger.*build.*(\\w+)",
            "build.*(\\w+)",
            "build.*branch"
        ),
        "CREATE_PR", Arrays.asList(
            "create.*pull.*request.*(\\w+)",
            "create.*pr.*(\\w+)",
            "open.*pull.*request.*(\\w+)",
            "create.*pr",
            "pull.*request"
        ),
        "DEPLOY_STAGING", Arrays.asList(
            "deploy.*(\\w+).*to.*staging",
            "deploy.*staging.*(\\w+)",
            "push.*(\\w+).*to.*staging",
            "deploy.*(\\w+)",
            "staging.*deploy"
        ),
        "SHOW_BUILDS", Arrays.asList(
            "show.*my.*builds",
            "show.*recent.*builds",
            "list.*my.*builds",
            "my.*builds",
            "builds"
        ),
        "CHECK_STATUS", Arrays.asList(
            "check.*build.*status",
            "show.*build.*status",
            "what.*is.*build.*status",
            "build.*status",
            "status"
        )
    );
    
    public CommandResponse processVoiceCommand(String voiceInput, String username) {
        long startTime = System.currentTimeMillis();
        
        try {
            log.info("=== Voice Command Processing Start ===");
            log.info("Input: '{}'", voiceInput);
            log.info("Username: '{}'", username);
            
            // Find user
            User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found: " + username));
            
            log.info("User found: {} (Role: {})", user.getUsername(), user.getRole());
            
            // Parse command first
            log.info("Parsing voice command...");
            CommandIntent intent = parseVoiceCommand(voiceInput, user.getRole());
            
            // Create voice command record
            VoiceCommand voiceCommand = new VoiceCommand();
            voiceCommand.setOriginalText(voiceInput);
            voiceCommand.setUser(user);
            voiceCommand.setStatus(VoiceCommand.CommandStatus.PROCESSING);
            voiceCommand.setCreatedAt(LocalDateTime.now());
            
            if (intent == null) {
                log.warn("Command not recognized: '{}'", voiceInput);
                voiceCommand.setStatus(VoiceCommand.CommandStatus.INVALID);
                voiceCommand.setCommandType("UNRECOGNIZED"); // Set a default command type
                voiceCommand.setResponse("Command not recognized. Please try again.");
                voiceCommandRepository.save(voiceCommand);
                return new CommandResponse("Command not recognized. Please try again.", false);
            }
            
            log.info("Command parsed successfully: Action={}, Parameters={}", intent.getAction(), intent.getParameters());
            
            // Execute command based on role
            CommandResponse response;
            if (user.getRole() == User.UserRole.ADMIN) {
                log.info("Processing as ADMIN command");
                response = processAdminCommand(intent, user);
            } else {
                log.info("Processing as USER command");
                response = processUserCommand(intent, user);
            }
            
            log.info("Command executed successfully: {}", response.getMessage());
            
            // Update voice command record
            voiceCommand.setProcessedText(intent.getAction());
            voiceCommand.setCommandType(intent.getAction());
            voiceCommand.setParameters(objectMapper.writeValueAsString(intent.getParameters()));
            voiceCommand.setStatus(VoiceCommand.CommandStatus.COMPLETED);
            voiceCommand.setResponse(response.getMessage());
            voiceCommand.setExecutionTimeMs(System.currentTimeMillis() - startTime);
            voiceCommand.setProcessedAt(LocalDateTime.now());
            voiceCommand.setConfidenceScore(0.9); // Mock confidence score
            
            voiceCommandRepository.save(voiceCommand);
            
            log.info("=== Voice Command Processing Complete ===");
            return response;
            
        } catch (Exception e) {
            log.error("Error processing voice command: '{}'", voiceInput, e);
            return new CommandResponse("Error processing command: " + e.getMessage(), false);
        }
    }
    
    private CommandIntent parseVoiceCommand(String voiceInput, User.UserRole role) {
        String normalizedInput = voiceInput.toLowerCase().trim();
        Map<String, List<String>> commands = (role == User.UserRole.ADMIN) ? ADMIN_COMMANDS : USER_COMMANDS;
        
        log.info("Parsing command: '{}' for role: {}", normalizedInput, role);
        log.info("Available commands for role {}: {}", role, commands.keySet());
        
        for (Map.Entry<String, List<String>> entry : commands.entrySet()) {
            String action = entry.getKey();
            List<String> patterns = entry.getValue();
            
            log.debug("Checking action: {} with {} patterns", action, patterns.size());
            
            for (String pattern : patterns) {
                log.debug("Testing pattern: '{}' against input: '{}'", pattern, normalizedInput);
                
                if (Pattern.matches(pattern, normalizedInput)) {
                    log.info("Pattern matched! Action: {}, Pattern: '{}'", action, pattern);
                    Map<String, String> parameters = extractParameters(normalizedInput, pattern);
                    log.info("Extracted parameters: {}", parameters);
                    return new CommandIntent(action, parameters);
                }
            }
        }
        
        log.warn("No patterns matched for input: '{}'", normalizedInput);
        return null;
    }
    
    private Map<String, String> extractParameters(String input, String pattern) {
        Map<String, String> parameters = new HashMap<>();
        
        // Extract build ID for approve build commands
        if (input.contains("approve") && input.contains("build")) {
            // Look for build ID after "approve build"
            java.util.regex.Pattern p = java.util.regex.Pattern.compile("approve\\s+build\\s+(\\S+)");
            java.util.regex.Matcher m = p.matcher(input);
            if (m.find()) {
                parameters.put("buildId", m.group(1));
            }
        }
        
        // Extract parameters based on pattern
        if (pattern.contains("(\\d+)")) {
            // Extract build number
            java.util.regex.Pattern p = java.util.regex.Pattern.compile("(\\d+)");
            java.util.regex.Matcher m = p.matcher(input);
            if (m.find()) {
                parameters.put("buildId", m.group(1));
            }
        }
        
        if (pattern.contains("(\\w+)")) {
            // Extract branch name, API name, or other text
            java.util.regex.Pattern p = java.util.regex.Pattern.compile("(\\w+)");
            java.util.regex.Matcher m = p.matcher(input);
            if (m.find()) {
                String value = m.group(1);
                // Determine parameter type based on context
                if (input.contains("api")) {
                    parameters.put("apiName", value);
                } else if (input.contains("production") || input.contains("staging")) {
                    parameters.put("target", value);
                } else if (input.contains("build") && !input.contains("approve")) {
                    parameters.put("branch", value);
                } else if (input.contains("deploy")) {
                    parameters.put("branch", value);
                } else {
                    // Default to branch for most cases
                    parameters.put("branch", value);
                }
            }
        }
        
        // Extract target environment if mentioned
        if (input.contains("production")) {
            parameters.put("target", "production");
        } else if (input.contains("staging")) {
            parameters.put("target", "staging");
        }
        
        // Extract branch if mentioned
        if (input.contains("main") || input.contains("master")) {
            parameters.put("branch", "main");
        } else if (input.contains("develop")) {
            parameters.put("branch", "develop");
        }
        
        return parameters;
    }
    
    private CommandResponse processAdminCommand(CommandIntent intent, User user) {
        switch (intent.getAction()) {
            case "APPROVE_BUILD":
                return jenkinsService.approveBuild(intent.getParameters(), user);
            case "DEPLOY_PRODUCTION":
                return jenkinsService.deployToProduction(intent.getParameters(), user);
            case "DEPLOY_API":
                return jenkinsService.deployApi(intent.getParameters(), user);
            case "DEPLOY_REWARDS_DETAILS":
                return deployRewardsDetailsAPI(user);
            case "ABORT_BUILD":
                return jenkinsService.abortBuild(intent.getParameters(), user);
            case "SHOW_APPROVALS":
                return jenkinsService.getPendingApprovals();
            case "GENERATE_REPORT":
                return generateDeploymentReport();
            case "DEPLOYMENT_ORCHESTRATION":
                return handleDeploymentOrchestration(intent.getParameters(), user);
            case "DEPLOYMENT_ANALYSIS":
                return handleDeploymentAnalysis(intent.getParameters(), user);
            default:
                return new CommandResponse("Unknown admin command: " + intent.getAction(), false);
        }
    }
    
    private CommandResponse processUserCommand(CommandIntent intent, User user) {
        switch (intent.getAction()) {
            case "BUILD_BRANCH":
                return jenkinsService.buildBranch(intent.getParameters(), user);
            case "CREATE_PR":
                return githubService.createPullRequest(intent.getParameters(), user);
            case "DEPLOY_STAGING":
                return jenkinsService.deployToStaging(intent.getParameters(), user);
            case "SHOW_BUILDS":
                return jenkinsService.getUserBuilds(user);
            case "CHECK_STATUS":
                return jenkinsService.getBuildStatus(intent.getParameters(), user);
            default:
                return new CommandResponse("Unknown user command: " + intent.getAction(), false);
        }
    }
    
    private CommandResponse generateDeploymentReport() {
        // Mock report generation
        return new CommandResponse("Deployment report generated successfully. Check your email.", true);
    }
    
    private CommandResponse deployRewardsDetailsAPI(User user) {
        try {
            // Create a new build for Rewards Details API deployment
            Build build = new Build();
            build.setJenkinsBuildId("API-" + System.currentTimeMillis());
            build.setJobName("api-deploy");
            build.setBranchName("rewards-api-v1");
            build.setBuildNumber((int) (System.currentTimeMillis() % 10000));
            build.setStatus(Build.BuildStatus.RUNNING);
            build.setEnvironment("production");
            build.setRequiresApproval(false);
            build.setTriggeredBy(user);
            build.setApiName("Rewards Details API");
            build.setDeploymentProgress(0);
            build.setBuildUrl("http://mock-jenkins.company.com/job/api-deploy/" + build.getBuildNumber());
            build.setStartedAt(LocalDateTime.now());
            
            buildRepository.save(build);
            
            log.info("Rewards Details API deployment triggered by {}", user.getUsername());
            
            // Start progress simulation in background (5 seconds)
            simulateRewardsAPIDeploymentProgress(build);
            
            return new CommandResponse(
                "Rewards Details API deployment started successfully. Build ID: " + build.getJenkinsBuildId() + 
                ". Progress tracking enabled. Deployment will complete in 5 seconds.", true);
                
        } catch (Exception e) {
            log.error("Error deploying Rewards Details API", e);
            return new CommandResponse("Error deploying Rewards Details API: " + e.getMessage(), false);
        }
    }
    
    private void simulateRewardsAPIDeploymentProgress(Build build) {
        new Thread(() -> {
            try {
                for (int progress = 0; progress <= 100; progress += 5) {
                    Thread.sleep(250); // 250ms intervals for 5 seconds total
                    build.setDeploymentProgress(progress);
                    buildRepository.save(build);
                    
                    if (progress == 100) {
                        build.setStatus(Build.BuildStatus.SUCCESS);
                        build.setCompletedAt(LocalDateTime.now());
                        build.setDurationSeconds(5L); // 5 seconds total
                        buildRepository.save(build);
                        log.info("Rewards Details API deployment completed successfully");
                    }
                }
            } catch (InterruptedException e) {
                log.error("Rewards API deployment progress simulation interrupted", e);
                Thread.currentThread().interrupt();
            }
        }).start();
    }
    
    /**
     * Handle deployment orchestration using the intelligent deployment agent
     */
    private CommandResponse handleDeploymentOrchestration(Map<String, String> parameters, User user) {
        try {
            log.info("Deployment orchestration requested by {} with parameters: {}", 
                    user.getUsername(), parameters);
            
            // Use the deployment agent service for intelligent deployment
            return deploymentAgentService.orchestrateDeployment(parameters, user);
            
        } catch (Exception e) {
            log.error("Error in deployment orchestration", e);
            return new CommandResponse("Deployment orchestration failed: " + e.getMessage(), false);
        }
    }
    
    /**
     * Handle deployment analysis and safety checks
     */
    private CommandResponse handleDeploymentAnalysis(Map<String, String> parameters, User user) {
        try {
            String target = parameters.get("target");
            String branch = parameters.get("branch");
            
            log.info("Deployment analysis requested by {} for target: {}, branch: {}", 
                    user.getUsername(), target, branch);
            
            // This would typically analyze deployment readiness
            // For now, return a mock analysis
            StringBuilder analysis = new StringBuilder();
            analysis.append("Deployment Analysis Results:\n");
            analysis.append("• Target: ").append(target != null ? target : "Not specified").append("\n");
            analysis.append("• Branch: ").append(branch != null ? branch : "Not specified").append("\n");
            analysis.append("• Safety Score: 85/100\n");
            analysis.append("• Recommendations:\n");
            analysis.append("  - Run full test suite\n");
            analysis.append("  - Verify branch protection rules\n");
            analysis.append("  - Check for pending pull requests\n");
            analysis.append("  - Review recent commits\n");
            analysis.append("• Status: Ready for deployment with caution");
            
            return new CommandResponse(analysis.toString(), true);
            
        } catch (Exception e) {
            log.error("Error in deployment analysis", e);
            return new CommandResponse("Deployment analysis failed: " + e.getMessage(), false);
        }
    }
    
    // Inner classes for command processing
    public static class CommandIntent {
        private String action;
        private Map<String, String> parameters;
        
        public CommandIntent(String action, Map<String, String> parameters) {
            this.action = action;
            this.parameters = parameters;
        }
        
        public String getAction() { return action; }
        public Map<String, String> getParameters() { return parameters; }
    }
    
    public static class CommandResponse {
        private String message;
        private boolean success;
        
        public CommandResponse(String message, boolean success) {
            this.message = message;
            this.success = success;
        }
        
        public String getMessage() { return message; }
        public boolean isSuccess() { return success; }
    }
} 