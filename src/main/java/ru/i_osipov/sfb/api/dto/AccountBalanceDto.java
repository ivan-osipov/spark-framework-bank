package ru.i_osipov.sfb.api.dto;

import java.util.UUID;

public class AccountBalanceDto {

    public UUID accountId;
    public double balance;

    public AccountBalanceDto(UUID accountId, double balance) {
        this.accountId = accountId;
        this.balance = balance;
    }

    public static AccountBalanceDto create(UUID accountId, double amount) {
        return new AccountBalanceDto(accountId, amount);
    }
}
