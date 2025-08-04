package com.devops.controller;

import com.devops.service.VoiceCommandProcessor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.Map;

@RestController
@RequestMapping("/api/voice")
@Slf4j
public class VoiceController {
    
    @Autowired
    private VoiceCommandProcessor commandProcessor;
    
    @PostMapping("/command")
    public ResponseEntity<VoiceCommandProcessor.CommandResponse> processVoiceCommand(
            @RequestParam("command") String voiceCommand,
            @RequestParam("username") String username) {
        
        try {
            log.info("Processing voice command: {} for user: {}", voiceCommand, username);
            
            VoiceCommandProcessor.CommandResponse response = commandProcessor.processVoiceCommand(voiceCommand, username);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error processing voice command", e);
            return ResponseEntity.badRequest()
                .body(new VoiceCommandProcessor.CommandResponse("Error processing command: " + e.getMessage(), false));
        }
    }
    
    @PostMapping("/audio")
    public ResponseEntity<VoiceCommandProcessor.CommandResponse> processAudioCommand(
            @RequestParam("audio") MultipartFile audioFile,
            @RequestParam("username") String username) {
        
        try {
            log.info("Processing audio command for user: {}", username);
            
            // For demo purposes, we'll simulate voice-to-text conversion
            String simulatedText = "build my feature branch";
            
            VoiceCommandProcessor.CommandResponse response = commandProcessor.processVoiceCommand(simulatedText, username);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error processing audio command", e);
            return ResponseEntity.badRequest()
                .body(new VoiceCommandProcessor.CommandResponse("Error processing audio: " + e.getMessage(), false));
        }
    }
    
    @GetMapping("/commands")
    public ResponseEntity<Map<String, Object>> getAvailableCommands(@RequestParam("role") String role) {
        try {
            Map<String, Object> commands = Map.of(
                "role", role,
                "admin_commands", Map.of(
                    "approve_build", "Approve production build",
                    "deploy_production", "Deploy branch to production",
                    "abort_build", "Abort running build",
                    "show_approvals", "Show pending approvals",
                    "generate_report", "Generate deployment report"
                ),
                "user_commands", Map.of(
                    "build_branch", "Build feature branch",
                    "create_pr", "Create pull request",
                    "deploy_staging", "Deploy to staging",
                    "show_builds", "Show my builds",
                    "check_status", "Check build status"
                )
            );
            
            return ResponseEntity.ok(commands);
            
        } catch (Exception e) {
            log.error("Error getting available commands", e);
            return ResponseEntity.badRequest().build();
        }
    }
} 