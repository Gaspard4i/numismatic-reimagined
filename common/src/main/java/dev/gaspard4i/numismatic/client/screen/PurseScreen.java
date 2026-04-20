package dev.gaspard4i.numismatic.client.screen;

import dev.gaspard4i.numismatic.NumismaticConstants;
import dev.gaspard4i.numismatic.client.PurseExtractLogic;
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
 * Purse popup — four rows (N/G/S/B) with +/- buttons, then an Extract
 * footer. Right panel from {@code purse_widget.png} drawn as background.
 */
public class PurseScreen extends Screen {

    private static final ResourceLocation TEXTURE =
            new ResourceLocation(NumismaticConstants.MOD_ID, "textures/gui/purse_widget.png");

    // Panel region : right half of the atlas (x=82, y=0, 44x71).
    private static final int UV_U = 82, UV_V = 0;
    private static final int PANEL_W = 44;
    private static final int PANEL_H = 71;

    // Plus / minus glyph UV regions in the same atlas.
    private static final int BTN_PLUS_UV_U = 38, BTN_PLUS_UV_V = 0;
    private static final int BTN_PLUS_W = 23, BTN_PLUS_H = 8;
    private static final int BTN_MINUS_UV_U = 38, BTN_MINUS_UV_V = 24;
    private static final int BTN_MINUS_W = 18, BTN_MINUS_H = 9;

    // Row geometry — 4 rows of 12 px each, first row baseline at y=5.
    private static final int ROW_Y0 = 5;
    private static final int ROW_HEIGHT = 12;
    private static final int COIN_X = 3;
    private static final int VALUE_X = 21;

    private final Screen parent;
    private int leftPos;
    private int topPos;

    private final long[] pending = new long[Currency.values().length];

    public PurseScreen(Screen parent) {
        super(Component.translatable("gui.numismatic_reimagined.purse"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        super.init();
        leftPos = (width - PANEL_W) / 2;
        topPos = (height - PANEL_H - 24) / 2;

        // Per-row +/- buttons.
        addRow(0, Currency.NETHERITE);
        addRow(1, Currency.GOLD);
        addRow(2, Currency.SILVER);
        addRow(3, Currency.BRONZE);

        // Extract footer.
        addRenderableWidget(Button.builder(
                Component.translatable("gui.numismatic_reimagined.purse.extract"),
                b -> onExtract())
                .bounds(leftPos, topPos + PANEL_H + 2, PANEL_W, 16)
                .build());

        // Close footer : return to the previous screen.
        addRenderableWidget(Button.builder(
                Component.literal("X"),
                b -> Minecraft.getInstance().setScreen(parent))
                .bounds(leftPos + PANEL_W + 2, topPos, 14, 14)
                .build());
    }

    private void addRow(int rowIndex, Currency c) {
        int y = topPos + ROW_Y0 + rowIndex * ROW_HEIGHT;
        int plusX = leftPos + PANEL_W - BTN_PLUS_W - 2;
        int minusX = leftPos + PANEL_W - BTN_MINUS_W - 2;
        addRenderableWidget(new SmallTexButton(
                plusX, y - 1, BTN_PLUS_W, BTN_PLUS_H,
                BTN_PLUS_UV_U, BTN_PLUS_UV_V, () -> pend(c, +1)));
        addRenderableWidget(new SmallTexButton(
                minusX, y + BTN_PLUS_H, BTN_MINUS_W, BTN_MINUS_H,
                BTN_MINUS_UV_U, BTN_MINUS_UV_V, () -> pend(c, -1)));
    }

    private void pend(Currency c, int delta) {
        boolean shift = hasShiftDown();
        long balance = ClientCurrencyData.getBalance();
        if (delta > 0) PurseExtractLogic.increment(pending, c, balance, shift);
        else PurseExtractLogic.decrement(pending, c, shift);
    }

    private long totalPending() {
        return PurseExtractLogic.totalPending(pending);
    }

    private void onExtract() {
        long amount = totalPending();
        if (amount <= 0) return;
        NumismaticNetworking.sendWithdraw(amount);
        for (int i = 0; i < pending.length; i++) pending[i] = 0;
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partial) {
        renderBackground(g);
        g.blit(TEXTURE, leftPos, topPos, UV_U, UV_V, PANEL_W, PANEL_H);

        long balance = ClientCurrencyData.getBalance();
        long[] split = splitValues(balance);

        Currency[] order = { Currency.NETHERITE, Currency.GOLD, Currency.SILVER, Currency.BRONZE };
        long[] owned = { split[3], split[2], split[1], split[0] };

        for (int i = 0; i < 4; i++) {
            int y = topPos + ROW_Y0 + i * ROW_HEIGHT;
            ItemStack coinStack = new ItemStack(NumismaticItems.getCoinItem(order[i]));
            g.renderItem(coinStack, leftPos + COIN_X, y - 4);
            long p = pending[order[i].ordinal()];
            String text = p > 0 ? (owned[i] - p) + " (-" + p + ")" : String.valueOf(owned[i]);
            g.drawString(font, text, leftPos + VALUE_X, y, 0xFFFFFF, false);
        }

        super.render(g, mouseX, mouseY, partial);

        g.drawCenteredString(font, title, leftPos + PANEL_W / 2, topPos - 12, 0xFFFFFF);
        long total = totalPending();
        if (total > 0) {
            g.drawCenteredString(font, "Extract " + String.format("%,d", total),
                    leftPos + PANEL_W / 2, topPos + PANEL_H + 22, 0xFFD700);
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
