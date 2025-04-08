package ru.aviasales.admin.security.xml;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import lombok.Data;

import java.util.List;

@XmlAccessorType(XmlAccessType.FIELD)
@Data
public class XmlRoleDefinitions {
    @XmlElement(name = "role")
    private List<XmlRole> roleList;
}
