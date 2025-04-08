package ru.aviasales.admin.security;

import jakarta.annotation.PostConstruct;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Unmarshaller;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import ru.aviasales.admin.security.xml.XmlRole;
import ru.aviasales.admin.security.xml.XmlSecurityConfig;
import ru.aviasales.admin.security.xml.XmlUser;
import ru.aviasales.admin.security.xml.XmlUsers;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class XmlUserDetailsService implements UserDetailsService {

    @Value("classpath:users.xml")
    private Resource usersXmlResource;

    private final Map<String, UserDetails> userCache = new ConcurrentHashMap<>();
    private final Map<String, Set<String>> rolePermissionsMap = new ConcurrentHashMap<>();

    @PostConstruct
    public void init() {
        loadUsersAndRolesFromXml();
    }

    private void loadUsersAndRolesFromXml() {
        try (InputStream is = usersXmlResource.getInputStream()) {
            JAXBContext context = JAXBContext.newInstance(XmlSecurityConfig.class);
            Unmarshaller unmarshaller = context.createUnmarshaller();
            XmlSecurityConfig securityConfig = (XmlSecurityConfig) unmarshaller.unmarshal(is);

            if (securityConfig == null) {
                log.error("Could not parse securityConfig from users.xml");
                throw new IllegalStateException("Failed to parse security configuration file.");
            }

            rolePermissionsMap.clear();
            if (securityConfig.getRoleDefinitions() != null && securityConfig.getRoleDefinitions().getRoleList() != null) {
                for (XmlRole xmlRole : securityConfig.getRoleDefinitions().getRoleList()) {
                    if (xmlRole.getName() != null && xmlRole.getPermissions() != null) {
                        Set<String> permissions = Arrays.stream(xmlRole.getPermissions().split(","))
                                .map(String::trim)
                                .filter(s -> !s.isEmpty())
                                .collect(Collectors.toSet());
                        rolePermissionsMap.put(xmlRole.getName(), permissions);
                        log.info("Loaded role '{}' with permissions: {}", xmlRole.getName(), permissions);
                    } else {
                        log.warn("Skipping role definition due to missing name or permissions: {}", xmlRole);
                    }
                }
                log.info("Successfully loaded {} role definitions.", rolePermissionsMap.size());
            } else {
                log.warn("No role definitions found in users.xml.");
            }

            userCache.clear();
            if (securityConfig.getUsers() == null || securityConfig.getUsers().getUserList() == null) {
                log.warn("No users found in users.xml or user section is missing.");
                return;
            }

            for (XmlUser xmlUser : securityConfig.getUsers().getUserList()) {
                List<GrantedAuthority> userRoles = AuthorityUtils.commaSeparatedStringToAuthorityList(xmlUser.getRoles());

                Set<String> userPermissions = userRoles.stream()
                        .map(GrantedAuthority::getAuthority)
                        .flatMap(roleName -> rolePermissionsMap.getOrDefault(roleName, Collections.emptySet()).stream())
                        .collect(Collectors.toSet());

                Set<GrantedAuthority> combinedAuthorities = new HashSet<>(userRoles);
                userPermissions.forEach(permission -> combinedAuthorities.add(new SimpleGrantedAuthority(permission)));

                log.debug("User '{}' assigned roles: {}", xmlUser.getUsername(), userRoles.stream().map(GrantedAuthority::getAuthority).collect(Collectors.toList()));
                log.debug("User '{}' derived permissions: {}", xmlUser.getUsername(), userPermissions);
                log.debug("User '{}' combined authorities: {}", xmlUser.getUsername(), combinedAuthorities.stream().map(GrantedAuthority::getAuthority).collect(Collectors.toList()));


                boolean isDisabled = (xmlUser.getEnabled() == null) || !xmlUser.getEnabled();
                UserDetails userDetails = User.builder()
                        .username(xmlUser.getUsername())
                        .password(xmlUser.getPassword())
                        .authorities(combinedAuthorities)
                        .disabled(isDisabled)
                        .accountExpired(false)
                        .credentialsExpired(false)
                        .accountLocked(false)
                        .build();
                userCache.put(xmlUser.getUsername().toLowerCase(), userDetails);
                log.info("Loaded user '{}' with authorities: {}", xmlUser.getUsername(), combinedAuthorities.stream().map(GrantedAuthority::getAuthority).collect(Collectors.joining(", ")));
            }
            log.info("Successfully loaded {} users from {}", userCache.size(), usersXmlResource.getFilename());
        } catch (IOException e) {
            log.error("IOException while reading users.xml", e);
            throw new IllegalStateException("Failed to read security configuration file.", e);
        } catch (JAXBException e) {
            log.error("JAXBException while parsing users.xml", e);
            if (e.getLinkedException() != null) {
                log.error("Linked Exception: ", e.getLinkedException());
            }
            throw new IllegalStateException("Failed to parse security configuration file.", e);
        } catch (Exception e) {
            log.error("Unexpected error loading security configuration from XML", e);
            throw new IllegalStateException("Unexpected error loading security configuration.", e);
        }
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        if (username == null) {
            throw new UsernameNotFoundException("Username cannot be null");
        }

        UserDetails cachedUser = userCache.get(username.toLowerCase());
        if (cachedUser == null) {
            log.warn("User not found in cache: {}", username);
            throw new UsernameNotFoundException("User not found: " + username);
        }

        log.debug("User found in cache: {}. Authorities: {}", username, cachedUser.getAuthorities().stream().map(GrantedAuthority::getAuthority).collect(Collectors.joining(", ")));
        log.debug("Password hash present in cached object when retrieved? {}", cachedUser.getPassword() != null && !cachedUser.getPassword().isEmpty());

        return User.builder()
                .username(cachedUser.getUsername())
                .password(cachedUser.getPassword())
                .authorities(cachedUser.getAuthorities())
                .disabled(!cachedUser.isEnabled())
                .accountExpired(!cachedUser.isAccountNonExpired())
                .credentialsExpired(!cachedUser.isCredentialsNonExpired())
                .accountLocked(!cachedUser.isAccountNonLocked())
                .build();
    }
}
