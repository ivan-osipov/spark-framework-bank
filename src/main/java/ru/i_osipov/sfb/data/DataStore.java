package ru.i_osipov.sfb.data;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.i_osipov.sfb.api.MoneyConverter;
import ru.i_osipov.sfb.data.model.Account;
import ru.i_osipov.sfb.data.model.Identifiable;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Supplier;

public class DataStore {

    private static final Logger logger = LoggerFactory.getLogger(DataStore.class);

    private MoneyConverter moneyConverter;

    private Map<UUID, Account> accounts = new ConcurrentHashMap<>();
    private LockManager lockManager = new LockManager();

    public DataStore(MoneyConverter moneyConverter) {
        this.moneyConverter = moneyConverter;
    }

    public Account createAccount(double balance) {
        Account account = new Account(moneyConverter.convertToInternal(balance));
        accounts.put(account.getId(), account);
        lockManager.locks.put(account.getId(), new ReentrantReadWriteLock());

        return account;
    }

    public boolean removeAccount(UUID accountId) {
        Account account = accounts.remove(accountId);
        lockManager.locks.remove(accountId);
        return account != null;
    }

    public void removeAllAccounts() {
        accounts.clear();
        lockManager.locks.clear();
    }

    public boolean replenish(UUID accountId, double amount) {
        checkInformationLoss(amount);
        return internalReplenish(accountId, moneyConverter.convertToInternal(amount));
    }

    private boolean internalReplenish(UUID accountId, long amount) {
        return Optional.ofNullable(accounts.get(accountId))
                .map(account -> {
                    return lockManager.writeWithoutResult(account, () -> account.replenish(amount));
                })
                .orElse(false);
    }

    public double getAccountBalance(UUID accountId) {
        return moneyConverter.convertToExternal(getInternalAccountBalance(accountId));
    }

    private long getInternalAccountBalance(UUID accountId) {
        Account account = lockManager.read(accountId, () -> accounts.get(accountId));

        return Optional.ofNullable(account)
                .map(Account::getMoneyAmount)
                .orElseThrow(() -> new IllegalStateException("A balance request for not existed account"));
    }

    public boolean exists(UUID accountId) {
        return accounts.containsKey(accountId);
    }

    public boolean transfer(UUID fromAccountId, UUID toAccountId, double amount) {
        checkInformationLoss(amount);
        Optional.ofNullable(accounts.get(fromAccountId))
                .orElseThrow(() -> new IllegalStateException("Attempt to transfer money from not existed account"));
        Optional.ofNullable(accounts.get(toAccountId))
                .orElseThrow(() -> new IllegalStateException("Attempt to transfer money to not existed account"));

        long internalAmount = moneyConverter.convertToInternal(amount);
        if (getInternalAccountBalance(fromAccountId) < internalAmount) {
            return false;
        }

        boolean successful = freeze(fromAccountId, internalAmount);
        if (successful) {
            successful = internalReplenish(toAccountId, internalAmount);
            if(successful) {
                actualWithdrawal(fromAccountId, internalAmount);
                logger.info("The successful transaction from {} to {} for {}",
                        fromAccountId, toAccountId, amount);
            } else {
                unfreeze(fromAccountId, internalAmount);
                logger.warn("The not successful transaction happened. From {} to {} for {}",
                        fromAccountId, toAccountId, amount);
            }
        }

        return successful;
    }

    public boolean withdrawal(UUID accountId, double amount) {
        checkInformationLoss(amount);
        long internalAmount = moneyConverter.convertToInternal(amount);
        boolean successful = freeze(accountId, internalAmount);
        if(successful) {
            successful = actualWithdrawal(accountId, internalAmount);
            if(!successful) {
                unfreeze(accountId, internalAmount);
            }
        }
        return successful;
    }

    private boolean actualWithdrawal(UUID accountId, long amount) {
        return Optional.ofNullable(accounts.get(accountId))
                .map(account -> {
                    return lockManager.write(account, () -> account.withdrawal(amount));
                })
                .orElse(false);
    }

    private boolean freeze(UUID accountId, long amount) {
        return Optional.ofNullable(accounts.get(accountId))
                .map(account -> {
                    return lockManager.write(account, () -> account.freeze(amount));
                })
                .orElse(false);
    }

    private void unfreeze(UUID accountId, long amount) {
        Optional.ofNullable(accounts.get(accountId))
                .ifPresent(account -> lockManager.write(account, () -> account.unfreeze(amount)));
    }

    private void checkInformationLoss(double value) {
        moneyConverter.checkInformationLoss(value);
    }

    private class LockManager {
        private Map<UUID, ReadWriteLock> locks = new ConcurrentHashMap<>();

        <T extends Identifiable> boolean writeWithoutResult(T entity, Runnable action) {
            return Optional.ofNullable(locks.get(entity.getId()))
                    .map(lock -> {
                        lock.writeLock().lock();
                        try {
                            action.run();
                        } finally {
                            lock.writeLock().unlock();
                        }
                        return true;
                    }).orElse(false);
        }

        <T extends Identifiable> boolean write(T entity, Supplier<Boolean> action) {
            return Optional.ofNullable(locks.get(entity.getId()))
                    .map(lock -> {
                        lock.writeLock().lock();
                        try {
                            return action.get();
                        } finally {
                            lock.writeLock().unlock();
                        }
                    }).orElse(false);
        }

        <T extends Identifiable> T read(UUID entityId, Supplier<T> supplier) {
            return Optional.ofNullable(locks.get(entityId))
                    .map(lock -> {
                        lock.readLock().lock();
                        try {
                            return supplier.get();
                        } finally {
                            lock.readLock().unlock();
                        }
                    }).orElse(null);
        }
    }

}
