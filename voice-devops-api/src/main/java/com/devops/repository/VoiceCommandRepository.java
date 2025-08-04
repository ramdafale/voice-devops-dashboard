package com.devops.repository;

import com.devops.entity.VoiceCommand;
import com.devops.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface VoiceCommandRepository extends JpaRepository<VoiceCommand, Long> {
    
    @Query("SELECT vc FROM VoiceCommand vc WHERE vc.user = :user ORDER BY vc.createdAt DESC")
    List<VoiceCommand> findByUserOrderByCreatedAtDesc(@Param("user") User user);
    
    @Query("SELECT vc FROM VoiceCommand vc WHERE vc.status = :status")
    List<VoiceCommand> findByStatus(@Param("status") VoiceCommand.CommandStatus status);
    
    @Query("SELECT vc FROM VoiceCommand vc WHERE vc.commandType = :commandType")
    List<VoiceCommand> findByCommandType(@Param("commandType") String commandType);
    
    @Query("SELECT vc FROM VoiceCommand vc WHERE vc.createdAt >= :since")
    List<VoiceCommand> findRecentCommands(@Param("since") LocalDateTime since);
    
    @Query("SELECT vc FROM VoiceCommand vc WHERE vc.user = :user AND vc.createdAt >= :since")
    List<VoiceCommand> findUserRecentCommands(@Param("user") User user, @Param("since") LocalDateTime since);
    
    @Query("SELECT COUNT(vc) FROM VoiceCommand vc WHERE vc.status = 'COMPLETED' AND vc.createdAt >= :since")
    Long countCompletedCommandsSince(@Param("since") LocalDateTime since);
    
    @Query("SELECT COUNT(vc) FROM VoiceCommand vc WHERE vc.status = 'FAILED' AND vc.createdAt >= :since")
    Long countFailedCommandsSince(@Param("since") LocalDateTime since);
} 