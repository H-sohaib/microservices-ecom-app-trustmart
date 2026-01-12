package tech.sohaib_tarek.commandservice.controller;

import jakarta.validation.Valid;
import tech.sohaib_tarek.commandservice.dto.CommandRequest;
import tech.sohaib_tarek.commandservice.dto.CommandResponse;
import tech.sohaib_tarek.commandservice.dto.CommandStatusUpdateRequest;
import tech.sohaib_tarek.commandservice.enums.CommandStatus;
import tech.sohaib_tarek.commandservice.service.CommandService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/commands")
public class CommandController {

  private static final Logger log = LoggerFactory.getLogger(CommandController.class);

  private final CommandService commandService;

  public CommandController(CommandService commandService) {
    this.commandService = commandService;
  }

  @PostMapping
  public ResponseEntity<CommandResponse> createCommand(
      @Valid @RequestBody CommandRequest request,
      @RequestHeader("X-User-Id") String userId,
      @RequestHeader("X-User-Name") String username) {
    log.info("Received request to create command with {} items for user: {}", request.getItems().size(), username);
    CommandResponse response = commandService.createCommand(request, userId, username);
    return new ResponseEntity<>(response, HttpStatus.CREATED);
  }

  @GetMapping("/{commandId}")
  public ResponseEntity<CommandResponse> getCommandById(
      @PathVariable Long commandId,
      @RequestHeader("X-User-Id") String userId,
      @RequestHeader(value = "X-User-Roles", required = false) String roles) {
    log.info("Received request to get command with ID: {} by user: {}", commandId, userId);
    boolean isAdmin = roles != null && roles.contains("ADMIN");

    CommandResponse response = commandService.getCommandById(commandId);

    // Authorization check: Users can only view their own orders unless they have ADMIN role
    if (!isAdmin && !response.getUserId().equals(userId)) {
      return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    }

    return ResponseEntity.ok(response);
  }

  @GetMapping
  public ResponseEntity<List<CommandResponse>> getAllCommands(
      @RequestParam(required = false) CommandStatus status,
      @RequestHeader("X-User-Id") String userId,
      @RequestHeader(value = "X-User-Roles", required = false) String roles) {
    log.info("Received request to get commands with status filter: {} by user: {}", status, userId);

    boolean isAdmin = roles != null && roles.contains("ADMIN");
    List<CommandResponse> response;

    if (isAdmin) {
      // Admin sees all commands
      if (status != null) {
        response = commandService.getCommandsByStatus(status);
      } else {
        response = commandService.getAllCommands();
      }
    } else {
      // Client sees only their commands
      if (status != null) {
        response = commandService.getCommandsByUserIdAndStatus(userId, status);
      } else {
        response = commandService.getCommandsByUserId(userId);
      }
    }

    return ResponseEntity.ok(response);
  }

  @PutMapping("/{commandId}")
  public ResponseEntity<CommandResponse> updateCommand(
      @PathVariable Long commandId,
      @Valid @RequestBody CommandRequest request,
      @RequestHeader("X-User-Id") String userId,
      @RequestHeader(value = "X-User-Roles", required = false) String roles) {
    log.info("Received request to update command with ID: {} by user: {}", commandId, userId);

    boolean isAdmin = roles != null && roles.contains("ADMIN");

    // Check if user owns this command or is admin
    if (!isAdmin && !commandService.isCommandOwner(commandId, userId)) {
      return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    }

    CommandResponse response = commandService.updateCommand(commandId, request);
    return ResponseEntity.ok(response);
  }

  @PatchMapping("/{commandId}/status")
  public ResponseEntity<CommandResponse> updateCommandStatus(
      @PathVariable Long commandId,
      @Valid @RequestBody CommandStatusUpdateRequest request) {
    // Only admin can update status (enforced by gateway)
    log.info("Received request to update command {} status to {}", commandId, request.getStatus());
    CommandResponse response = commandService.updateCommandStatus(commandId, request.getStatus());
    return ResponseEntity.ok(response);
  }

  @PostMapping("/{commandId}/cancel")
  public ResponseEntity<Void> cancelCommand(
      @PathVariable Long commandId,
      @RequestHeader("X-User-Id") String userId,
      @RequestHeader(value = "X-User-Roles", required = false) String roles) {
    log.info("Received request to cancel command with ID: {} by user: {}", commandId, userId);
    boolean isAdmin = roles != null && roles.contains("ADMIN");
    commandService.cancelCommand(commandId, userId, isAdmin);
    return ResponseEntity.ok().build();
  }

  @DeleteMapping("/{commandId}")
  public ResponseEntity<Void> deleteCommand(@PathVariable Long commandId) {
    // Only admin can delete (enforced by gateway)
    log.info("Received request to delete command with ID: {}", commandId);
    commandService.deleteCommand(commandId);
    return ResponseEntity.noContent().build();
  }
}
