package dev.gaspard4i.numismatic.client.screen;

import dev.gaspard4i.numismatic.client.ClientShopState;
import dev.gaspard4i.numismatic.network.NumismaticNetworking;
import dev.gaspard4i.numismatic.shop.OfferList;
import dev.gaspard4i.numismatic.shop.ShopMenu;
import dev.gaspard4i.numismatic.shop.ShopOffer;
import dev.gaspard4i.numismatic.shop.ShopPaymentHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

/**
 * Shop UI. Two tabs (Offers / Stock) for the owner; clients see only the
 * offers list with buy buttons. The screen extends {@link AbstractContainerScreen}
 * so the player's inventory + the 27 stock slots remain accessible (drag/drop)
 * in the Stock tab, and offers are drawn as an overlay panel in the Offers tab.
 */
public class ShopScreen extends AbstractContainerScreen<ShopMenu> {

    public enum Tab { OFFERS, STOCK }

    private static final int OFFER_ROW_H = 22;
    private static final int VISIBLE_ROWS = 5;
    private static final int LIST_W = 158;

    private Tab currentTab = Tab.STOCK;
    private boolean testMode = false;
    private int scroll = 0;

    private Button offersTabBtn;
    private Button stockTabBtn;
    private Button testToggleBtn;
    private Button addOfferBtn;
    private Button withdrawBtn;

    public ShopScreen(ShopMenu menu, Inventory inv, Component title) {
        super(menu, inv, title);
        this.imageWidth = 176;
        this.imageHeight = 166;
        this.inventoryLabelY = this.imageHeight - 94;
    }

    @Override
    protected void init() {
        super.init();
        if (!ClientShopState.canEdit()) {
            currentTab = Tab.OFFERS;
            testMode = true;
        }
        rebuildButtons();
    }

    private void rebuildButtons() {
        clearWidgets();

        // Tabs (top, side by side)
        int tabY = this.topPos - 22;
        int tabW = 60;
        if (ClientShopState.canEdit() && !ClientShopState.isAdmin()) {
            offersTabBtn = addRenderableWidget(Button.builder(
                    Component.translatable("gui.numismatic_reimagined.shop.tab.offers"),
                    b -> { currentTab = Tab.OFFERS; scroll = 0; rebuildButtons(); })
                    .bounds(this.leftPos, tabY, tabW, 20).build());
            stockTabBtn = addRenderableWidget(Button.builder(
                    Component.translatable("gui.numismatic_reimagined.shop.tab.stock"),
                    b -> { currentTab = Tab.STOCK; scroll = 0; rebuildButtons(); })
                    .bounds(this.leftPos + tabW + 2, tabY, tabW, 20).build());
        }

        if (currentTab == Tab.OFFERS) {
            // Test toggle (owner only)
            if (ClientShopState.canEdit()) {
                testToggleBtn = addRenderableWidget(Button.builder(
                        testMode
                            ? Component.translatable("gui.numismatic_reimagined.shop.test_off")
                            : Component.translatable("gui.numismatic_reimagined.shop.test_on"),
                        b -> { testMode = !testMode; rebuildButtons(); })
                        .bounds(this.leftPos + tabW * 2 + 4, tabY, 70, 20).build());
            }
            // New offer button (owner non-test only)
            if (ClientShopState.canEdit() && !testMode) {
                addOfferBtn = addRenderableWidget(Button.builder(
                        Component.translatable("gui.numismatic_reimagined.shop.new_offer"),
                        b -> openEditPopup(-1, ItemStack.EMPTY, 1, 1))
                        .bounds(this.leftPos + 8, this.topPos + this.imageHeight - 22,
                                this.imageWidth - 16, 18).build());
            }
        } else {
            // Stock tab: withdraw revenue button
            if (ClientShopState.canEdit() && !ClientShopState.isAdmin()) {
                long rev = ClientShopState.getRevenue();
                withdrawBtn = addRenderableWidget(Button.builder(
                        Component.translatable("gui.numismatic_reimagined.shop.withdraw_revenue",
                                String.format("%,d", rev)),
                        b -> {
                            if (ClientShopState.getPos() != null)
                                NumismaticNetworking.sendWithdrawRevenue(ClientShopState.getPos());
                        })
                        .bounds(this.leftPos + 8, this.topPos + this.imageHeight - 22,
                                this.imageWidth - 16, 18).build());
            }
        }
    }

    private void openEditPopup(int slotIndex, ItemStack template, long price, int qty) {
        Minecraft.getInstance().setScreen(new OfferEditScreen(this, slotIndex, template, price, qty));
    }

    @Override
    protected void renderBg(GuiGraphics g, float partialTick, int mouseX, int mouseY) {
        // Solid panel as background (avoid texture mismatches that can render
        // garbage / overlap with our overlays)
        g.fill(this.leftPos, this.topPos, this.leftPos + this.imageWidth, this.topPos + this.imageHeight, 0xFFC6C6C6);
        g.fill(this.leftPos + 1, this.topPos + 1, this.leftPos + this.imageWidth - 1, this.topPos + this.imageHeight - 1, 0xFF8B8B8B);
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(g);
        super.render(g, mouseX, mouseY, partialTick);

        if (currentTab == Tab.OFFERS) {
            renderOffersOverlay(g, mouseX, mouseY);
        }
        renderTooltip(g, mouseX, mouseY);
    }

    @Override
    protected void renderLabels(GuiGraphics g, int mouseX, int mouseY) {
        g.drawString(this.font, this.title, this.titleLabelX, this.titleLabelY, 0x404040, false);
        if (currentTab == Tab.STOCK) {
            g.drawString(this.font, this.playerInventoryTitle,
                    this.inventoryLabelX, this.inventoryLabelY, 0x404040, false);
        }
    }

    private void renderOffersOverlay(GuiGraphics g, int mouseX, int mouseY) {
        OfferList offers = ClientShopState.getOffers();
        long money = Minecraft.getInstance().player != null
                ? ShopPaymentHelper.countCoinsAndBags(Minecraft.getInstance().player) : 0;

        int listX = this.leftPos + 8;
        int listY = this.topPos + 16;
        int listH = VISIBLE_ROWS * OFFER_ROW_H;

        // Black panel covering the slot area
        g.fill(listX - 1, listY - 1, listX + LIST_W + 1, listY + listH + 1, 0xFF000000);
        g.fill(listX, listY, listX + LIST_W, listY + listH, 0xFF1F1F1F);

        for (int i = 0; i < VISIBLE_ROWS; i++) {
            int idx = scroll + i;
            if (idx >= offers.size()) break;
            ShopOffer offer = offers.get(idx);
            if (offer == null) continue;
            int rowY = listY + i * OFFER_ROW_H;
            renderOfferRow(g, listX, rowY, LIST_W, OFFER_ROW_H - 2, offer, money, mouseX, mouseY, idx);
        }

        // Scrollbar
        if (offers.size() > VISIBLE_ROWS) {
            int barX = listX + LIST_W - 6;
            g.fill(barX, listY, barX + 4, listY + listH, 0xFF000000);
            int max = Math.max(1, offers.size() - VISIBLE_ROWS);
            int thumbH = Math.max(8, listH * VISIBLE_ROWS / offers.size());
            int thumbY = listY + (listH - thumbH) * scroll / max;
            g.fill(barX, thumbY, barX + 4, thumbY + thumbH, 0xFFAAAAAA);
        }

        // Empty state
        if (offers.isEmpty()) {
            String msg = ClientShopState.canEdit() && !testMode
                    ? "No offers yet — click + New offer"
                    : "No offers available";
            int tw = this.font.width(msg);
            g.drawString(this.font, msg, listX + (LIST_W - tw) / 2, listY + listH / 2 - 4, 0xAAAAAA, false);
        }
    }

    private void renderOfferRow(GuiGraphics g, int x, int y, int w, int h, ShopOffer offer,
                                long playerMoney, int mouseX, int mouseY, int index) {
        boolean clientView = !ClientShopState.canEdit() || testMode;
        boolean fundsOk = !clientView || playerMoney >= offer.priceBronze();
        boolean hovered = mouseX >= x && mouseX < x + w && mouseY >= y && mouseY < y + h;

        int bg;
        if (clientView) bg = fundsOk ? (hovered ? 0xFF2D6D2D : 0xFF1F4D1F)
                                     : (hovered ? 0xFF6D2D2D : 0xFF4D1F1F);
        else bg = hovered ? 0xFF4A4A4A : 0xFF2A2A2A;
        g.fill(x + 1, y, x + w - 1, y + h, bg);

        // Item icon
        ItemStack template = offer.template();
        g.renderItem(template, x + 3, y + 3);
        g.renderItemDecorations(this.font, template, x + 3, y + 3);

        // Quantity
        String qtyStr = "x" + offer.quantityPerPurchase();
        g.drawString(this.font, qtyStr, x + 22, y + 7, 0xFFFFFF, true);

        // Price (right aligned)
        String priceStr = String.format("%,d", offer.priceBronze());
        int priceColor = (!clientView || fundsOk) ? 0xFFD700 : 0xFFFF6060;
        int priceW = this.font.width(priceStr);
        int priceX = x + w - priceW - 14;
        g.drawString(this.font, priceStr, priceX, y + 7, priceColor, true);

        if (hovered) {
            g.renderTooltip(this.font, template, mouseX, mouseY);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (currentTab == Tab.OFFERS) {
            OfferList offers = ClientShopState.getOffers();
            int listX = this.leftPos + 8;
            int listY = this.topPos + 16;
            for (int i = 0; i < VISIBLE_ROWS; i++) {
                int idx = scroll + i;
                if (idx >= offers.size()) break;
                int rowY = listY + i * OFFER_ROW_H;
                if (mouseX < listX || mouseX >= listX + LIST_W) continue;
                if (mouseY < rowY || mouseY >= rowY + OFFER_ROW_H - 2) continue;
                ShopOffer offer = offers.get(idx);
                if (offer == null) continue;
                boolean clientView = !ClientShopState.canEdit() || testMode;
                if (clientView) {
                    if (button == 0 && ClientShopState.getPos() != null) {
                        NumismaticNetworking.sendPurchaseOffer(ClientShopState.getPos(), idx);
                        return true;
                    }
                } else {
                    if (button == 0) {
                        openEditPopup(idx, offer.template(), offer.priceBronze(), offer.quantityPerPurchase());
                        return true;
                    } else if (button == 1 && ClientShopState.getPos() != null) {
                        NumismaticNetworking.sendRemoveOffer(ClientShopState.getPos(), idx);
                        return true;
                    }
                }
            }
            // Don't fall through to vanilla container clicks while on Offers tab
            // (otherwise clicks land on hidden slots underneath).
            // Still allow widget clicks (buttons).
            return super.mouseClicked(mouseX, mouseY, button);
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        if (currentTab == Tab.OFFERS) {
            int max = Math.max(0, ClientShopState.getOffers().size() - VISIBLE_ROWS);
            if (delta > 0 && scroll > 0) scroll--;
            else if (delta < 0 && scroll < max) scroll++;
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, delta);
    }

    public void refreshAfterStateSync() {
        rebuildButtons();
    }

    @Override
    public void onClose() {
        super.onClose();
        ClientShopState.clear();
    }
}
