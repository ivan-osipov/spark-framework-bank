package ru.i_osipov.sfb;

public final class Preconditions {
    private Preconditions() {}

    public static void checkArgument(boolean argument, String message) {
        if(!argument) {
            throw new IllegalArgumentException(message);
        }
    }

}
