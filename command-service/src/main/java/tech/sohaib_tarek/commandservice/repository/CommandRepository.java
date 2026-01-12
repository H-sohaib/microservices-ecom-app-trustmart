package tech.sohaib_tarek.commandservice.repository;

import tech.sohaib_tarek.commandservice.entity.Command;
import tech.sohaib_tarek.commandservice.enums.CommandStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface CommandRepository extends JpaRepository<Command, Long> {

    List<Command> findByStatus(CommandStatus status);

    List<Command> findByDateBetween(LocalDateTime startDate, LocalDateTime endDate);

    List<Command> findByUserId(String userId);

    List<Command> findByUserIdAndStatus(String userId, CommandStatus status);

    boolean existsByCommandIdAndUserId(Long commandId, String userId);
}

