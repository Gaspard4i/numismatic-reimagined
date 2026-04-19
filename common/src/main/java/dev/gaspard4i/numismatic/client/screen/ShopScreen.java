package dev.gaspard4i.numismatic.client.screen;

import dev.gaspard4i.numismatic.client.ClientShopState;
import dev.gaspard4i.numismatic.client.widgets.GuiTextures;
import dev.gaspard4i.numismatic.client.widgets.IconButton;
import dev.gaspard4i.numismatic.network.ClientCurrencyData;
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
import net.minecraft.world.item.Items;

/**
 * Shop UI. Three "tabs" rendered client-side via vertical IconButtons on
 * the left of the inventory texture: Offers (owner edit), Stock (vanilla
 * 3-row layout, drag/drop), and a "test mode" toggle for owners that
 * swaps the Offers tab into a buyer-style purchase view.
 */
public class ShopScreen extends AbstractContainerScreen<ShopMenu> {

    public enum Tab { OFFERS, STOCK }

    private static final int ROW_H = 18;
    private static final int VISIBLE_ROWS = 7;

    private Tab currentTab = Tab.STOCK;
    private boolean testMode = false;
    private int scroll = 0;

    private IconButton offersTab;
    private IconButton stockTab;
    private Button testToggle;
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
        rebuild();
    }

    private void rebuild() {
        clearWidgets();

        int tabX = this.leftPos - 28;
        offersTab = addRenderableWidget(new IconButton(tabX, this.topPos + 4,
                26, 26, new ItemStack(Items.EMERALD),
                Component.translatable("gui.numismatic_reimagined.shop.tab.offers"),
                b -> { currentTab = Tab.OFFERS; scroll = 0; rebuild(); })
                .tooltip(Component.translatable("gui.numismatic_reimagined.shop.tab.offers")));
        offersTab.setSelected(currentTab == Tab.OFFERS);

        if (ClientShopState.canEdit() && !ClientShopState.isAdmin()) {
            stockTab = addRenderableWidget(new IconButton(tabX, this.topPos + 32,
                    26, 26, new ItemStack(Items.CHEST),
                    Component.translatable("gui.numismatic_reimagined.shop.tab.stock"),
                    b -> { currentTab = Tab.STOCK; scroll = 0; rebuild(); })
                    .tooltip(Component.translatable("gui.numismatic_reimagined.shop.tab.stock")));
            stockTab.setSelected(currentTab == Tab.STOCK);
        }

        if (ClientShopState.canEdit() && currentTab == Tab.OFFERS) {
            testToggle = addRenderableWidget(Button.builder(
                    testMode ? Component.translatable("gui.numismatic_reimagined.shop.test_off")
                             : Component.translatable("gui.numismatic_reimagined.shop.test_on"),
                    b -> { testMode = !testMode; rebuild(); })
                    .bounds(this.leftPos + this.imageWidth + 4, this.topPos + 4, 80, 18).build());

            if (!testMode) {
                addOfferBtn = addRenderableWidget(Button.builder(
                        Component.translatable("gui.numismatic_reimagined.shop.new_offer"),
                        b -> openEditPopup(-1, ItemStack.EMPTY, 1, 1))
                        .bounds(this.leftPos + 8, this.topPos + this.imageHeight - 24,
                                this.imageWidth - 16, 18).build());
            }
        }

        if (ClientShopState.canEdit() && !ClientShopState.isAdmin() && currentTab == Tab.STOCK) {
            long rev = ClientShopState.getRevenue();
            withdrawBtn = addRenderableWidget(Button.builder(
                    Component.translatable("gui.numismatic_reimagined.shop.withdraw_revenue",
                            String.format("%,d", rev)),
                    b -> {
                        if (ClientShopState.getPos() != null)
                            NumismaticNetworking.sendWithdrawRevenue(ClientShopState.getPos());
                    })
                    .bounds(this.leftPos + this.imageWidth + 4, this.topPos + 26, 100, 18)
                    .build());
        }
    }

    private void openEditPopup(int slotIndex, ItemStack template, long price, int qty) {
        Minecraft.getInstance().setScreen(new OfferEditScreen(this, slotIndex, template, price, qty));
    }

    @Override
    protected void renderBg(GuiGraphics g, float partialTick, int mouseX, int mouseY) {
        GuiTextures bg = currentTab == Tab.STOCK ? GuiTextures.SHOP_GUI : GuiTextures.SHOP_GUI_TRADES;
        bg.render(g, this.leftPos, this.topPos);
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(g);
        super.render(g, mouseX, mouseY, partialTick);

        if (currentTab == Tab.OFFERS) {
            renderOffersList(g, mouseX, mouseY);
        }
        renderTooltip(g, mouseX, mouseY);
    }

    @Override
    protected void renderLabels(GuiGraphics g, int mouseX, int mouseY) {
        g.drawString(this.font, this.title, this.titleLabelX, this.titleLabelY, 0x404040, false);
        if (currentTab == Tab.OFFERS) {
            // hide vanilla "Inventory" label so the offers list reads cleanly
            g.drawString(this.font, this.playerInventoryTitle, this.inventoryLabelX, this.inventoryLabelY, 0x404040, false);
        } else {
            g.drawString(this.font, this.playerInventoryTitle, this.inventoryLabelX, this.inventoryLabelY, 0x404040, false);
        }
    }

    private void renderOffersList(GuiGraphics g, int mouseX, int mouseY) {
        OfferList offers = ClientShopState.getOffers();
        long money = Minecraft.getInstance().player != null
                ? ShopPaymentHelper.countCoinsAndBags(Minecraft.getInstance().player) : 0;
        int listX = this.leftPos + 8;
        int listY = this.topPos + 18;
        int listW = this.imageWidth - 16;

        // Hide vanilla container slots visually behind a panel for the offers tab
        g.fill(listX - 1, listY - 1, listX + listW + 1, listY + VISIBLE_ROWS * ROW_H + 1, 0x80000000);

        for (int i = 0; i < VISIBLE_ROWS; i++) {
            int idx = scroll + i;
            if (idx >= offers.size()) break;
            ShopOffer offer = offers.get(idx);
            if (offer == null) continue;
            int rowY = listY + i * ROW_H;
            renderOfferRow(g, listX, rowY, listW, ROW_H - 1, offer, money, mouseX, mouseY);
        }
        if (offers.size() > VISIBLE_ROWS) {
            int barX = listX + listW - 4;
            g.fill(barX, listY, barX + 4, listY + VISIBLE_ROWS * ROW_H, 0xFF202020);
            int max = Math.max(1, offers.size() - VISIBLE_ROWS);
            int thumbH = Math.max(8, VISIBLE_ROWS * ROW_H * VISIBLE_ROWS / offers.size());
            int thumbY = listY + (VISIBLE_ROWS * ROW_H - thumbH) * scroll / max;
            g.fill(barX, thumbY, barX + 4, thumbY + thumbH, 0xFFAAAAAA);
        }
    }

    private void renderOfferRow(GuiGraphics g, int x, int y, int w, int h, ShopOffer offer,
                                long playerMoney, int mouseX, int mouseY) {
        boolean clientView = !ClientShopState.canEdit() || testMode;
        boolean stockOk = ClientShopState.isAdmin() || true; // simplified: rely on server validation
        boolean fundsOk = !clientView || playerMoney >= offer.priceBronze();
        boolean hovered = mouseX >= x && mouseX < x + w && mouseY >= y && mouseY < y + h;

        int bg;
        if (clientView) bg = (stockOk && fundsOk) ? 0xFF1F4D1F : 0xFF4D1F1F;
        else bg = hovered ? 0xFF4A4A4A : 0xFF2A2A2A;
        g.fill(x, y, x + w, y + h, bg);

        ItemStack template = offer.template();
        g.renderItem(template, x + 2, y + 1);
        g.renderItemDecorations(this.font, template, x + 2, y + 1);
        g.drawString(this.font, "x" + offer.quantityPerPurchase(), x + 22, y + 5, 0xFFFFFF, false);

        String price = String.format("%,d c", offer.priceBronze());
        int color = (!clientView || fundsOk) ? 0xFFD700 : 0xFFFF6060;
        int priceX = x + w - this.font.width(price) - 12;
        g.drawString(this.font, price, priceX, y + 5, color, false);

        if (hovered) g.renderTooltip(this.font, template, mouseX, mouseY);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (currentTab == Tab.OFFERS) {
            OfferList offers = ClientShopState.getOffers();
            int listX = this.leftPos + 8;
            int listY = this.topPos + 18;
            int listW = this.imageWidth - 16;
            for (int i = 0; i < VISIBLE_ROWS; i++) {
                int idx = scroll + i;
                if (idx >= offers.size()) break;
                int rowY = listY + i * ROW_H;
                if (mouseX < listX || mouseX >= listX + listW) continue;
                if (mouseY < rowY || mouseY >= rowY + ROW_H) continue;
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
                    } else if (button == 1) {
                        if (ClientShopState.getPos() != null)
                            NumismaticNetworking.sendRemoveOffer(ClientShopState.getPos(), idx);
                        return true;
                    }
                }
            }
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
        rebuild();
    }

    @Override
    public void onClose() {
        super.onClose();
        ClientShopState.clear();
    }
}
