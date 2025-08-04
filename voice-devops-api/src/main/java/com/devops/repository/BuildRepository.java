package com.devops.repository;

import com.devops.entity.Build;
import com.devops.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface BuildRepository extends JpaRepository<Build, Long> {
    
    Optional<Build> findByJenkinsBuildId(String jenkinsBuildId);
    
    @Query("SELECT b FROM Build b WHERE b.triggeredBy = :user ORDER BY b.startedAt DESC")
    List<Build> findByTriggeredByOrderByStartedAtDesc(@Param("user") User user);
    
    @Query("SELECT b FROM Build b WHERE b.status = :status")
    List<Build> findByStatus(@Param("status") Build.BuildStatus status);
    
    @Query("SELECT b FROM Build b WHERE b.environment = :environment")
    List<Build> findByEnvironment(@Param("environment") String environment);
    
    @Query("SELECT b FROM Build b WHERE b.requiresApproval = true AND b.status = 'PENDING_APPROVAL'")
    List<Build> findPendingApprovals();
    
    @Query("SELECT b FROM Build b WHERE b.startedAt >= :since")
    List<Build> findRecentBuilds(@Param("since") LocalDateTime since);
    
    @Query("SELECT b FROM Build b WHERE b.branchName = :branchName ORDER BY b.startedAt DESC")
    List<Build> findByBranchNameOrderByStartedAtDesc(@Param("branchName") String branchName);
    
    @Query("SELECT COUNT(b) FROM Build b WHERE b.status = 'SUCCESS' AND b.startedAt >= :since")
    Long countSuccessfulBuildsSince(@Param("since") LocalDateTime since);
    
    @Query("SELECT COUNT(b) FROM Build b WHERE b.status = 'FAILED' AND b.startedAt >= :since")
    Long countFailedBuildsSince(@Param("since") LocalDateTime since);
} 