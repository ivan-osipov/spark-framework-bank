package ru.i_osipov.sfb.api.dto;

public class ErrorDto {

    public String message;

    public ErrorDto(String message) {
        this.message = message;
    }

    public static ErrorDto create(String message) {
        return new ErrorDto(message);
    }
}
