package ru.aviasales.admin.service.auth;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.aviasales.admin.dao.entity.User;
import ru.aviasales.admin.dto.request.AuthReq;
import ru.aviasales.admin.dto.request.UserCreateReq;
import ru.aviasales.admin.dto.response.UserResp;
import ru.aviasales.admin.exception.NoPermissionException;
import ru.aviasales.admin.service.core.UserService;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserService userService;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final ModelMapper modelMapper;

    @PostConstruct
    public void init() {
        if (userService.getAdminCount() == 0) {
            userService.create(User.builder().username("admin")
                    .password(passwordEncoder.encode("admin"))
                    .role(User.Role.ADMIN)
                    .build());
        }
    }

    @Transactional
    public UserResp signUp(AuthReq request) {

        var user = User.builder()
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(User.Role.ADMIN)
                .build();

        return modelMapper.map(userService.create(user), UserResp.class)
                .withToken(jwtService.generateToken(user));
    }

    @Transactional(readOnly = true)
    public UserResp signIn(AuthReq request) {

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                    request.getUsername(),
                    request.getPassword()
                )
        );

        var user = userService
                .userDetailsService()
                .loadUserByUsername(request.getUsername());

        return modelMapper.map(user, UserResp.class)
                .withToken(jwtService.generateToken(user));
    }

    @Transactional
    public UserResp createUser(User user, UserCreateReq req) {
        if (user.getRole() != User.Role.ADMIN) {
            throw new NoPermissionException("Нет прав для создания пользователя");
        }

        var res = User.builder()
                .username(req.getUsername())
                .password(passwordEncoder.encode(req.getPassword()))
                .role(User.Role.valueOf(req.getRole().name()))
                .build();

        return modelMapper.map(userService.create(res), UserResp.class)
                .withToken(jwtService.generateToken(res));
    }
}
