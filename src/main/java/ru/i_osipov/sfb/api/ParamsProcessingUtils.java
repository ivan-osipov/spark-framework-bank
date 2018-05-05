package ru.i_osipov.sfb.api;

import ru.i_osipov.sfb.api.exceptions.IncorrectUuid;

import java.util.UUID;

public final class ParamsProcessingUtils {

    private ParamsProcessingUtils() {
    }

    public static UUID parseUuid(String rawUuid) {
        try {
            return UUID.fromString(rawUuid);
        } catch (IllegalArgumentException e) {
            throw new IncorrectUuid(rawUuid, e);
        }
    }

}
