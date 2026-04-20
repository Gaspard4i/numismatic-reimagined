package dev.gaspard4i.numismatic.client;

import dev.architectury.networking.NetworkManager;
import dev.gaspard4i.numismatic.NumismaticConstants;
import dev.gaspard4i.numismatic.currency.Currency;
import dev.gaspard4i.numismatic.item.NumismaticItems;
import dev.gaspard4i.numismatic.network.NumismaticNetworking;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

public class PurseInventoryWidget extends AbstractWidget {

    private static final ResourceLocation PANEL_BG =
            ResourceLocation.fromNamespaceAndPath(NumismaticConstants.MOD_ID, "textures/gui/purse_panel.png");

    public static final int WIDGET_W = 16;
    public static final int WIDGET_H = 16;
    private static final int PANEL_W = 80;
    private static final int PANEL_H = 88;
    private static final int ROW_H = 14;
    private static final int BTN_W = 10;
    private static final int BTN_H = 6;

    private static final Currency[] ORDER = {
            Currency.NETHERITE, Currency.GOLD, Currency.SILVER, Currency.BRONZE
    };

    private final ItemStack iconStack = new ItemStack(NumismaticItems.MONEY_BAG.get());

    private boolean popupOpen = false;
    private final long[] pending = new long[Currency.values().length];

    public PurseInventoryWidget(int x, int y) {
        super(x, y, WIDGET_W, WIDGET_H,
                Component.translatable("gui.numismatic_reimagined.purse"));
    }

    public boolean isPopupOpen() { return popupOpen; }

    private int popupLeft() { return getX() - PANEL_W + WIDGET_W; }
    private int popupTop()  { return getY() + WIDGET_H + 2; }
    private int rowY(int i) { return popupTop() + 4 + i * ROW_H; }
    private int plusX()    { return popupLeft() + PANEL_W - 24; }
    private int minusX()   { return popupLeft() + PANEL_W - 12; }
    private int extractX() { return popupLeft() + 4; }
    private int extractY() { return popupTop() + PANEL_H - 14; }

    @Override
    protected void renderWidget(GuiGraphics g, int mouseX, int mouseY, float partial) {
        g.renderFakeItem(iconStack, getX(), getY());
        if (isHoveredOrFocused()) {
            g.fill(getX(), getY(), getX() + width, getY() + height, 0x30FFFFFF);
        }
        if (popupOpen) drawPopup(g, mouseX, mouseY);
    }

    private void drawPopup(GuiGraphics g, int mouseX, int mouseY) {
        int px = popupLeft();
        int py = popupTop();
        // Background : solid dark panel with border (no texture dependency).
        g.fill(px - 1, py - 1, px + PANEL_W + 1, py + PANEL_H + 1, 0xFF000000);
        g.fill(px, py, px + PANEL_W, py + PANEL_H, 0xF0222222);
        // Silence unused warning while keeping the identifier for future texture swap.
        var _unused = PANEL_BG;

        long[] owned = splitOwned(ClientCurrencyData.getBalance());

        for (int i = 0; i < ORDER.length; i++) {
            int y = rowY(i);
            Currency c = ORDER[i];
            int idx = c.ordinal();
            ItemStack coinStack = new ItemStack(NumismaticItems.getCoinItem(c));
            g.renderFakeItem(coinStack, px + 4, y - 4);

            long ownedForRow = owned[idx];
            long pendingForRow = pending[idx];
            String text = pendingForRow > 0
                    ? (ownedForRow - pendingForRow) + "(-" + pendingForRow + ")"
                    : String.valueOf(ownedForRow);
            g.drawString(Minecraft.getInstance().font, text, px + 22, y, 0xFFFFFF, false);

            drawButton(g, plusX(), y, "+", mouseX, mouseY);
            drawButton(g, minusX(), y, "-", mouseX, mouseY);
        }

        // Extract button.
        int ex = extractX(), ey = extractY();
        int ew = PANEL_W - 8, eh = 10;
        boolean hover = mouseX >= ex && mouseX < ex + ew && mouseY >= ey && mouseY < ey + eh;
        g.fill(ex, ey, ex + ew, ey + eh, hover ? 0xFF4A6E3A : 0xFF2A4E1A);
        String label = Component.translatable("gui.numismatic_reimagined.purse.extract").getString()
                + " (+" + totalPending() + ")";
        int lw = Minecraft.getInstance().font.width(label);
        g.drawString(Minecraft.getInstance().font, label,
                ex + (ew - lw) / 2, ey + 1, 0xFFFFFF, false);
    }

    private void drawButton(GuiGraphics g, int x, int y, String glyph, int mouseX, int mouseY) {
        boolean hover = mouseX >= x && mouseX < x + BTN_W && mouseY >= y && mouseY < y + BTN_H;
        g.fill(x, y, x + BTN_W, y + BTN_H, hover ? 0xFF666666 : 0xFF444444);
        int gw = Minecraft.getInstance().font.width(glyph);
        g.drawString(Minecraft.getInstance().font, glyph,
                x + (BTN_W - gw) / 2, y - 1, 0xFFFFFF, false);
    }

    @Override
    public void onClick(double mouseX, double mouseY) {
        popupOpen = !popupOpen;
        if (!popupOpen) PurseExtractLogic.clear(pending);
    }

    public boolean onInventoryClick(double mouseX, double mouseY, int button) {
        if (!popupOpen || button != 0) return false;
        int px = popupLeft(), py = popupTop();
        if (mouseX < px || mouseX >= px + PANEL_W || mouseY < py || mouseY >= py + PANEL_H) {
            popupOpen = false;
            PurseExtractLogic.clear(pending);
            return false;
        }

        boolean shift = Screen.hasShiftDown();
        long balance = ClientCurrencyData.getBalance();

        for (int i = 0; i < ORDER.length; i++) {
            int y = rowY(i);
            if (mouseY >= y && mouseY < y + BTN_H) {
                if (mouseX >= plusX() && mouseX < plusX() + BTN_W) {
                    PurseExtractLogic.increment(pending, ORDER[i], balance, shift);
                    return true;
                }
                if (mouseX >= minusX() && mouseX < minusX() + BTN_W) {
                    PurseExtractLogic.decrement(pending, ORDER[i], shift);
                    return true;
                }
            }
        }

        int ex = extractX(), ey = extractY();
        int ew = PANEL_W - 8, eh = 10;
        if (mouseX >= ex && mouseX < ex + ew && mouseY >= ey && mouseY < ey + eh) {
            onExtract();
            return true;
        }
        return true;
    }

    private void onExtract() {
        long amount = totalPending();
        if (amount <= 0) return;
        NetworkManager.sendToServer(new NumismaticNetworking.WithdrawPayload(amount));
        PurseExtractLogic.clear(pending);
    }

    private long totalPending() { return PurseExtractLogic.totalPending(pending); }

    private static long[] splitOwned(long v) {
        long[] split = new long[Currency.values().length];
        long remaining = v;
        for (int i = Currency.values().length - 1; i >= 0; i--) {
            Currency c = Currency.values()[i];
            split[i] = remaining / c.getValue();
            remaining %= c.getValue();
        }
        return split;
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput out) {
        this.defaultButtonNarrationText(out);
    }
}
