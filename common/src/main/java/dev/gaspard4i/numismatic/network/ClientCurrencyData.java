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

    public static long getBalance() {
        return balance;
    }

    public static String getFormattedBalance() {
        return CurrencyResolver.formatValue(balance);
    }

    public static void reset() {
        balance = 0;
    }
}
