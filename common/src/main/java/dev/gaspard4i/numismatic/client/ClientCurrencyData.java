package dev.gaspard4i.numismatic.client;

/**
 * Client-side cache of the player's balance, updated via sync packets.
 */
public final class ClientCurrencyData {

    private ClientCurrencyData() {}

    private static long balance = 0L;

    public static long getBalance() {
        return balance;
    }

    public static void setBalance(long newBalance) {
        balance = Math.max(0L, newBalance);
    }
}
