package com.devops.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "builds")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Build {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "jenkins_build_id", unique = true)
    private String jenkinsBuildId;
    
    @Column(name = "job_name", nullable = false)
    private String jobName;
    
    @Column(name = "branch_name")
    private String branchName;
    
    @Column(name = "build_number")
    private Integer buildNumber;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BuildStatus status;
    
    @Column(name = "build_url")
    private String buildUrl;
    
    @Column(name = "started_at")
    private LocalDateTime startedAt;
    
    @Column(name = "completed_at")
    private LocalDateTime completedAt;
    
    @Column(name = "duration_seconds")
    private Long durationSeconds;
    
    @ManyToOne
    @JoinColumn(name = "triggered_by")
    private User triggeredBy;
    
    @Column(name = "environment")
    private String environment;
    
    @Column(name = "requires_approval")
    private Boolean requiresApproval = false;
    
    @Column(name = "approved_by")
    private String approvedBy;
    
    @Column(name = "approved_at")
    private LocalDateTime approvedAt;
    
    @Column(name = "build_log")
    @Lob
    private String buildLog;
    
    @Column(name = "api_name")
    private String apiName;
    
    @Column(name = "deployment_progress")
    private Integer deploymentProgress = 0;
    
    public enum BuildStatus {
        QUEUED, RUNNING, SUCCESS, FAILED, ABORTED, PENDING_APPROVAL
    }
    
    @PrePersist
    protected void onCreate() {
        if (startedAt == null) {
            startedAt = LocalDateTime.now();
        }
    }
} 