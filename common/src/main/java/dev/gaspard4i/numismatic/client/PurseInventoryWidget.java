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
 * Layout matches upstream wisp-forest/numismatic-overhaul owo_ui/purse.xml :
 *  - button 11x13 at UV (62, 0)
 *  - popup panel 37x59 at UV (0, 0), positioned at button + (-30, 15)
 *  - 3 count labels at (5, 12) vertical gap 3
 *  - +/- buttons 9x5 at UV (37, 24) / (46, 24), positioned at (18, 10) gap 1
 *  - extract button 24x8 at UV (37, 0), positioned at (3, 46)
 *
 * <p>Reimagined adaptation : we keep 4 denominations instead of upstream's 3.
 * Netherite row is appended at the top (extra 9 px between label block and extract row).
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
    // Upstream panel is 59 px for 3 rows. We extend to 68 px to accommodate a 4th netherite row (9 px extra).
    private static final int PANEL_H = 68;
    private static final int PANEL_UV_U = 0;
    private static final int PANEL_UV_V = 0;

    // Label (count text) layout.
    private static final int LABELS_ORIGIN_X = 5;
    private static final int LABELS_ORIGIN_Y = 12;
    private static final int LABEL_ROW_STRIDE = 12; // text line + gap

    // Adjust-button layout : + then - stacked per row, repeating per denom.
    private static final int BTN_W = 9;
    private static final int BTN_H = 5;
    private static final int BTNS_ORIGIN_X = 18;
    private static final int BTNS_ORIGIN_Y = 10;
    private static final int BTN_GAP = 1;
    private static final int BTN_PLUS_UV_U = 37;
    private static final int BTN_MINUS_UV_U = 46;
    private static final int BTN_UV_V = 24;

    // Extract button : placed below the denom rows. Upstream y=46 for 3 rows ; we bump to 55 for 4.
    private static final int EXTRACT_W = 24;
    private static final int EXTRACT_H = 8;
    private static final int EXTRACT_X = 3;
    private static final int EXTRACT_Y = 55;
    private static final int EXTRACT_UV_U = 37;
    private static final int EXTRACT_UV_V = 0;

    // Popup margins relative to the button (upstream : left=-30, top=15).
    private static final int POPUP_MARGIN_X = -30;
    private static final int POPUP_MARGIN_Y = 15;

    // MSD-first display order : netherite at top, bronze at bottom.
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

    private int labelY(int i) { return popupTop() + LABELS_ORIGIN_Y + i * LABEL_ROW_STRIDE; }
    private int plusY(int i)  { return popupTop() + BTNS_ORIGIN_Y + i * LABEL_ROW_STRIDE; }
    private int minusY(int i) { return plusY(i) + BTN_H + BTN_GAP; }
    private int adjustX()     { return popupLeft() + BTNS_ORIGIN_X; }
    private int extractX()    { return popupLeft() + EXTRACT_X; }
    private int extractY()    { return popupTop() + EXTRACT_Y; }

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

        // Panel texture — upstream uses region 37x59 at (0, 0). We reuse it and stretch the bottom
        // slightly for a 4th row by redrawing the bottom strip.
        g.blit(TEXTURE, px, py, PANEL_UV_U, PANEL_UV_V, PANEL_W, Math.min(PANEL_H, 59), TEX_W, TEX_H);
        if (PANEL_H > 59) {
            // Repeat the last 9-px strip of the panel source region to fill the extra row.
            g.blit(TEXTURE, px, py + 59, PANEL_UV_U, PANEL_UV_V + 50, PANEL_W, PANEL_H - 59, TEX_W, TEX_H);
        }

        long[] owned = splitOwnedMsdFirst(ClientCurrencyData.getBalance());

        // Rows (MSD-first).
        for (int i = 0; i < ORDER.length; i++) {
            Currency c = ORDER[i];
            int idx = c.ordinal();

            // Count text : owned minus pending, colored with the currency's name color.
            long ownedRow = owned[i];
            long pendingRow = pending[idx];
            long display = Math.max(0, ownedRow - pendingRow);
            String text = String.valueOf(Math.min(display, 99));
            g.drawString(Minecraft.getInstance().font, text,
                    px + LABELS_ORIGIN_X, labelY(i), c.getNameColor(), false);

            // +/- buttons.
            int bx = adjustX();
            int yPlus = plusY(i);
            int yMinus = minusY(i);
            drawTextureButton(g, BTN_PLUS_UV_U, BTN_UV_V, bx, yPlus, mouseX, mouseY);
            drawTextureButton(g, BTN_MINUS_UV_U, BTN_UV_V, bx, yMinus, mouseX, mouseY);
        }

        // Extract button.
        drawTextureButton(g, EXTRACT_UV_U, EXTRACT_UV_V, extractX(), extractY(), mouseX, mouseY,
                EXTRACT_W, EXTRACT_H);

        // Pending total, drawn just below the panel.
        long total = totalPending();
        if (total > 0) {
            String label = "+" + total;
            int lw = Minecraft.getInstance().font.width(label);
            g.drawString(Minecraft.getInstance().font, label,
                    px + (PANEL_W - lw) / 2, py + PANEL_H + 2,
                    Currency.GOLD.getNameColor(), true);
        }
    }

    private void drawTextureButton(GuiGraphics g, int uvU, int uvV, int x, int y, int mouseX, int mouseY) {
        drawTextureButton(g, uvU, uvV, x, y, mouseX, mouseY, BTN_W, BTN_H);
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
        int bx = adjustX();

        for (int i = 0; i < ORDER.length; i++) {
            int yPlus = plusY(i);
            int yMinus = minusY(i);
            if (mouseX >= bx && mouseX < bx + BTN_W) {
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
