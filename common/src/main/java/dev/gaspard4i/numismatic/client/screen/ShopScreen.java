package dev.gaspard4i.numismatic.client.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import dev.gaspard4i.numismatic.NumismaticConstants;
import dev.gaspard4i.numismatic.client.ClientShopState;
import dev.gaspard4i.numismatic.network.NumismaticNetworking;
import dev.gaspard4i.numismatic.shop.OfferList;
import dev.gaspard4i.numismatic.shop.ShopMenu;
import dev.gaspard4i.numismatic.shop.ShopMenuMode;
import dev.gaspard4i.numismatic.shop.ShopOffer;
import dev.gaspard4i.numismatic.shop.ShopPaymentHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

/**
 * Shop UI backed by {@link ShopMenu} (vanilla container). Three tabs rendered
 * client-side (no extra menu swap): OFFERS / STOCK / CLIENT. Only STOCK shows
 * the real container slots; OFFERS and CLIENT render offers as interactive
 * cells above the background while the stock slots are hidden via
 * {@link dev.gaspard4i.numismatic.shop.HideableSlot}.
 */
public class ShopScreen extends AbstractContainerScreen<ShopMenu> {

    private static final ResourceLocation BG = new ResourceLocation(NumismaticConstants.MOD_ID, "textures/gui/shop_gui.png");
    private static final ResourceLocation TRADES_BG = new ResourceLocation(NumismaticConstants.MOD_ID, "textures/gui/shop_gui_trades.png");

    private static final int TAB_W = 32;
    private static final int TAB_H = 28;
    private static final int OFFER_ROW_H = 20;
    private static final int VISIBLE_OFFER_ROWS = 7;

    private int scroll = 0;
    private Button offersTab;
    private Button stockTab;
    private Button clientTab;
    private Button withdrawButton;

    public ShopScreen(ShopMenu menu, Inventory inv, Component title) {
        super(menu, inv, title);
        this.imageWidth = 176;
        this.imageHeight = 166;
        this.inventoryLabelY = this.imageHeight - 94;
    }

    @Override
    protected void init() {
        super.init();
        // Default to OFFERS if the viewer can edit, else CLIENT.
        if (ClientShopState.canEdit() && ClientShopState.getMode() == ShopMenuMode.CLIENT) {
            ClientShopState.setMode(ShopMenuMode.OFFERS);
        } else if (!ClientShopState.canEdit()) {
            ClientShopState.setMode(ShopMenuMode.CLIENT);
        }
        rebuildShopWidgets();
        applySlotVisibility();
    }

    private void rebuildShopWidgets() {
        clearWidgets();
        // Tabs on the left side.
        int tabX = this.leftPos - TAB_W + 3;
        if (ClientShopState.canEdit()) {
            offersTab = addRenderableWidget(Button.builder(Component.literal("Off"),
                    b -> switchTab(ShopMenuMode.OFFERS))
                    .bounds(tabX, this.topPos + 4, TAB_W, 20).build());
            stockTab = addRenderableWidget(Button.builder(Component.literal("Stk"),
                    b -> switchTab(ShopMenuMode.STOCK))
                    .bounds(tabX, this.topPos + 28, TAB_W, 20).build());
            clientTab = addRenderableWidget(Button.builder(Component.literal("Buy"),
                    b -> switchTab(ShopMenuMode.CLIENT))
                    .bounds(tabX, this.topPos + 52, TAB_W, 20).build());
        }
        // Withdraw revenue button (STOCK tab only, owner only).
        if (ClientShopState.canEdit() && ClientShopState.getMode() == ShopMenuMode.STOCK) {
            long rev = ClientShopState.getRevenue();
            withdrawButton = addRenderableWidget(Button.builder(
                    Component.translatable("gui.numismatic_reimagined.shop.withdraw_revenue",
                            String.format("%,d", rev)),
                    b -> {
                        if (ClientShopState.getPos() != null) {
                            NumismaticNetworking.sendWithdrawRevenue(ClientShopState.getPos());
                        }
                    })
                    .bounds(this.leftPos + 8, this.topPos + this.imageHeight + 4,
                            this.imageWidth - 16, 20).build());
        }
        // Add offer button (OFFERS tab only, owner only).
        if (ClientShopState.canEdit() && ClientShopState.getMode() == ShopMenuMode.OFFERS) {
            addRenderableWidget(Button.builder(
                    Component.translatable("gui.numismatic_reimagined.shop.new_offer"),
                    b -> Minecraft.getInstance().setScreen(
                            new OfferEditScreen(this, -1, ItemStack.EMPTY, 1, 1)))
                    .bounds(this.leftPos + 8, this.topPos + this.imageHeight + 4,
                            this.imageWidth - 16, 20).build());
        }
    }

    private void switchTab(ShopMenuMode mode) {
        ClientShopState.setMode(mode);
        scroll = 0;
        rebuildShopWidgets();
        applySlotVisibility();
    }

    /**
     * Shows real container slots only in STOCK (with edit rights) so buyers
     * and OFFERS-tab viewers can't drop items into the shop's stock.
     */
    private void applySlotVisibility() {
        boolean stockVisible = ClientShopState.getMode() == ShopMenuMode.STOCK
                && ClientShopState.canEdit();
        this.menu.setStockVisible(stockVisible);
        this.menu.setStockReadOnly(!stockVisible);
    }

    // ------------------------------------------------------------------
    // Rendering
    // ------------------------------------------------------------------

    @Override
    protected void renderBg(GuiGraphics g, float partialTick, int mouseX, int mouseY) {
        RenderSystem.setShaderColor(1, 1, 1, 1);
        ShopMenuMode mode = ClientShopState.getMode();
        ResourceLocation tex = mode == ShopMenuMode.STOCK ? BG : TRADES_BG;
        g.blit(tex, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight);
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(g);
        super.render(g, mouseX, mouseY, partialTick);

        ShopMenuMode mode = ClientShopState.getMode();
        if (mode == ShopMenuMode.OFFERS) renderOffersTab(g, mouseX, mouseY);
        else if (mode == ShopMenuMode.CLIENT) renderClientTab(g, mouseX, mouseY);
        else renderStockTab(g, mouseX, mouseY);

        this.renderTooltip(g, mouseX, mouseY);
    }

    @Override
    protected void renderLabels(GuiGraphics g, int mouseX, int mouseY) {
        g.drawString(this.font, this.title, this.titleLabelX, this.titleLabelY, 0x404040, false);
        g.drawString(this.font, this.playerInventoryTitle, this.inventoryLabelX, this.inventoryLabelY, 0x404040, false);
    }

    // ---------- OFFERS tab ----------

    private void renderOffersTab(GuiGraphics g, int mouseX, int mouseY) {
        OfferList offers = ClientShopState.getOffers();
        int listLeft = this.leftPos + 8;
        int listTop = this.topPos + 18;
        int listWidth = this.imageWidth - 16;
        for (int i = 0; i < VISIBLE_OFFER_ROWS; i++) {
            int idx = scroll + i;
            if (idx >= offers.size()) break;
            ShopOffer offer = offers.get(idx);
            if (offer == null) continue;
            int y = listTop + i * OFFER_ROW_H;
            renderOfferRow(g, listLeft, y, listWidth, offer, idx, mouseX, mouseY, true, 0L);
        }
    }

    // ---------- CLIENT tab ----------

    private void renderClientTab(GuiGraphics g, int mouseX, int mouseY) {
        OfferList offers = ClientShopState.getOffers();
        long money = Minecraft.getInstance().player != null
                ? ShopPaymentHelper.countCoinsAndBags(Minecraft.getInstance().player)
                : 0L;
        int listLeft = this.leftPos + 8;
        int listTop = this.topPos + 18;
        int listWidth = this.imageWidth - 16;
        for (int i = 0; i < VISIBLE_OFFER_ROWS; i++) {
            int idx = scroll + i;
            if (idx >= offers.size()) break;
            ShopOffer offer = offers.get(idx);
            if (offer == null) continue;
            int y = listTop + i * OFFER_ROW_H;
            renderOfferRow(g, listLeft, y, listWidth, offer, idx, mouseX, mouseY, false, money);
        }
    }

    // ---------- STOCK tab ----------

    private void renderStockTab(GuiGraphics g, int mouseX, int mouseY) {
        g.drawString(this.font,
                Component.translatable("gui.numismatic_reimagined.shop.revenue",
                        String.format("%,d", ClientShopState.getRevenue())),
                this.leftPos + 8, this.topPos + 6, 0xFFAA00, false);
    }

    // ---------- Shared offer-row rendering ----------

    private void renderOfferRow(GuiGraphics g, int x, int y, int w, ShopOffer offer, int idx,
                                int mouseX, int mouseY, boolean edit, long playerMoney) {
        boolean hovered = mouseX >= x && mouseX < x + w && mouseY >= y && mouseY < y + OFFER_ROW_H;
        boolean stockOk = ClientShopState.isAdmin()
                || countClientStock(offer) >= offer.quantityPerPurchase();
        boolean fundsOk = edit || playerMoney >= offer.priceBronze();

        int bg = 0xFF2A2A2A;
        if (!edit) bg = (stockOk && fundsOk) ? 0xFF1E3A1E : 0xFF3A1E1E;
        if (hovered) bg = 0xFF4A4A4A;
        g.fill(x, y, x + w, y + OFFER_ROW_H - 2, bg);

        ItemStack template = offer.template();
        g.renderItem(template, x + 3, y + 2);
        g.renderItemDecorations(this.font, template, x + 3, y + 2);

        String countText = "x" + offer.quantityPerPurchase();
        g.drawString(this.font, countText, x + 22, y + 6, 0xFFFFFF, false);

        String priceText = String.format("%,d c", offer.priceBronze());
        int priceColor = edit || fundsOk ? 0xFFD700 : 0xFF6060;
        int priceX = x + w - this.font.width(priceText) - 4;
        g.drawString(this.font, priceText, priceX, y + 6, priceColor, false);

        if (!stockOk) {
            g.fill(x + 2, y + OFFER_ROW_H / 2 - 1,
                    x + w - 2, y + OFFER_ROW_H / 2, 0xFFFF5555);
        }

        if (hovered) {
            g.renderTooltip(this.font, template, mouseX, mouseY);
        }
    }

    private int countClientStock(ShopOffer offer) {
        if (ClientShopState.isAdmin()) return Integer.MAX_VALUE;
        int total = 0;
        for (ItemStack s : ClientShopState.getStock()) {
            if (s.isEmpty()) continue;
            if (s.is(offer.template().getItem())) total += s.getCount();
        }
        return total;
    }

    // ------------------------------------------------------------------
    // Input
    // ------------------------------------------------------------------

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        ShopMenuMode mode = ClientShopState.getMode();
        if (mode == ShopMenuMode.OFFERS || mode == ShopMenuMode.CLIENT) {
            OfferList offers = ClientShopState.getOffers();
            int listLeft = this.leftPos + 8;
            int listTop = this.topPos + 18;
            int listWidth = this.imageWidth - 16;
            for (int i = 0; i < VISIBLE_OFFER_ROWS; i++) {
                int idx = scroll + i;
                if (idx >= offers.size()) break;
                int y = listTop + i * OFFER_ROW_H;
                if (mouseX < listLeft || mouseX >= listLeft + listWidth) continue;
                if (mouseY < y || mouseY >= y + OFFER_ROW_H) continue;
                ShopOffer offer = offers.get(idx);
                if (offer == null) continue;
                if (mode == ShopMenuMode.OFFERS) {
                    if (button == 0) {
                        Minecraft.getInstance().setScreen(
                                new OfferEditScreen(this, idx,
                                        offer.template(), offer.priceBronze(),
                                        offer.quantityPerPurchase()));
                        return true;
                    } else if (button == 1) {
                        if (ClientShopState.getPos() != null) {
                            NumismaticNetworking.sendRemoveOffer(ClientShopState.getPos(), idx);
                        }
                        return true;
                    }
                } else if (button == 0 && ClientShopState.getPos() != null) {
                    NumismaticNetworking.sendPurchaseOffer(ClientShopState.getPos(), idx);
                    return true;
                }
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        ShopMenuMode mode = ClientShopState.getMode();
        if (mode == ShopMenuMode.OFFERS || mode == ShopMenuMode.CLIENT) {
            int max = Math.max(0, ClientShopState.getOffers().size() - VISIBLE_OFFER_ROWS);
            if (delta > 0 && scroll > 0) scroll--;
            else if (delta < 0 && scroll < max) scroll++;
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, delta);
    }

    /** Called by the sync packet when the server pushes new shop state. */
    public void refreshAfterStateSync() {
        rebuildShopWidgets();
    }

    @Override
    public void onClose() {
        super.onClose();
        ClientShopState.clear();
    }
}
