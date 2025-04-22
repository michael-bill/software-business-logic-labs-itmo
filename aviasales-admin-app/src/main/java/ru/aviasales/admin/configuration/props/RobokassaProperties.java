package ru.aviasales.admin.configuration.props;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@NoArgsConstructor
@Component
@ConfigurationProperties(prefix = "robokassa")
public class RobokassaProperties {
    private String merchantLogin;
    private String password1;
    private String password2;
    private int isTest;
}
