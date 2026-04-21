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
 * Purse button + popup rendered as an overlay over the inventory screen.
 *
 * <p>Layout ported from upstream wisp-forest/numismatic-overhaul owo_ui/purse.xml :
 * <ul>
 *   <li>Button 11x13 at UV (62, 0) in purse_widget.png (128x64)</li>
 *   <li>Popup panel 37x59 at UV (0, 0), positioned at button + (-30, +15)</li>
 *   <li>3 count labels at (5, 12) with vertical gap of 3 → effective stride ≈ 12 px</li>
 *   <li>6 +/- buttons stacked vertically at (18, 10), gap 1 → (plus/minus) pair per denom, stride 12 px</li>
 *   <li>Extract button 24x8 at UV (37, 0) positioned at (3, 46)</li>
 * </ul>
 *
 * <p>Reimagined adaptation : we keep the same per-row stride (12 px) but add a 4th netherite
 * row on top, extending the panel height from 59 to 71 px. The last 12 px are drawn by
 * repeating the bottom strip of the source region so the extract button stays on a clean edge.
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
    private static final int PANEL_BASE_H = 59;
    private static final int ROW_STRIDE = 12;
    private static final int EXTRA_ROW_H = ROW_STRIDE; // one extra row for netherite
    private static final int PANEL_H = PANEL_BASE_H + EXTRA_ROW_H;

    // Upstream origins (inside the panel).
    private static final int LABEL_ORIGIN_X = 5;
    private static final int LABEL_ORIGIN_Y = 12;
    private static final int BTN_ORIGIN_X = 18;
    private static final int BTN_ORIGIN_Y = 10;
    private static final int BTN_W = 9;
    private static final int BTN_H = 5;
    private static final int BTN_GAP = 1;

    // Extract button : upstream y=46 for 3 rows ; shift by EXTRA_ROW_H for our 4th row.
    private static final int EXTRACT_X = 3;
    private static final int EXTRACT_Y = 46 + EXTRA_ROW_H;
    private static final int EXTRACT_W = 24;
    private static final int EXTRACT_H = 8;

    // UV coords inside purse_widget.png.
    private static final int BTN_PLUS_UV_U = 37;
    private static final int BTN_MINUS_UV_U = 46;
    private static final int BTN_UV_V = 24;
    private static final int EXTRACT_UV_U = 37;
    private static final int EXTRACT_UV_V = 0;

    // Popup margins relative to the button (upstream : left=-30, top=15).
    private static final int POPUP_MARGIN_X = -30;
    private static final int POPUP_MARGIN_Y = 15;

    // MSD-first display order. Row index = position in this array.
    private static final Currency[] ORDER = {
            Currency.NETHERITE, Currency.GOLD, Currency.SILVER, Currency.BRONZE
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

    /** Y position of the count label for row {@code i} (MSD-first). */
    private int labelY(int i) { return popupTop() + LABEL_ORIGIN_Y + i * ROW_STRIDE; }
    /** Y position of the + button for row {@code i}. */
    private int plusY(int i)  { return popupTop() + BTN_ORIGIN_Y + i * (BTN_H + BTN_GAP + BTN_H + BTN_GAP); }
    /** Y position of the - button for row {@code i} (just below +). */
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

        // Top part : upstream's base panel (37x59) starting at (0, 0).
        // Covers the labels rows 0..2 and the top of the extract button area.
        int baseTopH = PANEL_BASE_H - 13; // 46 px : everything above the extract band
        g.blit(TEXTURE, px, py, 0, 0, PANEL_W, baseTopH, TEX_W, TEX_H);

        // Extra row : repeat the strip covering the 3rd upstream label row (y 24..36 in source)
        // to create our 4th row without stretching.
        int stripSrcY = 24;
        int stripDstY = py + baseTopH;
        int stripH = ROW_STRIDE;
        g.blit(TEXTURE, px, stripDstY, 0, stripSrcY, PANEL_W, stripH, TEX_W, TEX_H);

        // Bottom part : the extract button zone (last 13 px of upstream panel source).
        g.blit(TEXTURE, px, stripDstY + stripH, 0, PANEL_BASE_H - 13, PANEL_W, 13, TEX_W, TEX_H);

        long[] owned = splitOwnedMsdFirst(ClientCurrencyData.getBalance());

        // Rows (MSD-first).
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

    /**
     * Intercepts clicks that fall inside the popup bounds (the widget itself is only 11x13,
     * so vanilla {@code mouseClicked} dispatch wouldn't reach the popup without this override).
     */
    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (popupOpen && button == 0 && isInsidePopup(mouseX, mouseY)) {
            return handlePopupClick(mouseX, mouseY);
        }
        boolean handled = super.mouseClicked(mouseX, mouseY, button);
        if (!handled && popupOpen && button == 0 && !isInsideButton(mouseX, mouseY)) {
            // Click outside the button and outside the popup → close popup.
            popupOpen = false;
            PurseExtractLogic.clear(pending);
        }
        return handled;
    }

    /** Returns true if the widget popup consumed the click (needed so the inventory doesn't react to it). */
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

    /** Returns an MSD-first split matching {@link #ORDER}: [NETHERITE, GOLD, SILVER, BRONZE]. */
    private static long[] splitOwnedMsdFirst(long v) {
        long[] split = new long[ORDER.length];
        long remaining = v;
        for (int i = 0; i < ORDER.length; i++) {
            long unit = ORDER[i].getValue();
            split[i] = remaining / unit;
            remaining %= unit;
        }
        return split;
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput out) {
        this.defaultButtonNarrationText(out);
    }
}
