package ru.aviasales.admin.worker;

import org.camunda.bpm.client.ExternalTaskClient;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Component;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ru.aviasales.admin.service.auth.AuthService;
import ru.aviasales.common.dto.request.AuthReq;
import ru.aviasales.common.dto.response.UserResp;

import java.util.Map;

@Component
@Slf4j
@RequiredArgsConstructor
public class AuthenticationWorker {

    private final ExternalTaskClient client;
    private final AuthService authService;

    @PostConstruct
    public void subscribe() {
        client.subscribe("authenticate-user")
                .lockDuration(10000)
                .handler((externalTask, externalTaskService) -> {
                    String username = externalTask.getVariable("username");
                    String password = externalTask.getVariable("password");

                    log.info("Worker 'authenticate-user': обрабатывается задача для пользователя '{}'", username);

                    try {
                        AuthReq authReq = AuthReq.builder().username(username).password(password).build();
                        UserResp userResp = authService.signIn(authReq);

                        Map<String, Object> variablesToSet = Map.of(
                                "jwtToken", userResp.getToken(),
                                "authenticationSuccessful", true
                        );
                        externalTaskService.complete(externalTask, variablesToSet);
                        log.info("Worker 'authenticate-user': аутентификация успешна для '{}', токен выдан.", username);

                    } catch (BadCredentialsException e) {
                        log.warn("Worker 'authenticate-user': неверные учетные данные для '{}'", username);
                        externalTaskService.handleBpmnError(
                                externalTask,
                                "AUTH_FAILED",
                                "Неверный логин или пароль.",
                                Map.of("authenticationSuccessful", false)
                        );
                    } catch (Exception e) {
                        log.error("Worker 'authenticate-user': неожиданная ошибка при аутентификации '{}'", username, e);
                        externalTaskService.handleFailure(
                                externalTask,
                                "Техническая ошибка",
                                e.getMessage(),
                                0,
                                0
                        );
                    }
                })
                .open();
    }
}
