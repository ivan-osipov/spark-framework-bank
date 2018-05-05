package ru.i_osipov.sfb.services;

import ru.i_osipov.sfb.data.DataStore;

import java.util.UUID;

public class AccountService {

    private final DataStore dataStore;

    public AccountService(DataStore dataStore) {
        this.dataStore = dataStore;
    }

    public UUID create() {
        return dataStore.createAccount(0).getId();
    }

    public boolean delete(UUID accountId) {
        return dataStore.removeAccount(accountId);
    }

    public boolean replenish(UUID accountId, double amount) {
        if(amount <= 0) {
            return false;
        }
        return dataStore.replenish(accountId, amount);
    }


    public boolean exists(UUID accountId) {
        return dataStore.exists(accountId);
    }

    public double getBalance(UUID accountId) {
        return dataStore.getAccountBalance(accountId);
    }

    public boolean transfer(UUID fromAccountId, UUID toAccountId, double amount) {
        if(dataStore.exists(toAccountId) && dataStore.exists(fromAccountId)
                && dataStore.getAccountBalance(fromAccountId) >= amount) {
            return dataStore.transfer(fromAccountId, toAccountId, amount);
        }
        return false;
    }

    public boolean withdrawal(UUID accountId, double amount) {
        if(amount <= 0) {
            return false;
        }
        return dataStore.withdrawal(accountId, amount);
    }

}
