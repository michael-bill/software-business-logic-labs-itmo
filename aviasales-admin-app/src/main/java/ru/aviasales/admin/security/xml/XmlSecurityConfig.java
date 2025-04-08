package ru.aviasales.admin.security.xml;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import lombok.Data;

@XmlRootElement(name = "securityConfig")
@XmlAccessorType(XmlAccessType.FIELD)
@Data
public class XmlSecurityConfig {
    @XmlElement(name = "roleDefinitions")
    private XmlRoleDefinitions roleDefinitions;

    @XmlElement(name = "users")
    private XmlUsers users;
}
