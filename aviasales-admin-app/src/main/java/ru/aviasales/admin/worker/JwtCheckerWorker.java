package ru.aviasales.admin.worker;

import org.camunda.bpm.client.ExternalTaskClient;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ru.aviasales.admin.security.XmlUserDetailsService;
import ru.aviasales.admin.service.auth.JwtService;

import java.util.List;
import java.util.stream.Collectors;

@Component
@Slf4j
@RequiredArgsConstructor
public class JwtCheckerWorker {

    private final ExternalTaskClient client;
    private final JwtService jwtService;
    private final XmlUserDetailsService userDetailsService;

    @PostConstruct
    public void subscribe() {
        client.subscribe("check-token")
                .lockDuration(10000)
                .handler((externalTask, externalTaskService) -> {
                    String token = externalTask.getVariable("jwt");
                    String requiredRole = externalTask.getVariable("requiredRole");

                    log.info("Worker 'check-token': processing token validation for role '{}'", requiredRole);

                    try {
                        String username = jwtService.extractUserName(token);
                        var userDetails = userDetailsService.loadUserByUsername(username);

                        if (!jwtService.isTokenValid(token, userDetails)) {
                            log.warn("Worker 'check-token': invalid token for user '{}'", username);
                            externalTaskService.handleBpmnError(
                                    externalTask,
                                    "AUTH_ERROR",
                                    "Неверный токен",
                                    null
                            );
                            return;
                        }

                        boolean hasRequiredRole = userDetails.getAuthorities().stream()
                                .map(GrantedAuthority::getAuthority)
                                .anyMatch(authority -> authority.equals(requiredRole));

                        if (!hasRequiredRole) {
                            log.warn("Worker 'check-token': user '{}' does not have required role '{}'", username, requiredRole);
                            externalTaskService.handleBpmnError(
                                    externalTask,
                                    "AUTH_ERROR",
                                    "У пользователя нет требуемой роли: " + requiredRole,
                                    null
                            );
                            return;
                        }

                        log.info("Worker 'check-token': token validation successful for user '{}' with role '{}'", username, requiredRole);
                        externalTaskService.complete(externalTask);

                    } catch (Exception e) {
                        log.error("Worker 'check-token': error during token validation");
                        externalTaskService.handleBpmnError(
                                externalTask,
                                "AUTH_ERROR",
                                "Произошла ошибка во время валидации пользователя " + e.getMessage(),
                                null
                        );
                    }
                })
                .open();
    }
}
