package dev.gaspard4i.numismatic.client.tooltip;

import net.minecraft.world.inventory.tooltip.TooltipComponent;

/**
 * Marker tooltip payload carrying a split-down currency amount. Consumed
 * by {@link CurrencyTooltipComponent} (client-only) to render the coin
 * icons + counts stack.
 *
 * <p>The data is split per denomination so the renderer does not have to
 * re-do the math every frame.
 */
public record CurrencyTooltipData(long bronze, long silver, long gold, long netherite)
        implements TooltipComponent {

    /** Decomposes a raw bronze amount into its 4-denomination components. */
    public static CurrencyTooltipData ofRawValue(long rawBronze) {
        long remaining = Math.max(0, rawBronze);
        long netherite = remaining / 1_000_000L;
        remaining %= 1_000_000L;
        long gold = remaining / 10_000L;
        remaining %= 10_000L;
        long silver = remaining / 100L;
        long bronze = remaining % 100L;
        return new CurrencyTooltipData(bronze, silver, gold, netherite);
    }

    /** Number of denominations with a non-zero count. Drives the tooltip height. */
    public int activeLines() {
        int n = 0;
        if (bronze > 0) n++;
        if (silver > 0) n++;
        if (gold > 0) n++;
        if (netherite > 0) n++;
        return n;
    }

    /** True when every denomination is zero. */
    public boolean isEmpty() {
        return bronze == 0 && silver == 0 && gold == 0 && netherite == 0;
    }
}
