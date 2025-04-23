package ru.aviasales.jca;

import jakarta.resource.Referenceable;
import java.io.Serializable;

public interface RandomNumberConnectionFactory extends Serializable, Referenceable {
    RandomNumberConnection getConnection() throws jakarta.resource.ResourceException;
}
