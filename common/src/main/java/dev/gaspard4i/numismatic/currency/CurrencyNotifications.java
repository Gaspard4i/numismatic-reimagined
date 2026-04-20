package dev.gaspard4i.numismatic.currency;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.server.level.ServerPlayer;

/**
 * Builds the per-denomination actionbar feedback shown when a player's
 * purse balance changes. Mirrors the format used by the original mod :
 * {@code "+ 12S, 4B"} in green for gains, {@code "- …"} in red for
 * losses, each denomination tinted with its brand colour.
 *
 * <p>Pure string-building — the decision to send comes from the server
 * side (see {@link PlayerCurrencyManager#addBalanceAndTrack}).
 */
public final class CurrencyNotifications {

    /** Hex tint per denomination (matches ShopTier tints + coin textures). */
    private static final int BRONZE_COLOR = 0xCD7F32;
    private static final int SILVER_COLOR = 0xC0C0C0;
    private static final int GOLD_COLOR = 0xFFD700;
    private static final int NETHERITE_COLOR = 0x4A4A4A;

    private CurrencyNotifications() {}

    /** Builds the coloured message component for {@code delta} bronze units. */
    public static Component buildActionbar(long delta) {
        if (delta == 0) return Component.empty();
        boolean gain = delta > 0;
        long abs = Math.abs(delta);

        long netherite = abs / Currency.NETHERITE.getValue();
        long rest = abs % Currency.NETHERITE.getValue();
        long gold = rest / Currency.GOLD.getValue();
        rest %= Currency.GOLD.getValue();
        long silver = rest / Currency.SILVER.getValue();
        long bronze = rest % Currency.SILVER.getValue();

        MutableComponent root = Component.literal(gain ? "+ " : "- ")
                .withStyle(gain ? ChatFormatting.GREEN : ChatFormatting.RED);

        boolean first = true;
        if (netherite > 0) { first = append(root, first, netherite + "N", NETHERITE_COLOR); }
        if (gold > 0)      { first = append(root, first, gold + "G", GOLD_COLOR); }
        if (silver > 0)    { first = append(root, first, silver + "S", SILVER_COLOR); }
        if (bronze > 0)    { first = append(root, first, bronze + "B", BRONZE_COLOR); }
        // Fallback when the value is smaller than 1 bronze (shouldn't happen
        // but avoids an empty "+").
        if (first) append(root, true, abs + "B", BRONZE_COLOR);
        return root;
    }

    /** Sends the actionbar component to the given player, if non-empty. */
    public static void sendActionbar(ServerPlayer player, long delta) {
        if (delta == 0 || player == null) return;
        player.displayClientMessage(buildActionbar(delta), true);
    }

    private static boolean append(MutableComponent root, boolean first,
                                   String text, int rgb) {
        if (!first) {
            root.append(Component.literal(", ")
                    .withStyle(Style.EMPTY.withColor(0xAAAAAA)));
        }
        root.append(Component.literal(text)
                .withStyle(Style.EMPTY.withColor(rgb)));
        return false;
    }
}
