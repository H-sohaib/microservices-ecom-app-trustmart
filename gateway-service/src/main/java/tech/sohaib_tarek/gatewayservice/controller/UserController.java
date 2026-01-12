package net.anassploit.gatewayservice.controller;

import jakarta.validation.Valid;
import net.anassploit.gatewayservice.dto.CreateUserRequest;
import net.anassploit.gatewayservice.dto.EnableUserRequest;
import net.anassploit.gatewayservice.dto.UpdateUserRequest;
import net.anassploit.gatewayservice.dto.UserResponse;
import net.anassploit.gatewayservice.service.KeycloakUserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/users")
public class UserController {

  private static final Logger log = LoggerFactory.getLogger(UserController.class);

  private final KeycloakUserService keycloakUserService;

  public UserController(KeycloakUserService keycloakUserService) {
    this.keycloakUserService = keycloakUserService;
  }

  @GetMapping
  public Flux<UserResponse> getAllUsers() {
    log.info("Received request to get all client users");
    return keycloakUserService.getAllClients();
  }

  @GetMapping("/{userId}")
  public Mono<ResponseEntity<UserResponse>> getUserById(@PathVariable String userId) {
    log.info("Received request to get user with ID: {}", userId);
    return keycloakUserService.getUserById(userId)
        .map(ResponseEntity::ok)
        .defaultIfEmpty(ResponseEntity.notFound().build());
  }

  @PostMapping
  public Mono<ResponseEntity<UserResponse>> createUser(@Valid @RequestBody CreateUserRequest request) {
    log.info("Received request to create user: {}", request.getUsername());
    return keycloakUserService.createUser(request, true)
        .map(user -> ResponseEntity.status(HttpStatus.CREATED).body(user))
        .onErrorResume(e -> {
          log.error("Error creating user: {}", e.getMessage());
          return Mono.just(ResponseEntity.badRequest().build());
        });
  }

  @PutMapping("/{userId}")
  public Mono<ResponseEntity<UserResponse>> updateUser(
      @PathVariable String userId,
      @Valid @RequestBody UpdateUserRequest request) {
    log.info("Received request to update user with ID: {}", userId);
    return keycloakUserService.updateUser(userId, request)
        .map(ResponseEntity::ok)
        .defaultIfEmpty(ResponseEntity.notFound().build());
  }

  @PatchMapping("/{userId}/enabled")
  public Mono<ResponseEntity<UserResponse>> toggleUserEnabled(
      @PathVariable String userId,
      @RequestBody EnableUserRequest request) {
    log.info("Received request to toggle user {} enabled status to: {}", userId, request.isEnabled());
    return keycloakUserService.toggleUserEnabled(userId, request.isEnabled())
        .map(ResponseEntity::ok)
        .defaultIfEmpty(ResponseEntity.notFound().build());
  }

  @DeleteMapping("/{userId}")
  public Mono<ResponseEntity<Void>> deleteUser(@PathVariable String userId) {
    log.info("Received request to delete user with ID: {}", userId);
    return keycloakUserService.deleteUser(userId)
        .then(Mono.just(ResponseEntity.noContent().<Void>build()))
        .onErrorResume(e -> {
          log.error("Error deleting user: {}", e.getMessage());
          return Mono.just(ResponseEntity.notFound().build());
        });
  }
}
