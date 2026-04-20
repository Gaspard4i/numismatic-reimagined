package dev.gaspard4i.numismatic.currency;

/**
 * Pure balance container for a player's purse. No MC dependency.
 * Server-side managers wrap this to persist via SavedData.
 */
public final class PurseAccount {

    private long balance;

    public PurseAccount() {
        this(0L);
    }

    public PurseAccount(long initialBalance) {
        if (initialBalance < 0) {
            throw new IllegalArgumentException("balance must be non-negative, got " + initialBalance);
        }
        this.balance = initialBalance;
    }

    public long getBalance() {
        return balance;
    }

    public void deposit(long amount) {
        if (amount < 0) {
            throw new IllegalArgumentException("deposit must be non-negative, got " + amount);
        }
        balance = Math.addExact(balance, amount);
    }

    public boolean tryWithdraw(long amount) {
        if (amount < 0) {
            throw new IllegalArgumentException("withdraw must be non-negative, got " + amount);
        }
        if (amount > balance) return false;
        balance -= amount;
        return true;
    }

    public void setBalance(long newBalance) {
        if (newBalance < 0) {
            throw new IllegalArgumentException("balance must be non-negative, got " + newBalance);
        }
        balance = newBalance;
    }

    public long[] split() {
        return CurrencyResolver.splitValues(balance);
    }
}
