package dev.gaspard4i.numismatic.client.screen;

import dev.gaspard4i.numismatic.NumismaticConstants;
import dev.gaspard4i.numismatic.currency.Currency;
import dev.gaspard4i.numismatic.item.NumismaticItems;
import dev.gaspard4i.numismatic.network.ClientCurrencyData;
import dev.gaspard4i.numismatic.network.NumismaticNetworking;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

/**
 * Purse popup screen. Uses the right panel of {@code purse_widget.png}
 * (UV 82,0, 44x71) as background. Four rows (netherite/gold/silver/bronze)
 * each show the coin icon, the current balance, and a +/- pair of buttons
 * that accumulate the amount to withdraw. Extract sends the total to the
 * server.
 */
public class PurseScreen extends Screen {

    private static final ResourceLocation TEXTURE =
            new ResourceLocation(NumismaticConstants.MOD_ID, "textures/gui/purse_widget.png");

    // Right panel UV: (82, 0), size 44x71.
    private static final int UV_U = 82, UV_V = 0;
    private static final int PANEL_W = 44;
    private static final int PANEL_H = 71;

    // Button UVs (small icons at top of the atlas).
    // "+" / "extract" button family: UV (38, 0) normal, (38, 8) hover, 23x8.
    private static final int BTN_PLUS_UV_U = 38, BTN_PLUS_UV_V = 0;
    private static final int BTN_PLUS_W = 23, BTN_PLUS_H = 8;
    // "-" button: UV (38, 24), 18x9.
    private static final int BTN_MINUS_UV_U = 38, BTN_MINUS_UV_V = 24;
    private static final int BTN_MINUS_W = 18, BTN_MINUS_H = 9;

    // Row geometry (relative to panel top-left).
    private static final int ROW_Y0 = 5;
    private static final int ROW_HEIGHT = 12;
    private static final int COIN_X = 3;
    private static final int VALUE_X = 21;

    private final Screen parent;
    private int leftPos;
    private int topPos;

    // Pending withdrawal amounts per denomination.
    private final long[] pending = new long[4]; // indices match Currency.ordinal()

    public PurseScreen(Screen parent) {
        super(Component.translatable("gui.numismatic_reimagined.purse"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        super.init();
        leftPos = (width - PANEL_W) / 2;
        topPos = (height - PANEL_H - 20) / 2;

        // Per-row +/- buttons placed to the right of the value column.
        addRow(0, Currency.NETHERITE);
        addRow(1, Currency.GOLD);
        addRow(2, Currency.SILVER);
        addRow(3, Currency.BRONZE);

        // Extract button below the panel (full-width below).
        addRenderableWidget(Button.builder(
                Component.translatable("gui.numismatic_reimagined.purse.extract"),
                b -> onExtract())
                .bounds(leftPos, topPos + PANEL_H + 2, PANEL_W, 16)
                .build());
    }

    private void addRow(int rowIndex, Currency c) {
        int y = topPos + ROW_Y0 + rowIndex * ROW_HEIGHT;
        int btnX = leftPos + PANEL_W - BTN_MINUS_W - 2;
        // Minus button on left, Plus button right next to it.
        addRenderableWidget(new SmallTexButton(
                btnX - BTN_PLUS_W - 1, y - 1, BTN_PLUS_W, BTN_PLUS_H,
                BTN_PLUS_UV_U, BTN_PLUS_UV_V, b -> pend(c, +1)));
        addRenderableWidget(new SmallTexButton(
                btnX, y - 1, BTN_MINUS_W, BTN_MINUS_H,
                BTN_MINUS_UV_U, BTN_MINUS_UV_V, b -> pend(c, -1)));
    }

    private void pend(Currency c, int delta) {
        int idx = c.ordinal();
        long step = hasShiftDown() ? 10 : 1;
        long newVal = pending[idx] + delta * step;
        if (newVal < 0) newVal = 0;

        // Cap at the player's balance for that denomination.
        long max = ClientCurrencyData.getBalance() / c.getValue();
        if (newVal > max) newVal = max;
        pending[idx] = newVal;
    }

    private long totalPending() {
        long total = 0;
        for (Currency c : Currency.values()) {
            total += pending[c.ordinal()] * c.getValue();
        }
        return total;
    }

    private void onExtract() {
        long amount = totalPending();
        if (amount <= 0) return;
        NumismaticNetworking.sendWithdraw(amount);
        Minecraft.getInstance().setScreen(parent);
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partial) {
        renderBackground(g);
        g.blit(TEXTURE, leftPos, topPos, UV_U, UV_V, PANEL_W, PANEL_H);

        long balance = ClientCurrencyData.getBalance();
        long[] split = splitValues(balance);

        Currency[] order = { Currency.NETHERITE, Currency.GOLD, Currency.SILVER, Currency.BRONZE };
        long[] counts = { split[3], split[2], split[1], split[0] };

        for (int i = 0; i < 4; i++) {
            int y = topPos + ROW_Y0 + i * ROW_HEIGHT;
            ItemStack coinStack = new ItemStack(NumismaticItems.getCoinItem(order[i]));
            g.renderItem(coinStack, leftPos + COIN_X, y - 4);
            String owned = String.valueOf(counts[i]);
            String text = owned;
            long p = pending[order[i].ordinal()];
            if (p > 0) text = (counts[i] - p) + " (-" + p + ")";
            g.drawString(font, text, leftPos + VALUE_X, y, 0xFFFFFF, false);
        }

        super.render(g, mouseX, mouseY, partial);

        // Title above the panel.
        g.drawCenteredString(font, title, leftPos + PANEL_W / 2, topPos - 12, 0xFFFFFF);
        long total = totalPending();
        if (total > 0) {
            g.drawCenteredString(font, "Extract " + total,
                    leftPos + PANEL_W / 2, topPos + PANEL_H + 20, 0xFFD700);
        }
    }

    @Override
    public boolean isPauseScreen() { return false; }

    private static long[] splitValues(long v) {
        long netherite = v / Currency.NETHERITE.getValue();
        long rest = v % Currency.NETHERITE.getValue();
        long gold = rest / Currency.GOLD.getValue();
        rest %= Currency.GOLD.getValue();
        long silver = rest / Currency.SILVER.getValue();
        long bronze = rest % Currency.SILVER.getValue();
        return new long[]{ bronze, silver, gold, netherite };
    }

    /** Button rendered from a flat UV region of purse_widget.png. */
    private static final class SmallTexButton extends AbstractWidget {
        private final int uvU, uvV;
        private final Runnable onClick;

        SmallTexButton(int x, int y, int w, int h, int uvU, int uvV, Runnable onClick) {
            super(x, y, w, h, Component.empty());
            this.uvU = uvU;
            this.uvV = uvV;
            this.onClick = onClick;
        }

        SmallTexButton(int x, int y, int w, int h, int uvU, int uvV,
                       net.minecraft.client.gui.components.Button.OnPress press) {
            this(x, y, w, h, uvU, uvV, () -> press.onPress(null));
        }

        @Override
        protected void renderWidget(GuiGraphics g, int mouseX, int mouseY, float partial) {
            int v = uvV;
            if (isHovered()) v += this.height;
            g.blit(TEXTURE, getX(), getY(), uvU, v, this.width, this.height);
        }

        @Override
        public void onClick(double mouseX, double mouseY) { onClick.run(); }

        @Override
        protected void updateWidgetNarration(NarrationElementOutput out) {
            this.defaultButtonNarrationText(out);
        }
    }
}
