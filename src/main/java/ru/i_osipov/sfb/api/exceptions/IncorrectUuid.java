package ru.i_osipov.sfb.api.exceptions;

public class IncorrectUuid extends RuntimeException {

    private String incorrectUuid;

    public IncorrectUuid(String incorrectUuid, Throwable cause) {
        super(cause);
        this.incorrectUuid = incorrectUuid;
    }

    public String getIncorrectUuid() {
        return incorrectUuid;
    }
}
