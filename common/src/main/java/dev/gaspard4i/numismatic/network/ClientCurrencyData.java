package dev.gaspard4i.numismatic.network;

import dev.architectury.networking.NetworkManager;
import dev.gaspard4i.numismatic.currency.CurrencyResolver;

/**
 * Client-side cache for the local player's currency balance.
 * Updated via S2C packets from the server.
 */
public final class ClientCurrencyData {

    private static long balance = 0;

    private ClientCurrencyData() {}

    public static void registerClientReceivers() {
        NetworkManager.registerReceiver(
                NetworkManager.Side.S2C,
                NumismaticNetworking.SYNC_CURRENCY_S2C,
                (buf, context) -> {
                    long newBalance = buf.readLong();
                    context.queue(() -> balance = newBalance);
                }
        );
    }

    /**
     * @return the cached balance in bronze units
     */
    public static long getBalance() {
        return balance;
    }

    /**
     * @return the cached balance formatted as a string (e.g., "1N 2G 3S 4B")
     */
    public static String getFormattedBalance() {
        return CurrencyResolver.formatValue(balance);
    }

    /**
     * Resets the client cache (called on disconnect).
     */
    public static void reset() {
        balance = 0;
    }
}
