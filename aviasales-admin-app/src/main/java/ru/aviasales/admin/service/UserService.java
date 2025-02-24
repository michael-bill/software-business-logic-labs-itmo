package ru.aviasales.admin.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import ru.aviasales.admin.dao.entity.User;
import ru.aviasales.admin.dao.repository.UserRepository;
import ru.aviasales.admin.exception.UniqueValueExistsException;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository repository;

    public User create(User user) {
        if (repository.existsByUsername(user.getUsername())) {
            throw new UniqueValueExistsException("Пользователь с таким именем уже существует");
        }
        return repository.save(user);
    }

    public User getByUsername(String username) {
        return repository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Пользователь с таким именем не найден"));
    }

    public UserDetailsService userDetailsService() {
        return this::getByUsername;
    }

    public Long getAdminCount() {
        return repository.countByRole(User.Role.ADMIN);
    }
}
