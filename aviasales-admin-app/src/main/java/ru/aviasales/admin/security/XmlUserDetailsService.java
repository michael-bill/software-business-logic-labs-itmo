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
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import ru.aviasales.admin.security.xml.XmlUser;
import ru.aviasales.admin.security.xml.XmlUsers;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Slf4j
@RequiredArgsConstructor
public class XmlUserDetailsService implements UserDetailsService {

    @Value("classpath:users.xml")
    private Resource usersXmlResource;

    private final Map<String, UserDetails> userCache = new ConcurrentHashMap<>();

    @PostConstruct
    public void init() {
        loadUsersFromXml();
    }

    private void loadUsersFromXml() {
        try (InputStream is = usersXmlResource.getInputStream()) {
            JAXBContext context = JAXBContext.newInstance(XmlUsers.class);
            Unmarshaller unmarshaller = context.createUnmarshaller();
            XmlUsers xmlUsers = (XmlUsers) unmarshaller.unmarshal(is);

            if (xmlUsers == null || xmlUsers.getUserList() == null) {
                log.warn("users.xml is empty or could not be parsed correctly.");
                return;
            }

            userCache.clear();
            for (XmlUser xmlUser : xmlUsers.getUserList()) {
                List<GrantedAuthority> authorities = AuthorityUtils.commaSeparatedStringToAuthorityList(xmlUser.getRoles());
                boolean isDisabled = (xmlUser.getEnabled() == null) || !xmlUser.getEnabled();
                UserDetails userDetails = User.builder()
                        .username(xmlUser.getUsername())
                        .password(xmlUser.getPassword())
                        .authorities(authorities)
                        .disabled(isDisabled)
                        .accountExpired(false)
                        .credentialsExpired(false)
                        .accountLocked(false)
                        .build();
                userCache.put(xmlUser.getUsername().toLowerCase(), userDetails);
                log.info("Loaded user '{}' with roles {}", xmlUser.getUsername(), xmlUser.getRoles());
            }
            log.info("Successfully loaded {} users from {}", userCache.size(), usersXmlResource.getFilename());

        } catch (IOException e) {
            log.error("IOException while reading users.xml", e);
            throw new IllegalStateException("Failed to read user definition file.", e);
        } catch (JAXBException e) {
            log.error("JAXBException while parsing users.xml", e);
            if (e.getLinkedException() != null) {
                log.error("Linked Exception: ", e.getLinkedException());
            }
            throw new IllegalStateException("Failed to parse user definition file.", e);
        } catch (Exception e) {
            log.error("Unexpected error loading users from XML", e);
            throw new IllegalStateException("Unexpected error loading users.", e);
        }
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        if (username == null) {
            throw new UsernameNotFoundException("Username cannot be null");
        }

        UserDetails user = userCache.get(username.toLowerCase());
        if (user == null) {
            log.warn("User not found: {}", username);
            throw new UsernameNotFoundException("User not found: " + username);
        }
        log.debug("User found: {}", username);
        return user;
    }
}
