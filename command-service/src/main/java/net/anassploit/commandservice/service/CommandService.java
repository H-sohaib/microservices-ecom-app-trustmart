package net.anassploit.commandservice.service;

import net.anassploit.commandservice.dto.CommandRequest;
import net.anassploit.commandservice.dto.CommandResponse;
import net.anassploit.commandservice.enums.CommandStatus;

import java.util.List;

public interface CommandService {

    CommandResponse createCommand(CommandRequest request, String userId, String username);

    CommandResponse getCommandById(Long commandId);

    List<CommandResponse> getAllCommands();

    List<CommandResponse> getCommandsByStatus(CommandStatus status);

    List<CommandResponse> getCommandsByUserId(String userId);

    List<CommandResponse> getCommandsByUserIdAndStatus(String userId, CommandStatus status);

    CommandResponse updateCommand(Long commandId, CommandRequest request);

    CommandResponse updateCommandStatus(Long commandId, CommandStatus status);

    void deleteCommand(Long commandId);

    void cancelCommand(Long commandId, String userId, boolean isAdmin);

    boolean isCommandOwner(Long commandId, String userId);
}

