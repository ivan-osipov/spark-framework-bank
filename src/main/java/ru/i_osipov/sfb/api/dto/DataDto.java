package ru.i_osipov.sfb.api.dto;

public class DataDto<T> {

    public T data;

    public DataDto(T data) {
        this.data = data;
    }

    public static <T> DataDto create(T data) {
        return new DataDto<>(data);
    }
}
