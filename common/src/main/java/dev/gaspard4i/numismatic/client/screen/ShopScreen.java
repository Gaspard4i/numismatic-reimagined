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

    // Currency widget (right column).
    private static final int CUR_UV_U = 146, CUR_UV_V = 169, CUR_W = 34, CUR_H = 54;
    private static final int CUR_X_OFFSET = BG_W + 2; // right-column padding-left 2
    private static final int EXTRACT_UV_U = 146, EXTRACT_UV_V = 224, EXTRACT_W = 26, EXTRACT_H = 8;

    // Trade-edit widget (owner, tab=1).
    private static final int EDIT_UV_U = 15, EDIT_UV_V = 169, EDIT_W = 98, EDIT_H = 54;
    private static final int SUBMIT_UV_U = 15, SUBMIT_UV_V = 223, SUBMIT_W = 41, SUBMIT_H = 11;
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

        // --- Currency widget extract button (owner only)
        if (owner && !ClientShopState.isAdmin()) {
            extractBtn = addRenderableWidget(new TexturedButton(
                    leftPos + CUR_X_OFFSET + 4, topPos + 41,
                    EXTRACT_W, EXTRACT_H, EXTRACT_UV_U, EXTRACT_UV_V,
                    TEXTURE_PNG,
                    b -> {
                        BlockPos pos = ClientShopState.getPos();
                        if (pos != null) NumismaticNetworking.sendWithdrawRevenue(pos);
                    }));
            extractBtn.active = ClientShopState.getRevenue() > 0;
        }

        // --- Trade edit widget (owner + tab=1)
        if (owner && tab == 1) {
            int ex = leftPos + CUR_X_OFFSET;
            int ey = topPos + CUR_H + 3; // below currency widget + margin 3

            priceField = new EditBox(font, ex + 35, ey + 18, 47, 11, Component.empty());
            priceField.setMaxLength(7);
            priceField.setBordered(false);
            priceField.setFilter(s -> s.matches("\\d*"));
            priceField.setResponder(s -> refreshEditState());
            addRenderableWidget(priceField);

            submitBtn = addRenderableWidget(new TexturedButton(
                    ex + 7, ey + 36, SUBMIT_W, SUBMIT_H, SUBMIT_UV_U, SUBMIT_UV_V,
                    TEXTURE_PNG, b -> onSubmit()));

            deleteBtn = addRenderableWidget(new TexturedButton(
                    ex + 50, ey + 36, SUBMIT_W, SUBMIT_H, DELETE_UV_U, SUBMIT_UV_V,
                    TEXTURE_PNG, b -> onDelete()));

            refreshEditState();
        }
    }

    private void selectTab(int newTab) {
        if (this.tab == newTab) return;
        this.tab = newTab;
        this.scroll = 0;
        if (newTab == 0) editBuffer = ItemStack.EMPTY;
        rebuildUI();
    }

    private void refreshEditState() {
        boolean canSubmit = !editBuffer.isEmpty()
                && priceField != null
                && !priceField.getValue().isEmpty()
                && parsePrice() > 0;
        if (submitBtn != null) submitBtn.active = canSubmit;
        if (deleteBtn != null) deleteBtn.active = hasOfferFor(editBuffer);
    }

    private long parsePrice() {
        try { return Long.parseLong(priceField.getValue()); }
        catch (NumberFormatException e) { return 0; }
    }

    private boolean hasOfferFor(ItemStack stack) {
        if (stack.isEmpty()) return false;
        for (ShopOffer o : ClientShopState.getOffers().asList()) {
            if (ItemStack.isSameItemSameTags(o.template(), stack)) return true;
        }
        return false;
    }

    private void onSubmit() {
        BlockPos pos = ClientShopState.getPos();
        if (pos == null || editBuffer.isEmpty()) return;
        long price = parsePrice();
        if (price <= 0) return;
        ItemStack template = editBuffer.copy();
        int qty = template.getCount() > 0 ? template.getCount() : 1;
        template.setCount(1);
        NumismaticNetworking.sendEditOffer(pos, -1, template, price, qty);
    }

    private void onDelete() {
        BlockPos pos = ClientShopState.getPos();
        if (pos == null || editBuffer.isEmpty()) return;
        int idx = findOfferIndex(editBuffer);
        if (idx < 0) return;
        NumismaticNetworking.sendRemoveOffer(pos, idx);
        editBuffer = ItemStack.EMPTY;
        if (priceField != null) priceField.setValue("");
    }

    private int findOfferIndex(ItemStack stack) {
        var list = ClientShopState.getOffers().asList();
        for (int i = 0; i < list.size(); i++) {
            if (ItemStack.isSameItemSameTags(list.get(i).template(), stack)) return i;
        }
        return -1;
    }

    @Override
    protected void renderBg(GuiGraphics g, float partial, int mouseX, int mouseY) {
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);

        ResourceLocation bg = tab == 1 ? TRADES_TEXTURE : TEXTURE_PNG;
        g.blit(bg, leftPos, topPos, 0, 0, BG_W, BG_H);

        // Currency widget background (right column).
        g.blit(TEXTURE_PNG, leftPos + CUR_X_OFFSET, topPos,
                CUR_UV_U, CUR_UV_V, CUR_W, CUR_H);

        if (tab == 1 && ClientShopState.canEdit()) {
            int ex = leftPos + CUR_X_OFFSET;
            int ey = topPos + CUR_H + 3;
            // Trade-edit background.
            g.blit(TEXTURE_PNG, ex, ey, EDIT_UV_U, EDIT_UV_V, EDIT_W, EDIT_H);
            // Fake slot for the edit buffer (draws the item at 8,15 relative to edit bg).
            if (!editBuffer.isEmpty()) {
                g.renderItem(editBuffer, ex + 8, ey + 15);
                g.renderItemDecorations(font, editBuffer, ex + 8, ey + 15);
            }
        }
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partial) {
        renderBackground(g);
        super.render(g, mouseX, mouseY, partial);

        if (tab == 1) renderOffers(g, mouseX, mouseY);

        renderCurrencyLabels(g);
        if (tab == 1 && ClientShopState.canEdit()) renderEditPanelLabels(g);

        renderTooltip(g, mouseX, mouseY);
    }

    private void renderCurrencyLabels(GuiGraphics g) {
        long stored = ClientShopState.getRevenue();
        long[] split = splitValues(stored);
        int cx = leftPos + CUR_X_OFFSET + 5;
        g.drawString(font, String.valueOf(split[2]), cx, topPos + 7, 0x404040, false);  // gold
        g.drawString(font, String.valueOf(split[1]), cx, topPos + 19, 0x404040, false); // silver
        g.drawString(font, String.valueOf(split[0]), cx, topPos + 31, 0x404040, false); // bronze
        if (extractBtn != null) extractBtn.active = stored > 0;
    }

    private void renderEditPanelLabels(GuiGraphics g) {
        if (priceField == null) return;
        long price = parsePrice();
        long[] split = splitValues(price);
        int ex = leftPos + CUR_X_OFFSET;
        int ey = topPos + CUR_H + 3;
        int color = 0x898989;
        // Position absolute (36, 5), widths 12/12/18 margin-left 8 between each.
        int baseX = ex + 36;
        int baseY = ey + 5;
        g.drawString(font, String.valueOf(split[0]), baseX, baseY, color, false);
        g.drawString(font, String.valueOf(split[1]), baseX + 12 + 8, baseY, color, false);
        g.drawString(font, String.valueOf(split[2]), baseX + 12 + 8 + 12 + 8, baseY, color, false);
    }

    /** Returns {bronze, silver, gold} (indices 0..2). */
    private static long[] splitValues(long price) {
        long gold = price / Currency.GOLD.getValue();
        long rest = price % Currency.GOLD.getValue();
        long silver = rest / Currency.SILVER.getValue();
        long bronze = rest % Currency.SILVER.getValue();
        return new long[]{ bronze, silver, gold };
    }

    private void renderOffers(GuiGraphics g, int mouseX, int mouseY) {
        OfferList offers = ClientShopState.getOffers();
        int baseX = leftPos + OFFERS_X;
        int baseY = topPos + OFFERS_Y;

        // Clip region for the offers list.
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
                    ShopOffer offer = offers.get(i);
                    if (offer != null) {
                        editBuffer = offer.template().copy();
                        editBuffer.setCount(offer.quantityPerPurchase());
                        if (priceField != null) priceField.setValue(String.valueOf(offer.priceBronze()));
                        refreshEditState();
                    }
                    return true;
                }
            }

            // Click on the trade-buffer fake-slot → pick up the cursor stack.
            int ex = leftPos + CUR_X_OFFSET;
            int ey = topPos + CUR_H + 3;
            int bx = ex + 8, by = ey + 15;
            if (mouseX >= bx && mouseX < bx + 16 && mouseY >= by && mouseY < by + 16
                    && ClientShopState.canEdit()) {
                ItemStack cursor = menu.getCarried();
                if (!cursor.isEmpty()) {
                    editBuffer = cursor.copy();
                    refreshEditState();
                } else if (!editBuffer.isEmpty()) {
                    editBuffer = ItemStack.EMPTY;
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
            int totalRows = (ClientShopState.getOffers().size() + 1) / 2;
            int maxScroll = Math.max(0, totalRows * TRADE_BUTTON_H - OFFERS_H);
            scroll = Math.max(0, Math.min(maxScroll, scroll - (int) (delta * TRADE_BUTTON_H)));
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, delta);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (priceField != null && priceField.isFocused() && keyCode != 256) {
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

    public void refreshAfterStateSync() {
        // Called when server pushes a SYNC_SHOP_STATE packet.
        if (submitBtn != null || deleteBtn != null) refreshEditState();
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
