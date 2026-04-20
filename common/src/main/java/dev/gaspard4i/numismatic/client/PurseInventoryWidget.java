package dev.gaspard4i.numismatic.client;

import dev.gaspard4i.numismatic.NumismaticConstants;
import dev.gaspard4i.numismatic.currency.Currency;
import dev.gaspard4i.numismatic.item.NumismaticItems;
import dev.gaspard4i.numismatic.network.ClientCurrencyData;
import dev.gaspard4i.numismatic.network.NumismaticNetworking;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

/**
 * Purse affordance on the vanilla inventory: a money-bag button that,
 * when clicked, unfolds a popup rendered on top of the existing inventory
 * screen (no new Screen is opened). Mirrors the upstream mod's
 * {@code PurseLayerElement}.
 *
 * <p>The widget stays active the whole time the inventory screen is open;
 * clicking outside the popup while it is open only closes the popup and
 * does NOT close the inventory.
 */
public class PurseInventoryWidget extends AbstractWidget {

    private static final ResourceLocation TEXTURE =
            new ResourceLocation(NumismaticConstants.MOD_ID, "textures/gui/purse_widget.png");

    // Button size = money_bag item icon.
    public static final int WIDGET_W = 16;
    public static final int WIDGET_H = 16;
    private static final ItemStack ICON = new ItemStack(NumismaticItems.MONEY_BAG.get());

    // Popup panel (uses the left region of purse_widget.png).
    private static final int PANEL_UV_U = 0, PANEL_UV_V = 5;
    private static final int PANEL_W = 37;
    private static final int PANEL_H = 66;

    // +/- button sprites.
    private static final int BTN_W = 9, BTN_H = 5;
    private static final int BTN_PLUS_UV_U = 37, BTN_MINUS_UV_U = 46, BTN_UV_V = 24;

    // Extract button sprite.
    private static final int EXTRACT_W = 24, EXTRACT_H = 8;
    private static final int EXTRACT_UV_U = 37, EXTRACT_UV_V = 0;

    // Row layout inside the popup.
    private static final int ROW_Y0 = 6;
    private static final int ROW_HEIGHT = 12;
    private static final int ICON_X = 3;
    private static final int VALUE_X = 14;
    private static final int PLUS_X = 24;
    private static final int EXTRACT_REL_X = 6;
    private static final int EXTRACT_REL_Y = 54;

    private static final Currency[] ORDER = {
            Currency.NETHERITE, Currency.GOLD, Currency.SILVER, Currency.BRONZE
    };

    private boolean popupOpen = false;
    private final long[] pending = new long[Currency.values().length];

    public PurseInventoryWidget(int x, int y) {
        super(x, y, WIDGET_W, WIDGET_H,
                Component.translatable("gui.numismatic_reimagined.purse"));
    }

    public boolean isPopupOpen() { return popupOpen; }

    /** Geometry accessors so {@code mouseClicked} can route clicks. */
    private int popupLeft() {
        // Upstream mod uses margins(left=-30, top=15) relative to the button.
        // Our button sits at (leftPos+152, topPos+6) — offset matching the
        // button position so the popup sits just under-left of the icon
        // and doesn't overflow the screen.
        return getX() - 30;
    }
    private int popupTop()  { return getY() + 15; }
    private int rowY(int i) { return popupTop() + ROW_Y0 + i * ROW_HEIGHT; }
    private int plusX()  { return popupLeft() + PLUS_X; }
    private int minusX() { return popupLeft() + PLUS_X; }
    private int extractX() { return popupLeft() + EXTRACT_REL_X; }
    private int extractY() { return popupTop() + EXTRACT_REL_Y; }

    @Override
    protected void renderWidget(GuiGraphics g, int mouseX, int mouseY, float partial) {
        g.renderFakeItem(ICON, getX(), getY());
        if (isHoveredOrFocused()) {
            g.fill(getX(), getY(), getX() + width, getY() + height, 0x30FFFFFF);
        }
        if (popupOpen) drawPopup(g, mouseX, mouseY);
    }

    private void drawPopup(GuiGraphics g, int mouseX, int mouseY) {
        int px = popupLeft();
        int py = popupTop();

        // Panel background.
        g.blit(TEXTURE, px, py, PANEL_UV_U, PANEL_UV_V, PANEL_W, PANEL_H);

        // Per-row icon + balance.
        long[] split = splitValues(ClientCurrencyData.getBalance());
        long[] owned = { split[3], split[2], split[1], split[0] };

        for (int i = 0; i < ORDER.length; i++) {
            int y = rowY(i);
            ItemStack coinStack = new ItemStack(NumismaticItems.getCoinItem(ORDER[i]));
            g.renderFakeItem(coinStack, px + ICON_X, y);

            long p = pending[ORDER[i].ordinal()];
            String text = p > 0 ? (owned[i] - p) + "(-" + p + ")" : String.valueOf(owned[i]);
            g.drawString(net.minecraft.client.Minecraft.getInstance().font,
                    text, px + VALUE_X, y + 2, 0xFFFFFF, false);

            // + and – sprites stacked vertically in the right column.
            int bx = plusX();
            int yPlus = y;
            int yMinus = y + BTN_H + 1;
            drawSprite(g, BTN_PLUS_UV_U, BTN_UV_V, bx, yPlus, BTN_W, BTN_H, mouseX, mouseY);
            drawSprite(g, BTN_MINUS_UV_U, BTN_UV_V, bx, yMinus, BTN_W, BTN_H, mouseX, mouseY);
        }

        // Extract button.
        drawSprite(g, EXTRACT_UV_U, EXTRACT_UV_V, extractX(), extractY(),
                EXTRACT_W, EXTRACT_H, mouseX, mouseY);

        // Pending total below the panel.
        long total = totalPending();
        if (total > 0) {
            String label = "+" + String.format("%,d", total);
            int lw = net.minecraft.client.Minecraft.getInstance().font.width(label);
            g.drawString(net.minecraft.client.Minecraft.getInstance().font,
                    label, px + (PANEL_W - lw) / 2, py + PANEL_H + 2,
                    0xFFD700, true);
        }
    }

    private void drawSprite(GuiGraphics g, int uvU, int uvV, int x, int y, int w, int h,
                            int mouseX, int mouseY) {
        g.blit(TEXTURE, x, y, uvU, uvV, w, h);
        if (mouseX >= x && mouseX < x + w && mouseY >= y && mouseY < y + h) {
            g.fill(x, y, x + w, y + h, 0x30FFFFFF);
        }
    }

    @Override
    public void onClick(double mouseX, double mouseY) {
        // Left click on the money-bag icon toggles the popup.
        popupOpen = !popupOpen;
        if (!popupOpen) clearPending();
    }

    /** Called by {@code FabricPurseInventoryHook} on every mouse click on
     *  the InventoryScreen so the popup can intercept its own buttons. */
    public boolean onInventoryClick(double mouseX, double mouseY, int button) {
        if (!popupOpen || button != 0) return false;

        // Inside the popup background ? Route to the adjust / extract buttons.
        int px = popupLeft(), py = popupTop();
        if (mouseX < px || mouseX > px + PANEL_W || mouseY < py || mouseY > py + PANEL_H + 14) {
            // Click outside → close popup, don't swallow the event.
            popupOpen = false;
            clearPending();
            return false;
        }

        // Adjust buttons per row.
        for (int i = 0; i < ORDER.length; i++) {
            int y = rowY(i);
            int bx = plusX();
            if (mouseX >= bx && mouseX < bx + BTN_W) {
                if (mouseY >= y && mouseY < y + BTN_H) {
                    pend(ORDER[i], +1);
                    return true;
                }
                if (mouseY >= y + BTN_H + 1 && mouseY < y + BTN_H + 1 + BTN_H) {
                    pend(ORDER[i], -1);
                    return true;
                }
            }
        }

        // Extract button.
        int ex = extractX(), ey = extractY();
        if (mouseX >= ex && mouseX < ex + EXTRACT_W
                && mouseY >= ey && mouseY < ey + EXTRACT_H) {
            onExtract();
            return true;
        }

        // Click inside panel but not on a button: swallow so it doesn't
        // bleed into the inventory slots below.
        return true;
    }

    private void pend(Currency c, int delta) {
        boolean shift = Screen.hasShiftDown();
        long balance = ClientCurrencyData.getBalance();
        if (delta > 0) PurseExtractLogic.increment(pending, c, balance, shift);
        else PurseExtractLogic.decrement(pending, c, shift);
    }

    private void onExtract() {
        long amount = totalPending();
        if (amount <= 0) return;
        NumismaticNetworking.sendWithdraw(amount);
        clearPending();
    }

    private long totalPending() { return PurseExtractLogic.totalPending(pending); }

    private void clearPending() {
        for (int i = 0; i < pending.length; i++) pending[i] = 0;
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

    @Override
    protected void updateWidgetNarration(NarrationElementOutput out) {
        this.defaultButtonNarrationText(out);
    }
}
