package dev.gaspard4i.numismatic.client;

import dev.gaspard4i.numismatic.NumismaticConstants;
import dev.gaspard4i.numismatic.client.screen.PurseScreen;
import dev.gaspard4i.numismatic.currency.Currency;
import dev.gaspard4i.numismatic.network.ClientCurrencyData;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

/**
 * The "inventory purse" widget — a 37x66 panel blitted next to the vanilla
 * inventory. Displays the four denominations (netherite/gold/silver/bronze)
 * top→bottom, each with the coin icon on the left and the current balance
 * on the right. Clicking the widget opens {@link PurseScreen}.
 */
public class PurseInventoryWidget extends AbstractWidget {

    private static final ResourceLocation TEXTURE =
            new ResourceLocation(NumismaticConstants.MOD_ID, "textures/gui/purse_widget.png");

    // Source UV inside purse_widget.png — left panel sits at (0, 5), 37x66.
    private static final int UV_U = 0, UV_V = 5;
    public static final int WIDGET_W = 37;
    public static final int WIDGET_H = 66;

    // Rows: first row baseline at y=5 inside the widget (top-left coin icon).
    // Rows are 12 pixels tall.
    private static final int ROW_HEIGHT = 12;
    private static final int ROW_Y0 = 5;
    private static final int COIN_X = 3;
    private static final int VALUE_X = 21;

    public PurseInventoryWidget(int x, int y) {
        super(x, y, WIDGET_W, WIDGET_H,
                Component.translatable("gui.numismatic_reimagined.purse"));
    }

    @Override
    protected void renderWidget(GuiGraphics g, int mouseX, int mouseY, float partial) {
        // Blit the static background.
        g.blit(TEXTURE, getX(), getY(), UV_U, UV_V, WIDGET_W, WIDGET_H);

        long balance = ClientCurrencyData.getBalance();
        long[] split = splitValues(balance);

        // Draw per-denomination icon + count. Order top→bottom:
        // netherite, gold, silver, bronze.
        Currency[] order = { Currency.NETHERITE, Currency.GOLD, Currency.SILVER, Currency.BRONZE };
        long[] counts = { split[3], split[2], split[1], split[0] };

        for (int i = 0; i < 4; i++) {
            int y = getY() + ROW_Y0 + i * ROW_HEIGHT;
            // Coin icon (vanilla item render — picks up the registered coin textures).
            var coinStack = new net.minecraft.world.item.ItemStack(
                    dev.gaspard4i.numismatic.item.NumismaticItems.getCoinItem(order[i]));
            g.renderItem(coinStack, getX() + COIN_X, y - 4);
            String count = String.valueOf(counts[i]);
            g.drawString(Minecraft.getInstance().font, count,
                    getX() + VALUE_X, y, 0xFFFFFF, false);
        }
    }

    @Override
    public void onClick(double mouseX, double mouseY) {
        Minecraft mc = Minecraft.getInstance();
        mc.setScreen(new PurseScreen(mc.screen));
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput out) {
        this.defaultButtonNarrationText(out);
    }

    private static long[] splitValues(long v) {
        long netherite = v / Currency.NETHERITE.getValue();
        long rest = v % Currency.NETHERITE.getValue();
        long gold = rest / Currency.GOLD.getValue();
        rest %= Currency.GOLD.getValue();
        long silver = rest / Currency.SILVER.getValue();
        long bronze = rest % Currency.SILVER.getValue();
        return new long[]{ bronze, silver, gold, netherite };
    }
}
