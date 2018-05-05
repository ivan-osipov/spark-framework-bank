package ru.i_osipov.sfb.api;

import ru.i_osipov.sfb.api.exceptions.InformationLossException;

public class MoneyConverter {

    private double multiplier;
    private int precision;

    public MoneyConverter(int precision) {
        multiplier = Math.pow(10, precision);
        this.precision = precision;
    }

    public long convertToInternal(double value) {
        return (long) (value * multiplier);
    }

    public double convertToExternal(long value) {
        return value / multiplier;
    }

    public void checkInformationLoss(double value) {
        if(value * multiplier - convertToInternal(value) > 0) {
            throw new InformationLossException(value, precision);
        }
    }

}
