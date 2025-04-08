package ru.aviasales.admin.security.xml;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import lombok.Data;

@XmlAccessorType(XmlAccessType.FIELD)
@Data
public class XmlRole {
    @XmlAttribute
    private String name;

    @XmlAttribute
    private String permissions;
}
