package ru.i_osipov.sfb.data.model;

import java.util.Objects;
import java.util.UUID;

public class Identifiable {

    protected UUID id = UUID.randomUUID();

    public UUID getId() {
        return id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Identifiable that = (Identifiable) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
