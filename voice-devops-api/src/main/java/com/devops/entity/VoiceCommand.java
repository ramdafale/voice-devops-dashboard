package com.devops.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "voice_commands")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class VoiceCommand {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "original_text", nullable = false)
    private String originalText;
    
    @Column(name = "processed_text")
    private String processedText;
    
    @Column(name = "command_type", nullable = false)
    private String commandType;
    
    @Column(name = "parameters")
    private String parameters; // JSON string
    
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CommandStatus status;
    
    @Column(name = "response")
    private String response;
    
    @Column(name = "execution_time_ms")
    private Long executionTimeMs;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "processed_at")
    private LocalDateTime processedAt;
    
    @Column(name = "confidence_score")
    private Double confidenceScore;
    
    public enum CommandStatus {
        PENDING, PROCESSING, COMPLETED, FAILED, INVALID
    }
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
} 