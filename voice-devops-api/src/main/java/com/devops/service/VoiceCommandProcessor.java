package com.devops.service;

import com.devops.entity.*;
import com.devops.repository.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.*;
import java.util.regex.Pattern;

@Service
@Slf4j
public class VoiceCommandProcessor {
    
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
    private ObjectMapper objectMapper;
    
    // Command patterns for different roles
    private static final Map<String, List<String>> ADMIN_COMMANDS = Map.of(
        "APPROVE_BUILD", Arrays.asList(
            "approve.*build.*(\\d+)",
            "approve.*production.*build",
            "approve.*build.*for.*(\\w+)"
        ),
        "DEPLOY_PRODUCTION", Arrays.asList(
            "deploy.*(\\w+).*to.*production",
            "deploy.*production.*(\\w+)",
            "release.*(\\w+).*to.*production"
        ),
        "ABORT_BUILD", Arrays.asList(
            "abort.*build.*(\\d+)",
            "stop.*build.*(\\d+)",
            "cancel.*build.*(\\d+)"
        ),
        "SHOW_APPROVALS", Arrays.asList(
            "show.*pending.*approvals",
            "show.*approvals",
            "list.*approvals"
        ),
        "GENERATE_REPORT", Arrays.asList(
            "generate.*report",
            "create.*report",
            "show.*deployment.*report"
        )
    );
    
    private static final Map<String, List<String>> USER_COMMANDS = Map.of(
        "BUILD_BRANCH", Arrays.asList(
            "build.*my.*(\\w+).*branch",
            "build.*branch.*(\\w+)",
            "trigger.*build.*(\\w+)"
        ),
        "CREATE_PR", Arrays.asList(
            "create.*pull.*request.*(\\w+)",
            "create.*pr.*(\\w+)",
            "open.*pull.*request.*(\\w+)"
        ),
        "DEPLOY_STAGING", Arrays.asList(
            "deploy.*(\\w+).*to.*staging",
            "deploy.*staging.*(\\w+)",
            "push.*(\\w+).*to.*staging"
        ),
        "SHOW_BUILDS", Arrays.asList(
            "show.*my.*builds",
            "show.*recent.*builds",
            "list.*my.*builds"
        ),
        "CHECK_STATUS", Arrays.asList(
            "check.*build.*status",
            "show.*build.*status",
            "what.*is.*build.*status"
        )
    );
    
    public CommandResponse processVoiceCommand(String voiceInput, String username) {
        long startTime = System.currentTimeMillis();
        
        try {
            // Find user
            User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found: " + username));
            
            // Create voice command record
            VoiceCommand voiceCommand = new VoiceCommand();
            voiceCommand.setOriginalText(voiceInput);
            voiceCommand.setUser(user);
            voiceCommand.setStatus(VoiceCommand.CommandStatus.PROCESSING);
            voiceCommand.setCreatedAt(LocalDateTime.now());
            
            voiceCommandRepository.save(voiceCommand);
            
            // Parse command
            CommandIntent intent = parseVoiceCommand(voiceInput, user.getRole());
            
            if (intent == null) {
                voiceCommand.setStatus(VoiceCommand.CommandStatus.INVALID);
                voiceCommand.setResponse("Command not recognized. Please try again.");
                voiceCommandRepository.save(voiceCommand);
                return new CommandResponse("Command not recognized. Please try again.", false);
            }
            
            // Execute command based on role
            CommandResponse response;
            if (user.getRole() == User.UserRole.ADMIN) {
                response = processAdminCommand(intent, user);
            } else {
                response = processUserCommand(intent, user);
            }
            
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
            
            return response;
            
        } catch (Exception e) {
            log.error("Error processing voice command: " + voiceInput, e);
            return new CommandResponse("Error processing command: " + e.getMessage(), false);
        }
    }
    
    private CommandIntent parseVoiceCommand(String voiceInput, User.UserRole role) {
        String normalizedInput = voiceInput.toLowerCase().trim();
        Map<String, List<String>> commands = (role == User.UserRole.ADMIN) ? ADMIN_COMMANDS : USER_COMMANDS;
        
        for (Map.Entry<String, List<String>> entry : commands.entrySet()) {
            String action = entry.getKey();
            List<String> patterns = entry.getValue();
            
            for (String pattern : patterns) {
                if (Pattern.matches(pattern, normalizedInput)) {
                    Map<String, String> parameters = extractParameters(normalizedInput, pattern);
                    return new CommandIntent(action, parameters);
                }
            }
        }
        
        return null;
    }
    
    private Map<String, String> extractParameters(String input, String pattern) {
        Map<String, String> parameters = new HashMap<>();
        
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
            // Extract branch name or other text
            java.util.regex.Pattern p = java.util.regex.Pattern.compile("(\\w+)");
            java.util.regex.Matcher m = p.matcher(input);
            if (m.find()) {
                parameters.put("branch", m.group(1));
            }
        }
        
        return parameters;
    }
    
    private CommandResponse processAdminCommand(CommandIntent intent, User user) {
        switch (intent.getAction()) {
            case "APPROVE_BUILD":
                return jenkinsService.approveBuild(intent.getParameters(), user);
            case "DEPLOY_PRODUCTION":
                return jenkinsService.deployToProduction(intent.getParameters(), user);
            case "ABORT_BUILD":
                return jenkinsService.abortBuild(intent.getParameters(), user);
            case "SHOW_APPROVALS":
                return jenkinsService.getPendingApprovals();
            case "GENERATE_REPORT":
                return generateDeploymentReport();
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