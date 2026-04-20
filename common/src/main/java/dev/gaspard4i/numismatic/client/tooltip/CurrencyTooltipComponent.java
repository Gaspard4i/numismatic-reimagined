package dev.gaspard4i.numismatic.client.tooltip;

import dev.gaspard4i.numismatic.currency.Currency;
import dev.gaspard4i.numismatic.item.NumismaticItems;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.world.item.ItemStack;

/**
 * Renders a {@link CurrencyTooltipData} as a compact vertical stack of
 * coin icons + counts (netherite → gold → silver → bronze, largest first).
 * Empty denominations are skipped so a "1N 20B" tooltip shows 2 rows
 * rather than 4.
 */
public class CurrencyTooltipComponent implements ClientTooltipComponent {

    private static final int ROW_HEIGHT = 10;
    private static final int ICON_SIZE = 9;
    private static final int ICON_X = 0;
    private static final int TEXT_X = 12;

    private final CurrencyTooltipData data;

    public CurrencyTooltipComponent(CurrencyTooltipData data) {
        this.data = data;
    }

    private record Line(Currency currency, long count) {}

    private Line[] visibleLines() {
        // Top-down: netherite → gold → silver → bronze (largest denom first).
        Line[] all = {
                new Line(Currency.NETHERITE, data.netherite()),
                new Line(Currency.GOLD, data.gold()),
                new Line(Currency.SILVER, data.silver()),
                new Line(Currency.BRONZE, data.bronze())
        };
        int live = 0;
        for (Line l : all) if (l.count > 0) live++;
        if (live == 0) {
            // Empty wallet → single "0 bronze" line so the tooltip is not blank.
            return new Line[]{ new Line(Currency.BRONZE, 0L) };
        }
        Line[] out = new Line[live];
        int idx = 0;
        for (Line l : all) if (l.count > 0) out[idx++] = l;
        return out;
    }

    @Override
    public int getHeight() {
        return visibleLines().length * ROW_HEIGHT;
    }

    @Override
    public int getWidth(Font font) {
        int max = 0;
        for (Line l : visibleLines()) {
            int w = TEXT_X + font.width(String.valueOf(l.count));
            if (w > max) max = w;
        }
        return max;
    }

    @Override
    public void renderImage(Font font, int x, int y, GuiGraphics g) {
        Line[] lines = visibleLines();
        for (int i = 0; i < lines.length; i++) {
            Line l = lines[i];
            int rowY = y + i * ROW_HEIGHT;
            ItemStack icon = new ItemStack(NumismaticItems.getCoinItem(l.currency));
            g.renderFakeItem(icon, x + ICON_X, rowY);
            // Lazy vertical centering: icon 9px, row 10px → 1px margin bottom.
            int textY = rowY + (ROW_HEIGHT - font.lineHeight) / 2 + 1;
            g.drawString(font, String.valueOf(l.count), x + TEXT_X, textY,
                    0xFFFFFFFF, true);
        }
    }
}
