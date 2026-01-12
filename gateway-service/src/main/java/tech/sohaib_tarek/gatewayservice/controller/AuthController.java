package net.anassploit.gatewayservice.controller;

import jakarta.validation.Valid;
import net.anassploit.gatewayservice.dto.CreateUserRequest;
import net.anassploit.gatewayservice.dto.UserResponse;
import net.anassploit.gatewayservice.service.KeycloakUserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

  private static final Logger log = LoggerFactory.getLogger(AuthController.class);

  private final KeycloakUserService keycloakUserService;

  public AuthController(KeycloakUserService keycloakUserService) {
    this.keycloakUserService = keycloakUserService;
  }

  @PostMapping("/register")
  public Mono<ResponseEntity<UserResponse>> register(@Valid @RequestBody CreateUserRequest request) {
    log.info("Received registration request for user: {}", request.getUsername());
    return keycloakUserService.createUser(request, true)
        .map(user -> ResponseEntity.status(HttpStatus.CREATED).body(user))
        .onErrorResume(e -> {
          log.error("Error registering user: {}", e.getMessage());
          return Mono.just(ResponseEntity.badRequest().build());
        });
  }
}
