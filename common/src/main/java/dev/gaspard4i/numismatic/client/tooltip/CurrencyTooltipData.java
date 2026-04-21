package dev.gaspard4i.numismatic.client.tooltip;

import dev.gaspard4i.numismatic.currency.Currency;
import dev.gaspard4i.numismatic.currency.CurrencyResolver;
import net.minecraft.world.inventory.tooltip.TooltipComponent;

/**
 * Tooltip data carrying the split per denomination of a stack's raw value.
 * Rendered by {@link CurrencyTooltipComponent} as a row of coin icons + counts,
 * matching the upstream wisp-forest/numismatic-overhaul presentation.
 */
public record CurrencyTooltipData(long[] split) implements TooltipComponent {

    public static CurrencyTooltipData ofRawValue(long raw) {
        return new CurrencyTooltipData(CurrencyResolver.splitValues(Math.max(0L, raw)));
    }

    public long amountFor(Currency currency) {
        return split[currency.ordinal()];
    }

    public int activeLines() {
        int n = 0;
        for (long v : split) if (v > 0) n++;
        return n;
    }
}
