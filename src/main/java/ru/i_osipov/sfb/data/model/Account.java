package ru.i_osipov.sfb.data.model;

import static ru.i_osipov.sfb.Preconditions.checkArgument;

public class Account extends Identifiable {

    private long actualAmount;

    private long frozenAmount;

    public Account(long actualAmount) {
        this(actualAmount, 0);
    }

    public Account(long actualAmount, long frozenAmount) {
        checkArgument(frozenAmount <= actualAmount,
                "Frozen amount should be less or equal actual amount");
        this.actualAmount = actualAmount;
        this.frozenAmount = frozenAmount;
    }

    public void replenish(long amount) {
        actualAmount += amount;
    }

    public boolean withdrawal(long amount) {
        if (amount > frozenAmount || amount > actualAmount) {
            return false;
        }
        actualAmount -= amount;
        frozenAmount -= amount;
        return true;
    }

    public boolean freeze(long amount) {
        if (amount > actualAmount) {
            return false;
        }
        frozenAmount += amount;
        return true;
    }

    public boolean unfreeze(long amount) {
        if(frozenAmount < amount) {
            return false;
        }
        frozenAmount -= amount;
        return true;
    }

    public long getMoneyAmount() {
        return actualAmount - frozenAmount;
    }
}
