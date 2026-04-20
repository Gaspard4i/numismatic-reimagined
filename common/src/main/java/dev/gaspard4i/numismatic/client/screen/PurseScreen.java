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
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

/**
 * Purse popup — faithful port of the original {@code purse.xml} layout
 * but with 4 denominations (netherite / gold / silver / bronze) instead
 * of the upstream 3. Background is the left panel of
 * {@code purse_widget.png} (UV 0, 5, 37×66); increment/decrement buttons
 * are 9×5 texture regions (UV 37,24 and 46,24); the extract button is
 * 24×8 at UV (37, 0).
 *
 * <p>Shift-click on + or – applies {@link PurseExtractLogic#SHIFT_MULTIPLIER}.
 */
public class PurseScreen extends Screen {

    private static final ResourceLocation TEXTURE =
            new ResourceLocation(NumismaticConstants.MOD_ID, "textures/gui/purse_widget.png");

    // Left panel of our atlas : starts at (0, 5), 37 wide × 66 tall
    // (6 extra pixels vs the upstream 59 so the 4th denomination row fits).
    private static final int PANEL_UV_U = 0, PANEL_UV_V = 5;
    private static final int PANEL_W = 37;
    private static final int PANEL_H = 66;

    // Adjustment buttons (match upstream 9×5 spritelets at v=24).
    private static final int BTN_W = 9;
    private static final int BTN_H = 5;
    private static final int BTN_PLUS_UV_U = 37;
    private static final int BTN_MINUS_UV_U = 46;
    private static final int BTN_UV_V = 24;

    // Extract button (upstream 24×8 at (37, 0)).
    private static final int EXTRACT_W = 24;
    private static final int EXTRACT_H = 8;
    private static final int EXTRACT_UV_U = 37;
    private static final int EXTRACT_UV_V = 0;

    // Row geometry (relative to panel top-left).
    private static final int ROW_Y0 = 6;
    private static final int ROW_HEIGHT = 12;
    private static final int ICON_X = 3;      // coin icon column
    private static final int VALUE_X = 14;    // balance text column (8 px wide)
    private static final int PLUS_X = 24;     // +/- column
    private static final int MINUS_X = 24;
    private static final int EXTRACT_REL_X = 6;
    private static final int EXTRACT_REL_Y = 54;

    // Top-down currency order, largest denom first.
    private static final Currency[] ORDER = {
            Currency.NETHERITE, Currency.GOLD, Currency.SILVER, Currency.BRONZE
    };

    private final Screen parent;
    private int leftPos;
    private int topPos;

    // Pending withdrawal amounts per denomination (indexed by Currency.ordinal()).
    private final long[] pending = new long[Currency.values().length];

    public PurseScreen(Screen parent) {
        super(Component.translatable("gui.numismatic_reimagined.purse"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        super.init();
        leftPos = (width - PANEL_W) / 2;
        topPos = (height - PANEL_H) / 2;

        // One +/- pair per row (4 rows).
        for (int i = 0; i < ORDER.length; i++) {
            Currency c = ORDER[i];
            int rowY = topPos + ROW_Y0 + i * ROW_HEIGHT;
            addRenderableWidget(new AdjustButton(
                    leftPos + PLUS_X, rowY, BTN_PLUS_UV_U, true,
                    () -> pend(c, +1)));
            addRenderableWidget(new AdjustButton(
                    leftPos + MINUS_X, rowY + BTN_H + 1, BTN_MINUS_UV_U, false,
                    () -> pend(c, -1)));
        }

        // Extract (texture button embedded in the panel).
        addRenderableWidget(new ExtractButton(
                leftPos + EXTRACT_REL_X, topPos + EXTRACT_REL_Y,
                this::onExtract));
    }

    private void pend(Currency c, int delta) {
        boolean shift = hasShiftDown();
        long balance = ClientCurrencyData.getBalance();
        if (delta > 0) PurseExtractLogic.increment(pending, c, balance, shift);
        else PurseExtractLogic.decrement(pending, c, shift);
    }

    private long totalPending() { return PurseExtractLogic.totalPending(pending); }

    private void onExtract() {
        long amount = totalPending();
        if (amount <= 0) return;
        NumismaticNetworking.sendWithdraw(amount);
        for (int i = 0; i < pending.length; i++) pending[i] = 0;
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partial) {
        renderBackground(g);

        // Panel background.
        g.blit(TEXTURE, leftPos, topPos, PANEL_UV_U, PANEL_UV_V, PANEL_W, PANEL_H);

        // Coin icon + value text per row.
        long balance = ClientCurrencyData.getBalance();
        long[] split = splitValues(balance);
        long[] owned = { split[3], split[2], split[1], split[0] };

        for (int i = 0; i < ORDER.length; i++) {
            int y = topPos + ROW_Y0 + i * ROW_HEIGHT;
            ItemStack coinStack = new ItemStack(NumismaticItems.getCoinItem(ORDER[i]));
            // Coin icon rendered at 8×8 (renderFakeItem auto-scales down).
            g.renderFakeItem(coinStack, leftPos + ICON_X, y);
            long p = pending[ORDER[i].ordinal()];
            String text = p > 0 ? (owned[i] - p) + "(-" + p + ")" : String.valueOf(owned[i]);
            // Clip so we never bleed past the +/- column.
            g.drawString(font, text, leftPos + VALUE_X, y + 2, 0xFFFFFF, false);
        }

        super.render(g, mouseX, mouseY, partial);

        // Title above, pending total below.
        g.drawCenteredString(font, title, leftPos + PANEL_W / 2, topPos - 12, 0xFFFFFF);
        long total = totalPending();
        if (total > 0) {
            g.drawCenteredString(font, "Extract " + String.format("%,d", total),
                    leftPos + PANEL_W / 2, topPos + PANEL_H + 4, 0xFFD700);
        }
    }

    @Override public boolean isPauseScreen() { return false; }

    @Override
    public boolean mouseClicked(double x, double y, int btn) {
        // Click outside the panel closes the popup (familiar UX).
        if (btn == 0 && !inPanel(x, y)) {
            Minecraft.getInstance().setScreen(parent);
            return true;
        }
        return super.mouseClicked(x, y, btn);
    }

    private boolean inPanel(double x, double y) {
        return x >= leftPos && x < leftPos + PANEL_W
                && y >= topPos && y < topPos + PANEL_H;
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

    /** Small +/- button (9×5 texture sprite, hover uses a darker overlay). */
    private static final class AdjustButton extends AbstractWidget {
        private final int uvU;
        private final Runnable onClick;

        AdjustButton(int x, int y, int uvU, boolean plus, Runnable onClick) {
            super(x, y, BTN_W, BTN_H, Component.literal(plus ? "+" : "-"));
            this.uvU = uvU;
            this.onClick = onClick;
        }

        @Override
        protected void renderWidget(GuiGraphics g, int mouseX, int mouseY, float partial) {
            g.blit(TEXTURE, getX(), getY(), uvU, BTN_UV_V, BTN_W, BTN_H);
            if (isHovered()) {
                g.fill(getX(), getY(), getX() + width, getY() + height, 0x30FFFFFF);
            }
        }

        @Override
        public void onClick(double mouseX, double mouseY) { onClick.run(); }

        @Override
        protected void updateWidgetNarration(NarrationElementOutput out) {
            this.defaultButtonNarrationText(out);
        }
    }

    /** Extract texture button (24×8 sprite at UV 37, 0). */
    private static final class ExtractButton extends AbstractWidget {
        private final Runnable onClick;

        ExtractButton(int x, int y, Runnable onClick) {
            super(x, y, EXTRACT_W, EXTRACT_H,
                    Component.translatable("gui.numismatic_reimagined.purse.extract"));
            this.onClick = onClick;
        }

        @Override
        protected void renderWidget(GuiGraphics g, int mouseX, int mouseY, float partial) {
            g.blit(TEXTURE, getX(), getY(), EXTRACT_UV_U, EXTRACT_UV_V, EXTRACT_W, EXTRACT_H);
            if (isHovered()) {
                g.fill(getX(), getY(), getX() + width, getY() + height, 0x40FFFFFF);
            }
        }

        @Override
        public void onClick(double mouseX, double mouseY) { onClick.run(); }

        @Override
        protected void updateWidgetNarration(NarrationElementOutput out) {
            this.defaultButtonNarrationText(out);
        }
    }
}
