package dev.gaspard4i.numismatic.client;

import dev.architectury.networking.NetworkManager;
import dev.gaspard4i.numismatic.NumismaticConstants;
import dev.gaspard4i.numismatic.currency.Currency;
import dev.gaspard4i.numismatic.network.NumismaticNetworking;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

/**
 * Purse button + extract popup rendered as an overlay. Layout (texture UV, positions) is
 * the faithful port of upstream wisp-forest/numismatic-overhaul {@code owo_ui/purse.xml} :
 * <ul>
 *   <li>Button 11x13 at UV (62, 0) in {@code purse_widget.png} (128x64)</li>
 *   <li>Popup panel 37x59 at UV (0, 0), positioned at button + (-30, +15)</li>
 *   <li>3 count labels at (5, 12) with vertical stride 12 (gold / silver / bronze)</li>
 *   <li>6 +/- buttons at (18, 10), 9x5, gap 1 (2 per denom)</li>
 *   <li>Extract button 24x8 at UV (37, 0) at (3, 46)</li>
 * </ul>
 */
public class PurseInventoryWidget extends AbstractWidget {

    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(NumismaticConstants.MOD_ID, "textures/gui/purse_widget.png");
    private static final int TEX_W = 128;
    private static final int TEX_H = 64;

    public static final int BUTTON_W = 11;
    public static final int BUTTON_H = 13;
    private static final int BUTTON_UV_U = 62;
    private static final int BUTTON_UV_V = 0;

    private static final int PANEL_W = 37;
    private static final int PANEL_H = 59;

    private static final int LABEL_ORIGIN_X = 5;
    private static final int LABEL_ORIGIN_Y = 12;
    private static final int ROW_STRIDE = 12;

    private static final int BTN_ORIGIN_X = 18;
    private static final int BTN_ORIGIN_Y = 10;
    private static final int BTN_W = 9;
    private static final int BTN_H = 5;
    private static final int BTN_GAP = 1;

    private static final int EXTRACT_X = 3;
    private static final int EXTRACT_Y = 46;
    private static final int EXTRACT_W = 24;
    private static final int EXTRACT_H = 8;

    private static final int BTN_PLUS_UV_U = 37;
    private static final int BTN_MINUS_UV_U = 46;
    private static final int BTN_UV_V = 24;
    private static final int EXTRACT_UV_U = 37;
    private static final int EXTRACT_UV_V = 0;

    private static final int POPUP_MARGIN_X = -30;
    private static final int POPUP_MARGIN_Y = 15;

    private static final Currency[] ORDER = {
            Currency.GOLD, Currency.SILVER, Currency.BRONZE
    };

    private boolean popupOpen = false;
    private final long[] pending = new long[Currency.values().length];

    public PurseInventoryWidget(int x, int y) {
        super(x, y, BUTTON_W, BUTTON_H,
                Component.translatable("gui.numismatic_reimagined.purse"));
    }

    public boolean isPopupOpen() { return popupOpen; }

    private int popupLeft() { return getX() + POPUP_MARGIN_X; }
    private int popupTop()  { return getY() + POPUP_MARGIN_Y; }

    private int labelY(int i) { return popupTop() + LABEL_ORIGIN_Y + i * ROW_STRIDE; }
    private int plusY(int i)  { return popupTop() + BTN_ORIGIN_Y + i * 2 * (BTN_H + BTN_GAP); }
    private int minusY(int i) { return plusY(i) + BTN_H + BTN_GAP; }

    private int adjustX()  { return popupLeft() + BTN_ORIGIN_X; }
    private int extractX() { return popupLeft() + EXTRACT_X; }
    private int extractY() { return popupTop() + EXTRACT_Y; }

    @Override
    protected void renderWidget(GuiGraphics g, int mouseX, int mouseY, float partial) {
        g.blit(TEXTURE, getX(), getY(), BUTTON_UV_U, BUTTON_UV_V, BUTTON_W, BUTTON_H, TEX_W, TEX_H);
        if (isHoveredOrFocused()) {
            g.fill(getX(), getY(), getX() + width, getY() + height, 0x30FFFFFF);
        }
        if (popupOpen) drawPopup(g, mouseX, mouseY);
    }

    private void drawPopup(GuiGraphics g, int mouseX, int mouseY) {
        int px = popupLeft();
        int py = popupTop();
        g.blit(TEXTURE, px, py, 0, 0, PANEL_W, PANEL_H, TEX_W, TEX_H);

        long[] owned = splitOwnedMsdFirst(ClientCurrencyData.getBalance());

        for (int i = 0; i < ORDER.length; i++) {
            Currency c = ORDER[i];
            int idx = c.ordinal();

            long ownedRow = owned[i];
            long pendingRow = pending[idx];
            long display = Math.max(0, ownedRow - pendingRow);
            String text = String.valueOf(Math.min(display, 99));
            g.drawString(Minecraft.getInstance().font, text,
                    px + LABEL_ORIGIN_X, labelY(i), c.getNameColor(), false);

            int bx = adjustX();
            drawTextureButton(g, BTN_PLUS_UV_U, BTN_UV_V, bx, plusY(i), mouseX, mouseY, BTN_W, BTN_H);
            drawTextureButton(g, BTN_MINUS_UV_U, BTN_UV_V, bx, minusY(i), mouseX, mouseY, BTN_W, BTN_H);
        }

        drawTextureButton(g, EXTRACT_UV_U, EXTRACT_UV_V, extractX(), extractY(),
                mouseX, mouseY, EXTRACT_W, EXTRACT_H);

        long total = totalPending();
        if (total > 0) {
            String label = "+" + total;
            int lw = Minecraft.getInstance().font.width(label);
            g.drawString(Minecraft.getInstance().font, label,
                    px + (PANEL_W - lw) / 2, py + PANEL_H + 2,
                    Currency.GOLD.getNameColor(), true);
        }
    }

    private void drawTextureButton(GuiGraphics g, int uvU, int uvV, int x, int y,
                                   int mouseX, int mouseY, int w, int h) {
        g.blit(TEXTURE, x, y, uvU, uvV, w, h, TEX_W, TEX_H);
        if (mouseX >= x && mouseX < x + w && mouseY >= y && mouseY < y + h) {
            g.fill(x, y, x + w, y + h, 0x30FFFFFF);
        }
    }

    @Override
    public void onClick(double mouseX, double mouseY) {
        popupOpen = !popupOpen;
        if (!popupOpen) PurseExtractLogic.clear(pending);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (popupOpen && button == 0 && isInsidePopup(mouseX, mouseY)) {
            return handlePopupClick(mouseX, mouseY);
        }
        boolean handled = super.mouseClicked(mouseX, mouseY, button);
        if (!handled && popupOpen && button == 0 && !isInsideButton(mouseX, mouseY)) {
            popupOpen = false;
            PurseExtractLogic.clear(pending);
        }
        return handled;
    }

    public boolean consumesClick(double mouseX, double mouseY) {
        return popupOpen && isInsidePopup(mouseX, mouseY);
    }

    private boolean isInsideButton(double mouseX, double mouseY) {
        return mouseX >= getX() && mouseX < getX() + BUTTON_W
                && mouseY >= getY() && mouseY < getY() + BUTTON_H;
    }

    private boolean isInsidePopup(double mouseX, double mouseY) {
        int px = popupLeft(), py = popupTop();
        return mouseX >= px && mouseX < px + PANEL_W
                && mouseY >= py && mouseY < py + PANEL_H;
    }

    private boolean handlePopupClick(double mouseX, double mouseY) {
        boolean shift = Screen.hasShiftDown();
        long balance = ClientCurrencyData.getBalance();
        int bx = adjustX();

        for (int i = 0; i < ORDER.length; i++) {
            if (mouseX >= bx && mouseX < bx + BTN_W) {
                int yPlus = plusY(i);
                int yMinus = minusY(i);
                if (mouseY >= yPlus && mouseY < yPlus + BTN_H) {
                    PurseExtractLogic.increment(pending, ORDER[i], balance, shift);
                    return true;
                }
                if (mouseY >= yMinus && mouseY < yMinus + BTN_H) {
                    PurseExtractLogic.decrement(pending, ORDER[i], shift);
                    return true;
                }
            }
        }

        int ex = extractX(), ey = extractY();
        if (mouseX >= ex && mouseX < ex + EXTRACT_W && mouseY >= ey && mouseY < ey + EXTRACT_H) {
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

    private static long[] splitOwnedMsdFirst(long v) {
        long[] split = new long[ORDER.length];
        long remaining = v;
        split[0] = remaining / Currency.GOLD.getValue();
        remaining %= Currency.GOLD.getValue();
        split[1] = remaining / Currency.SILVER.getValue();
        remaining %= Currency.SILVER.getValue();
        split[2] = remaining;
        return split;
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput out) {
        this.defaultButtonNarrationText(out);
    }
}
