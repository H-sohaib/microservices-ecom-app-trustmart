package net.anassploit.gatewayservice.service;

import net.anassploit.gatewayservice.dto.CreateUserRequest;
import net.anassploit.gatewayservice.dto.UpdateUserRequest;
import net.anassploit.gatewayservice.dto.UserResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

@Service
public class KeycloakUserService {

  private static final Logger log = LoggerFactory.getLogger(KeycloakUserService.class);

  private final WebClient webClient;

  @Value("${keycloak.auth-server-url:http://localhost:8080}")
  private String keycloakUrl;

  @Value("${keycloak.realm:trustmart}")
  private String realm;

  @Value("${keycloak.admin.username:admin}")
  private String adminUsername;

  @Value("${keycloak.admin.password:admin}")
  private String adminPassword;

  public KeycloakUserService(WebClient.Builder webClientBuilder) {
    this.webClient = webClientBuilder.build();
  }

  private Mono<String> getAdminToken() {
    return webClient.post()
        .uri(keycloakUrl + "/realms/master/protocol/openid-connect/token")
        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
        .body(BodyInserters.fromFormData("grant_type", "password")
            .with("client_id", "admin-cli")
            .with("username", adminUsername)
            .with("password", adminPassword))
        .retrieve()
        .bodyToMono(Map.class)
        .map(response -> (String) response.get("access_token"));
  }

  public Flux<UserResponse> getAllClients() {
    return getAdminToken()
        .flatMapMany(token -> webClient.get()
            .uri(keycloakUrl + "/admin/realms/" + realm + "/users?max=1000")
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
            .retrieve()
            .bodyToFlux(Map.class)
            .filter(this::isClientUser)
            .map(this::mapToUserResponse));
  }

  public Mono<UserResponse> getUserById(String userId) {
    return getAdminToken()
        .flatMap(token -> webClient.get()
            .uri(keycloakUrl + "/admin/realms/" + realm + "/users/" + userId)
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
            .retrieve()
            .bodyToMono(Map.class)
            .map(this::mapToUserResponse));
  }

  public Mono<UserResponse> createUser(CreateUserRequest request, boolean assignClientRole) {
    return getAdminToken()
        .flatMap(token -> {
          Map<String, Object> userPayload = Map.of(
              "username", request.getUsername(),
              "email", request.getEmail(),
              "firstName", request.getFirstName(),
              "lastName", request.getLastName(),
              "enabled", true,
              "emailVerified", true,
              "credentials", List.of(Map.of(
                  "type", "password",
                  "value", request.getPassword(),
                  "temporary", false)));

          return webClient.post()
              .uri(keycloakUrl + "/admin/realms/" + realm + "/users")
              .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
              .contentType(MediaType.APPLICATION_JSON)
              .bodyValue(userPayload)
              .retrieve()
              .toBodilessEntity()
              .flatMap(response -> {
                String location = response.getHeaders().getFirst(HttpHeaders.LOCATION);
                if (location != null) {
                  String userId = location.substring(location.lastIndexOf("/") + 1);
                  if (assignClientRole) {
                    return assignClientRole(token, userId)
                        .then(getUserById(userId));
                  }
                  return getUserById(userId);
                }
                return Mono.error(new RuntimeException("Failed to get user ID from response"));
              });
        });
  }

  private Mono<Void> assignClientRole(String token, String userId) {
    return getClientRole(token)
        .flatMap(role -> webClient.post()
            .uri(keycloakUrl + "/admin/realms/" + realm + "/users/" + userId + "/role-mappings/realm")
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(List.of(role))
            .retrieve()
            .toBodilessEntity()
            .then());
  }

  private Mono<Map> getClientRole(String token) {
    return webClient.get()
        .uri(keycloakUrl + "/admin/realms/" + realm + "/roles/CLIENT")
        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
        .retrieve()
        .bodyToMono(Map.class);
  }

  public Mono<UserResponse> updateUser(String userId, UpdateUserRequest request) {
    return getAdminToken()
        .flatMap(token -> getUserById(userId)
            .flatMap(existingUser -> {
              Map<String, Object> updatePayload = new java.util.HashMap<>();
              updatePayload.put("email", request.getEmail() != null ? request.getEmail() : existingUser.getEmail());
              updatePayload.put("firstName",
                  request.getFirstName() != null ? request.getFirstName() : existingUser.getFirstName());
              updatePayload.put("lastName",
                  request.getLastName() != null ? request.getLastName() : existingUser.getLastName());
              if (request.getEnabled() != null) {
                updatePayload.put("enabled", request.getEnabled());
              }

              return webClient.put()
                  .uri(keycloakUrl + "/admin/realms/" + realm + "/users/" + userId)
                  .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                  .contentType(MediaType.APPLICATION_JSON)
                  .bodyValue(updatePayload)
                  .retrieve()
                  .toBodilessEntity()
                  .then(getUserById(userId));
            }));
  }

  public Mono<UserResponse> toggleUserEnabled(String userId, boolean enabled) {
    return getAdminToken()
        .flatMap(token -> webClient.put()
            .uri(keycloakUrl + "/admin/realms/" + realm + "/users/" + userId)
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(Map.of("enabled", enabled))
            .retrieve()
            .toBodilessEntity()
            .then(getUserById(userId)));
  }

  public Mono<Void> deleteUser(String userId) {
    return getAdminToken()
        .flatMap(token -> webClient.delete()
            .uri(keycloakUrl + "/admin/realms/" + realm + "/users/" + userId)
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
            .retrieve()
            .toBodilessEntity()
            .then());
  }

  private boolean isClientUser(Map<String, Object> user) {
    // Filter out admin and service accounts
    String username = (String) user.get("username");
    return username != null &&
        !username.equals("admin") &&
        !username.startsWith("service-account-");
  }

  private UserResponse mapToUserResponse(Map<String, Object> user) {
    return new UserResponse(
        (String) user.get("id"),
        (String) user.get("username"),
        (String) user.get("email"),
        (String) user.get("firstName"),
        (String) user.get("lastName"),
        Boolean.TRUE.equals(user.get("enabled")),
        Boolean.TRUE.equals(user.get("emailVerified")),
        user.get("createdTimestamp") != null ? ((Number) user.get("createdTimestamp")).longValue() : 0);
  }
}
