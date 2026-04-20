package dev.gaspard4i.numismatic.client.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import dev.gaspard4i.numismatic.NumismaticConstants;
import dev.gaspard4i.numismatic.client.ClientShopState;
import dev.gaspard4i.numismatic.currency.Currency;
import dev.gaspard4i.numismatic.currency.CurrencyResolver;
import dev.gaspard4i.numismatic.network.NumismaticNetworking;
import dev.gaspard4i.numismatic.shop.OfferList;
import dev.gaspard4i.numismatic.shop.ShopMenu;
import dev.gaspard4i.numismatic.shop.ShopOffer;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

/**
 * Shop UI pixel-perfect port of wisp-forest/numismatic-overhaul (MIT).
 *
 * <p>Layout constants come from the original shop.xml (owo-ui). UV
 * coordinates target the {@code shop_gui.png} (tab=0) and
 * {@code shop_gui_trades.png} (tab=1) textures.
 *
 * <p>Tab 0 = stock (3x9 chest grid). Tab 1 = offers (scrollable grid +
 * edit panel). The owner sees both tabs; non-owner is redirected to the
 * vanilla merchant screen by {@code ShopBlock} and never reaches here.
 */
public class ShopScreen extends AbstractContainerScreen<ShopMenu> {

    public static final ResourceLocation TEXTURE_PNG =
            new ResourceLocation(NumismaticConstants.MOD_ID, "textures/gui/shop_gui.png");
    public static final ResourceLocation TRADES_TEXTURE =
            new ResourceLocation(NumismaticConstants.MOD_ID, "textures/gui/shop_gui_trades.png");

    // Size of the background panel (from shop.xml: 176x168).
    private static final int BG_W = 176;
    private static final int BG_H = 168;

    // Tab button (UV and dims from shop.xml).
    private static final int TAB_UV_U = 113, TAB_UV_V = 168, TAB_W = 32, TAB_H = 28;
    private static final int TAB_X_OFFSET = -29;  // relative to leftPos (left-column width 120 - 29 = sits outside bg)
    private static final int TAB_Y0 = 5;
    private static final int TAB_Y1 = 37; // TAB_Y0 + 28 + 4 margin-bottom

    // Currency widget (right column). 4-denomination layout custom-built by
    // the user — netherite/gold/silver/bronze top→bottom, widget 34x66.
    // Source: shop_gui.png x=146..179, y=169..234.
    private static final int CUR_UV_U = 146, CUR_UV_V = 169, CUR_W = 34, CUR_H = 66;
    private static final int CUR_X_OFFSET = BG_W + 2; // right-column padding-left 2

    // Extract button sits below the widget frame (the grey zone at y=222..229
    // inside the widget is drawn-over by this button at runtime).
    // Button UV starts at (146, 237) with 26x8 size + hover variant at v+8.
    private static final int EXTRACT_UV_U = 146, EXTRACT_UV_V = 237, EXTRACT_W = 26, EXTRACT_H = 8;
    // Position relative to the widget top-left: same as original (4, 53) — 12
    // pixels lower than the original 54-tall widget to account for netherite row.
    private static final int EXTRACT_REL_X = 4, EXTRACT_REL_Y = 53;

    // Trade-edit widget (owner, tab=1). User extended to 100px wide (added a column).
    private static final int EDIT_UV_U = 15, EDIT_UV_V = 169, EDIT_W = 100, EDIT_H = 54;
    private static final int SUBMIT_UV_U = 15, SUBMIT_UV_V = 223, SUBMIT_W = 41, SUBMIT_H = 10;
    private static final int DELETE_UV_U = 56, DELETE_UV_V = 223;

    // Offer-container region (tab=1). Absolute (8, 10), 160x60.
    private static final int OFFERS_X = 8, OFFERS_Y = 10;
    private static final int OFFERS_W = 160, OFFERS_H = 60;
    private static final int TRADE_BUTTON_W = 78, TRADE_BUTTON_H = 20;
    private static final int ARROW_UV_U = 1, ARROW_UV_V = 172, ARROW_W = 5, ARROW_H = 7;

    private int tab = 0;
    private int scroll = 0;

    @Nullable private Button tabChest;
    @Nullable private Button tabEmerald;
    @Nullable private Button extractBtn;
    @Nullable private Button submitBtn;
    @Nullable private Button deleteBtn;
    @Nullable private EditBox priceField;

    /** When the owner clicks a trade-button, its template becomes the edit
     * buffer. */
    private ItemStack editBuffer = ItemStack.EMPTY;

    /** Quantity to sell per purchase. Adjusted via scroll-on-fake-slot,
     *  capped at editBuffer.getMaxStackSize() * 27 (full stock fills). */
    private int editQty = 1;

    /** Index in ClientShopState offers of the offer currently being edited.
     *  -1 means we are creating a new offer. */
    private int editingIndex = -1;

    public ShopScreen(ShopMenu menu, Inventory inv, Component title) {
        super(menu, inv, title);
        this.imageWidth = BG_W;
        this.imageHeight = BG_H;
        this.titleLabelY = 5;
        this.inventoryLabelY = 75;
    }

    public int currentTab() { return tab; }

    @Override
    protected void init() {
        super.init();
        rebuildUI();
    }

    private void rebuildUI() {
        clearWidgets();

        boolean owner = ClientShopState.canEdit();

        // --- Tab buttons (owner only — buyer is on merchant screen anyway)
        if (owner) {
            tabChest = addRenderableWidget(new TabButton(
                    leftPos + TAB_X_OFFSET, topPos + TAB_Y0,
                    0, b -> selectTab(0)));
            tabEmerald = addRenderableWidget(new TabButton(
                    leftPos + TAB_X_OFFSET, topPos + TAB_Y1,
                    1, b -> selectTab(1)));
            tabChest.active = tab != 0;
            tabEmerald.active = tab != 1;
        }

        int px = leftPos + CUR_X_OFFSET;

        if (tab == 0) {
            // Tab 0 = stock : currency widget + withdraw button at the top of the right column.
            if (owner && !ClientShopState.isAdmin()) {
                extractBtn = addRenderableWidget(new TexturedButton(
                        px + EXTRACT_REL_X, topPos + EXTRACT_REL_Y,
                        EXTRACT_W, EXTRACT_H, EXTRACT_UV_U, EXTRACT_UV_V,
                        TEXTURE_PNG,
                        b -> {
                            BlockPos pos = ClientShopState.getPos();
                            if (pos != null) NumismaticNetworking.sendWithdrawRevenue(pos);
                        }));
                extractBtn.active = ClientShopState.getRevenue() > 0;
            }
        } else if (tab == 1 && owner) {
            // Tab 1 = offers : trade-edit at the top (matching shop.xml layout),
            // currency widget below.
            int ey = topPos;

            // Single price field at (35, 18) rel to edit panel, 47x11 — matches
            // shop.xml exactly. The 4 bronze/silver/gold/netherite labels sit
            // above it at (36, 5) via renderEditDenominationLabels.
            priceField = new EditBox(font, px + 35, ey + 18, 47, 11, Component.empty());
            priceField.setMaxLength(7);
            priceField.setBordered(false);
            priceField.setTextColor(0xFFFFFF);
            priceField.setFilter(s -> s.matches("\\d*"));
            priceField.setResponder(s -> refreshEditState());
            addRenderableWidget(priceField);

            // Save/delete buttons at (7, 36) and (50, 36), 41x10.
            submitBtn = addRenderableWidget(new TexturedButton(
                    px + 7, ey + 36, SUBMIT_W, SUBMIT_H, SUBMIT_UV_U, SUBMIT_UV_V,
                    TEXTURE_PNG, b -> onSubmit()));
            deleteBtn = addRenderableWidget(new TexturedButton(
                    px + 50, ey + 36, SUBMIT_W, SUBMIT_H, DELETE_UV_U, SUBMIT_UV_V,
                    TEXTURE_PNG, b -> onDelete()));

            // Currency widget in the bottom half of the right panel.
            if (!ClientShopState.isAdmin()) {
                extractBtn = addRenderableWidget(new TexturedButton(
                        px + EXTRACT_REL_X, topPos + EDIT_H + 3 + EXTRACT_REL_Y,
                        EXTRACT_W, EXTRACT_H, EXTRACT_UV_U, EXTRACT_UV_V,
                        TEXTURE_PNG,
                        b -> {
                            BlockPos pos = ClientShopState.getPos();
                            if (pos != null) NumismaticNetworking.sendWithdrawRevenue(pos);
                        }));
                extractBtn.active = ClientShopState.getRevenue() > 0;
            }

            refreshEditState();
        }
    }

    private void selectTab(int newTab) {
        if (this.tab == newTab) return;
        this.tab = newTab;
        this.scroll = 0;
        if (newTab == 0) {
            editBuffer = ItemStack.EMPTY;
            editingIndex = -1;
            editQty = 1;
        }
        rebuildUI();
    }

    private void refreshEditState() {
        boolean canSubmit = !editBuffer.isEmpty()
                && priceField != null
                && !priceField.getValue().isEmpty()
                && parsePrice() > 0
                && editQty > 0;
        if (submitBtn != null) submitBtn.active = canSubmit;
        if (deleteBtn != null) deleteBtn.active = editingIndex >= 0;
        if (extractBtn != null) extractBtn.active = ClientShopState.getRevenue() > 0;
    }

    private long parsePrice() {
        if (priceField == null) return 0;
        try { return Long.parseLong(priceField.getValue()); }
        catch (NumberFormatException e) { return 0; }
    }

    private int maxQty() {
        if (editBuffer.isEmpty()) return 1;
        return editBuffer.getMaxStackSize();
    }

    private void bumpQty(int delta) {
        int newQty = Math.max(1, Math.min(maxQty(), editQty + delta));
        if (newQty != editQty) {
            editQty = newQty;
            refreshEditState();
        }
    }

    private void onSubmit() {
        BlockPos pos = ClientShopState.getPos();
        if (pos == null || editBuffer.isEmpty()) return;
        long price = parsePrice();
        if (price <= 0 || editQty <= 0) return;
        ItemStack template = editBuffer.copy();
        template.setCount(1);
        NumismaticNetworking.sendEditOffer(pos, editingIndex, template, price, editQty);
        if (editingIndex < 0) {
            editBuffer = ItemStack.EMPTY;
            editQty = 1;
            if (priceField != null) priceField.setValue("");
        }
    }

    private void onDelete() {
        BlockPos pos = ClientShopState.getPos();
        if (pos == null || editingIndex < 0) return;
        NumismaticNetworking.sendRemoveOffer(pos, editingIndex);
        editBuffer = ItemStack.EMPTY;
        editingIndex = -1;
        editQty = 1;
        if (priceField != null) priceField.setValue("");
    }

    private void loadOfferIntoEdit(int index) {
        var list = ClientShopState.getOffers().asList();
        if (index < 0 || index >= list.size()) return;
        ShopOffer offer = list.get(index);
        if (offer == null) return;
        editingIndex = index;
        editBuffer = offer.template().copy();
        editBuffer.setCount(1);
        editQty = offer.quantityPerPurchase();
        if (priceField != null) priceField.setValue(String.valueOf(offer.priceBronze()));
        refreshEditState();
    }

    @Override
    protected void renderBg(GuiGraphics g, float partial, int mouseX, int mouseY) {
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);

        ResourceLocation bg = tab == 1 ? TRADES_TEXTURE : TEXTURE_PNG;
        g.blit(bg, leftPos, topPos, 0, 0, BG_W, BG_H);

        int px = leftPos + CUR_X_OFFSET;

        if (tab == 0) {
            // Currency widget at the top of the right column.
            g.blit(TEXTURE_PNG, px, topPos, CUR_UV_U, CUR_UV_V, CUR_W, CUR_H);
        } else if (tab == 1 && ClientShopState.canEdit()) {
            // Trade-edit at the top of the right column.
            g.blit(TEXTURE_PNG, px, topPos, EDIT_UV_U, EDIT_UV_V, EDIT_W, EDIT_H);
            // Fake slot item (x=8, y=15 relative to the trade-edit panel).
            if (!editBuffer.isEmpty()) {
                ItemStack render = editBuffer.copy();
                render.setCount(Math.max(1, Math.min(64, editQty)));
                g.renderItem(render, px + 8, topPos + 15);
                g.renderItemDecorations(font, render,
                        px + 8, topPos + 15, String.valueOf(editQty));
            }
            // Currency widget below the trade-edit panel.
            int cy = topPos + EDIT_H + 3;
            g.blit(TEXTURE_PNG, px, cy, CUR_UV_U, CUR_UV_V, CUR_W, CUR_H);
        }
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partial) {
        renderBackground(g);
        super.render(g, mouseX, mouseY, partial);

        if (tab == 1) renderOffers(g, mouseX, mouseY);

        if (tab == 0 || tab == 1) renderCurrencyLabels(g);
        if (tab == 1 && ClientShopState.canEdit()) renderPriceDenominationLabels(g);

        renderTooltip(g, mouseX, mouseY);
    }

    private void renderCurrencyLabels(GuiGraphics g) {
        long stored = ClientShopState.getRevenue();
        long[] split = splitValues(stored); // {bronze, silver, gold, netherite}
        int cx = leftPos + CUR_X_OFFSET + 5;
        int cy0 = (tab == 1) ? topPos + EDIT_H + 3 : topPos;
        // Labels are 12px apart starting at y=7 relative to the widget top;
        // top to bottom: netherite, gold, silver, bronze.
        g.drawString(font, String.valueOf(split[3]), cx, cy0 + 7, 0x404040, false);
        g.drawString(font, String.valueOf(split[2]), cx, cy0 + 19, 0x404040, false);
        g.drawString(font, String.valueOf(split[1]), cx, cy0 + 31, 0x404040, false);
        g.drawString(font, String.valueOf(split[0]), cx, cy0 + 43, 0x404040, false);
        if (extractBtn != null) extractBtn.active = stored > 0;
    }

    /** Draws the live price split (bronze/silver/gold/netherite) above the
     *  fake-slot in the trade-edit panel. Positioned at (36, 5) relative to
     *  the widget, matching the original shop.xml. */
    private void renderPriceDenominationLabels(GuiGraphics g) {
        long price = parsePrice();
        long[] split = splitValues(price); // {bronze, silver, gold, netherite}
        int px = leftPos + CUR_X_OFFSET;
        int baseX = px + 30;
        int baseY = topPos + 5;
        int step = 15;
        int color = 0x898989;
        g.drawString(font, String.valueOf(split[0]), baseX, baseY, color, false);
        g.drawString(font, String.valueOf(split[1]), baseX + step, baseY, color, false);
        g.drawString(font, String.valueOf(split[2]), baseX + step * 2, baseY, color, false);
        g.drawString(font, String.valueOf(split[3]), baseX + step * 3, baseY, color, false);
    }

    /** Returns {bronze, silver, gold, netherite} (indices 0..3). */
    private static long[] splitValues(long price) {
        long netherite = price / Currency.NETHERITE.getValue();
        long rest = price % Currency.NETHERITE.getValue();
        long gold = rest / Currency.GOLD.getValue();
        rest %= Currency.GOLD.getValue();
        long silver = rest / Currency.SILVER.getValue();
        long bronze = rest % Currency.SILVER.getValue();
        return new long[]{ bronze, silver, gold, netherite };
    }

    private void renderOffers(GuiGraphics g, int mouseX, int mouseY) {
        OfferList offers = ClientShopState.getOffers();
        int baseX = leftPos + OFFERS_X;
        int baseY = topPos + OFFERS_Y;

        g.enableScissor(baseX, baseY, baseX + OFFERS_W, baseY + OFFERS_H);

        int colW = TRADE_BUTTON_W;
        int colGap = 4;
        int scrollPx = scroll;

        for (int i = 0; i < offers.size(); i++) {
            ShopOffer offer = offers.get(i);
            if (offer == null) continue;

            int col = i % 2;
            int row = i / 2;
            int x = baseX + col * (colW + colGap);
            int y = baseY + row * TRADE_BUTTON_H - scrollPx;
            if (y + TRADE_BUTTON_H < baseY || y > baseY + OFFERS_H) continue;

            renderTradeButton(g, x, y, offer, mouseX, mouseY, i);
        }

        g.disableScissor();

        // Scrollbar on the right edge of the offers region.
        int contentH = ((offers.size() + 1) / 2) * TRADE_BUTTON_H;
        if (contentH > OFFERS_H) {
            int barX = baseX + OFFERS_W - 4;
            int barY = baseY;
            int barH = OFFERS_H;
            g.fill(barX, barY, barX + 4, barY + barH, 0xFF373737);
            int thumbH = Math.max(8, barH * barH / contentH);
            int maxScroll = contentH - OFFERS_H;
            int thumbY = barY + (int) ((barH - thumbH) * (scroll / (float) maxScroll));
            g.fill(barX, thumbY, barX + 4, thumbY + thumbH, 0xFFAAAAAA);
        }
    }

    private void renderTradeButton(GuiGraphics g, int x, int y, ShopOffer offer,
                                   int mouseX, int mouseY, int index) {
        boolean hovered = mouseX >= x && mouseX < x + TRADE_BUTTON_W
                && mouseY >= y && mouseY < y + TRADE_BUTTON_H;
        boolean selected = ClientStackEquals(editBuffer, offer.template());

        int bg = selected ? 0x60A0E060 : (hovered ? 0x40808080 : 0);
        if (bg != 0) g.fill(x, y, x + TRADE_BUTTON_W, y + TRADE_BUTTON_H, bg);

        // Icon.
        ItemStack icon = offer.template().copy();
        icon.setCount(offer.quantityPerPurchase());
        g.renderItem(icon, x + 4, y + 2);
        g.renderItemDecorations(font, icon, x + 4, y + 2);

        // Arrow.
        g.blit(TEXTURE_PNG, x + 26, y + 7, ARROW_UV_U, ARROW_UV_V, ARROW_W, ARROW_H);

        // Price label.
        String priceStr = String.valueOf(offer.priceBronze());
        g.drawString(font, priceStr, x + 36, y + 7, 0xFFFFFF, true);

        if (hovered) {
            g.renderTooltip(font, offer.template(), mouseX, mouseY);
        }
    }

    private static boolean ClientStackEquals(ItemStack a, ItemStack b) {
        if (a.isEmpty() || b.isEmpty()) return false;
        return ItemStack.isSameItemSameTags(a, b);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (tab == 1) {
            // Click on a trade-button → load into edit buffer.
            OfferList offers = ClientShopState.getOffers();
            int baseX = leftPos + OFFERS_X;
            int baseY = topPos + OFFERS_Y;
            for (int i = 0; i < offers.size(); i++) {
                int col = i % 2;
                int row = i / 2;
                int x = baseX + col * (TRADE_BUTTON_W + 4);
                int y = baseY + row * TRADE_BUTTON_H - scroll;
                if (mouseX >= x && mouseX < x + TRADE_BUTTON_W
                        && mouseY >= y && mouseY < y + TRADE_BUTTON_H
                        && mouseY >= baseY && mouseY <= baseY + OFFERS_H) {
                    loadOfferIntoEdit(i);
                    return true;
                }
            }

            // Click on the trade-buffer fake-slot → pick up the cursor stack.
            int ex = leftPos + CUR_X_OFFSET;
            int ey = topPos;
            int bx = ex + 8, by = ey + 15;
            if (mouseX >= bx && mouseX < bx + 16 && mouseY >= by && mouseY < by + 16
                    && ClientShopState.canEdit()) {
                ItemStack cursor = menu.getCarried();
                if (!cursor.isEmpty()) {
                    editBuffer = cursor.copy();
                    editingIndex = -1;
                    editQty = 1;
                    refreshEditState();
                } else if (!editBuffer.isEmpty()) {
                    editBuffer = ItemStack.EMPTY;
                    editingIndex = -1;
                    editQty = 1;
                    if (priceField != null) priceField.setValue("");
                    refreshEditState();
                }
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        if (tab == 1) {
            // Scroll on fake-slot → change editQty.
            int px = leftPos + CUR_X_OFFSET;
            int bx = px + 8, by = topPos + 15;
            if (mouseX >= bx && mouseX < bx + 16 && mouseY >= by && mouseY < by + 16
                    && !editBuffer.isEmpty()) {
                int step = hasShiftDown() ? 8 : 1;
                bumpQty(delta > 0 ? step : -step);
                return true;
            }
            // Scroll on offer list region → scroll offers.
            int baseX = leftPos + OFFERS_X;
            int baseY = topPos + OFFERS_Y;
            if (mouseX >= baseX && mouseX < baseX + OFFERS_W
                    && mouseY >= baseY && mouseY < baseY + OFFERS_H) {
                int totalRows = (ClientShopState.getOffers().size() + 1) / 2;
                int maxScroll = Math.max(0, totalRows * TRADE_BUTTON_H - OFFERS_H);
                scroll = Math.max(0, Math.min(maxScroll, scroll - (int) (delta * TRADE_BUTTON_H)));
                return true;
            }
        }
        return super.mouseScrolled(mouseX, mouseY, delta);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode != 256 && priceField != null && priceField.isFocused()) {
            return priceField.keyPressed(keyCode, scanCode, modifiers) || priceField.canConsumeInput();
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    protected void renderLabels(GuiGraphics g, int mouseX, int mouseY) {
        if (tab == 0) {
            g.drawString(font, title, titleLabelX, titleLabelY, 0x404040, false);
        }
        if (tab == 0) {
            g.drawString(font, playerInventoryTitle,
                    inventoryLabelX, inventoryLabelY, 0x404040, false);
        }
    }

    @Override
    public void onClose() {
        super.onClose();
        ClientShopState.clear();
    }

    @Override
    protected boolean hasClickedOutside(double mouseX, double mouseY, int guiLeft, int guiTop, int mouseButton) {
        // The side panels (tabs + currency + trade-edit) sit outside the
        // background rectangle. Without this override vanilla treats clicks
        // on them as "outside" and drops the carried stack on the floor.
        int px = leftPos + CUR_X_OFFSET;

        if (tab == 1 && ClientShopState.canEdit()) {
            // Trade-edit panel at top of the right column.
            if (mouseX >= px && mouseX < px + EDIT_W
                    && mouseY >= topPos && mouseY < topPos + EDIT_H) return false;
            // Currency widget below the trade-edit panel.
            int cy = topPos + EDIT_H + 3;
            if (mouseX >= px && mouseX < px + CUR_W
                    && mouseY >= cy && mouseY < cy + CUR_H) return false;
        } else {
            // Tab 0: currency widget at top.
            if (mouseX >= px && mouseX < px + CUR_W
                    && mouseY >= topPos && mouseY < topPos + CUR_H) return false;
        }
        // Tab buttons area (left of the background).
        int tx = leftPos + TAB_X_OFFSET;
        if (mouseX >= tx && mouseX < tx + TAB_W
                && mouseY >= topPos + TAB_Y0 && mouseY < topPos + TAB_Y1 + TAB_H) return false;
        return super.hasClickedOutside(mouseX, mouseY, guiLeft, guiTop, mouseButton);
    }

    public void refreshAfterStateSync() {
        // Called when server pushes a SYNC_SHOP_STATE packet. At init() time
        // the client may not yet know the player is the owner (packet hasn't
        // arrived), so the tabs + currency widget are missing until the first
        // sync. Rebuild widgets to reflect the now-known canEdit/isAdmin.
        rebuildUI();
    }

    // -------- Inner widgets --------

    private static final class TabButton extends Button {
        private final int tabIndex;

        TabButton(int x, int y, int tabIndex, OnPress onPress) {
            super(x, y, TAB_W, TAB_H, Component.empty(), onPress, DEFAULT_NARRATION);
            this.tabIndex = tabIndex;
        }

        @Override
        public void renderWidget(GuiGraphics g, int mouseX, int mouseY, float partial) {
            // UV(113,168) for base; convention: (u, v+h) hover, (u, v+2h) disabled.
            int v = TAB_UV_V;
            if (!this.active) v += 2 * TAB_H;         // selected/active tab = disabled
            else if (this.isHoveredOrFocused()) v += TAB_H;
            g.blit(TEXTURE_PNG, getX(), getY(), TAB_UV_U, v, TAB_W, TAB_H, 256, 256);
            // Icon absolute (9, 6) relative to the tab.
            ItemStack icon = tabIndex == 0
                    ? new ItemStack(net.minecraft.world.item.Items.CHEST)
                    : new ItemStack(net.minecraft.world.item.Items.EMERALD);
            g.renderItem(icon, getX() + 9, getY() + 6);
        }
    }

    private static final class TexturedButton extends Button {
        private final ResourceLocation tex;
        private final int uvU, uvV;

        TexturedButton(int x, int y, int w, int h, int uvU, int uvV,
                       ResourceLocation tex, OnPress onPress) {
            super(x, y, w, h, Component.empty(), onPress, DEFAULT_NARRATION);
            this.tex = tex;
            this.uvU = uvU;
            this.uvV = uvV;
        }

        @Override
        public void renderWidget(GuiGraphics g, int mouseX, int mouseY, float partial) {
            int v = uvV;
            if (!this.active) v += 2 * this.height;
            else if (this.isHoveredOrFocused()) v += this.height;
            g.blit(tex, getX(), getY(), uvU, v, this.width, this.height, 256, 256);
        }
    }
}
