package dev.gaspard4i.numismatic.client.tooltip;

import dev.gaspard4i.numismatic.currency.Currency;
import dev.gaspard4i.numismatic.item.NumismaticItems;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.world.item.ItemStack;

/**
 * Renders a row of coin icons + counts, one line per active denomination,
 * ordered MSD-first (NETHERITE → GOLD → SILVER → BRONZE).
 */
public class CurrencyTooltipComponent implements ClientTooltipComponent {

    private static final int ICON_SIZE = 18;
    private static final int LINE_HEIGHT = 18;
    private static final int TEXT_OFFSET_X = 20;
    private static final int TEXT_OFFSET_Y = 5;

    private final CurrencyTooltipData data;

    public CurrencyTooltipComponent(CurrencyTooltipData data) {
        this.data = data;
    }

    @Override
    public int getHeight() {
        return Math.max(1, data.activeLines()) * LINE_HEIGHT;
    }

    @Override
    public int getWidth(Font font) {
        int max = 0;
        for (Currency c : orderMsdFirst()) {
            long amt = data.amountFor(c);
            if (amt <= 0) continue;
            int w = TEXT_OFFSET_X + font.width(Long.toString(amt));
            if (w > max) max = w;
        }
        return Math.max(max, 40);
    }

    @Override
    public void renderImage(Font font, int x, int y, GuiGraphics g) {
        int row = 0;
        for (Currency c : orderMsdFirst()) {
            long amt = data.amountFor(c);
            if (amt <= 0) continue;
            int ry = y + row * LINE_HEIGHT;
            ItemStack icon = new ItemStack(NumismaticItems.getCoinItem(c));
            g.renderFakeItem(icon, x, ry);
            g.drawString(font, Long.toString(amt),
                    x + TEXT_OFFSET_X, ry + TEXT_OFFSET_Y, 0xFFFFFF, true);
            row++;
        }
    }

    private static Currency[] orderMsdFirst() {
        return new Currency[]{Currency.NETHERITE, Currency.GOLD, Currency.SILVER, Currency.BRONZE};
    }
}
