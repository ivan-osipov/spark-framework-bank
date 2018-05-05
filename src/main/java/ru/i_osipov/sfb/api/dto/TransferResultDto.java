package ru.i_osipov.sfb.api.dto;

public class TransferResultDto {

    public AccountBalanceDto from;
    public AccountBalanceDto to;

    public TransferResultDto(AccountBalanceDto from, AccountBalanceDto to) {
        this.from = from;
        this.to = to;
    }

    public static TransferResultDto create(AccountBalanceDto from, AccountBalanceDto to) {
        return new TransferResultDto(from, to);
    }
}
