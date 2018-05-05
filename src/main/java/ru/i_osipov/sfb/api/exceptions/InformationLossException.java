package ru.i_osipov.sfb.api.exceptions;

public class InformationLossException extends RuntimeException {

    public InformationLossException(double value, int precision) {
        super(String.format("Information loss. Processing of value %s with precision %s can loss information", value, precision));
    }

}
