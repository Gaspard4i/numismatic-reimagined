package dev.gaspard4i.numismatic.currency;

/**
 * Formatting utilities for currency display. Unit symbol : {@code ¢}.
 *
 * <p>Two complementary forms :
 * <ul>
 *   <li>{@link #formatSimple(long)} → {@code "1234¢"} (pure raw value + symbol)</li>
 *   <li>{@link #format(long)} → {@code "12g34s56b"} compact multi-denom breakdown with {@code ¢} prefix</li>
 * </ul>
 */
public final class CurrencyFormatter {

    public static final String SYMBOL = "¢";

    private CurrencyFormatter() {}

    public static String symbol() {
        return SYMBOL;
    }

    /** Returns {@code "1234¢"}, the raw value followed by the symbol. */
    public static String formatSimple(long raw) {
        if (raw < 0) throw new IllegalArgumentException("raw must be non-negative, got " + raw);
        return raw + SYMBOL;
    }

    /**
     * Returns a compact multi-denomination breakdown :
     * <ul>
     *   <li>{@code 0} → {@code "0¢"}</li>
     *   <li>{@code 1234567} → {@code "1n23g45s67b¢"} (n=netherite, g=gold, s=silver, b=bronze)</li>
     *   <li>{@code 50} → {@code "50b¢"}</li>
     * </ul>
     */
    public static String format(long raw) {
        if (raw < 0) throw new IllegalArgumentException("raw must be non-negative, got " + raw);
        if (raw == 0) return "0" + SYMBOL;

        long[] split = CurrencyResolver.splitValues(raw);
        StringBuilder sb = new StringBuilder();
        // Order MSD first : NETHERITE, GOLD, SILVER, BRONZE.
        appendPart(sb, split[Currency.NETHERITE.ordinal()], "n");
        appendPart(sb, split[Currency.GOLD.ordinal()], "g");
        appendPart(sb, split[Currency.SILVER.ordinal()], "s");
        appendPart(sb, split[Currency.BRONZE.ordinal()], "b");
        sb.append(SYMBOL);
        return sb.toString();
    }

    private static void appendPart(StringBuilder sb, long amount, String suffix) {
        if (amount > 0) {
            sb.append(amount).append(suffix);
        }
    }
}
