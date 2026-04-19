package dev.gaspard4i.numismatic.client.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import dev.gaspard4i.numismatic.client.ClientShopState;
import dev.gaspard4i.numismatic.network.NumismaticNetworking;
import dev.gaspard4i.numismatic.shop.OfferList;
import dev.gaspard4i.numismatic.shop.ShopMenuMode;
import dev.gaspard4i.numismatic.shop.ShopOffer;
import dev.gaspard4i.numismatic.shop.ShopPaymentHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

/**
 * Container screen for shop interaction. Renders one of three tabs depending
 * on {@link ClientShopState#getMode()}: OFFERS (owner edit), STOCK (owner
 * stock + revenue), CLIENT (buyer view).
 */
public class ShopScreen extends Screen {

    private static final int BG_W = 320;
    private static final int BG_H = 220;
    private static final int CELL_W = 100;
    private static final int CELL_H = 22;
    private static final int COLS = 3;

    private int leftPos;
    private int topPos;
    private int scroll = 0;
    private int maxScroll = 0;

    public ShopScreen() {
        super(Component.translatable("gui.numismatic_reimagined.shop"));
    }

    @Override
    protected void init() {
        super.init();
        leftPos = (this.width - BG_W) / 2;
        topPos = (this.height - BG_H) / 2;

        // Tabs (only if can edit)
        if (ClientShopState.canEdit()) {
            addRenderableWidget(Button.builder(Component.translatable("gui.numismatic_reimagined.shop.tab.offers"),
                    b -> switchMode(ShopMenuMode.OFFERS))
                    .bounds(leftPos, topPos - 22, 70, 20).build());
            addRenderableWidget(Button.builder(Component.translatable("gui.numismatic_reimagined.shop.tab.stock"),
                    b -> switchMode(ShopMenuMode.STOCK))
                    .bounds(leftPos + 75, topPos - 22, 70, 20).build());
            addRenderableWidget(Button.builder(Component.translatable("gui.numismatic_reimagined.shop.tab.client"),
                    b -> switchMode(ShopMenuMode.CLIENT))
                    .bounds(leftPos + 150, topPos - 22, 70, 20).build());
        }

        ShopMenuMode mode = ClientShopState.getMode();
        if (mode == ShopMenuMode.OFFERS) initOffersTab();
        else if (mode == ShopMenuMode.STOCK) initStockTab();
        else initClientTab();
    }

    private void switchMode(ShopMenuMode mode) {
        if (ClientShopState.getPos() == null) return;
        NumismaticNetworking.sendSwitchShopTab(ClientShopState.getPos(), mode);
    }

    // ---------------- OFFERS tab (owner edit) ----------------

    private void initOffersTab() {
        // "+ New offer" button
        addRenderableWidget(Button.builder(
                Component.translatable("gui.numismatic_reimagined.shop.new_offer"),
                b -> openEditScreen(-1, ItemStack.EMPTY, 1, 1))
                .bounds(leftPos + BG_W - 110, topPos + BG_H - 24, 100, 20).build());

        OfferList offers = ClientShopState.getOffers();
        maxScroll = Math.max(0, ((offers.size() + COLS - 1) / COLS) - getVisibleRows());
    }

    private int getVisibleRows() {
        return (BG_H - 40) / CELL_H;
    }

    // ---------------- STOCK tab (owner stock + revenue) ----------------

    private void initStockTab() {
        long rev = ClientShopState.getRevenue();
        addRenderableWidget(Button.builder(
                Component.translatable("gui.numismatic_reimagined.shop.withdraw_revenue", String.format("%,d", rev)),
                b -> {
                    if (ClientShopState.getPos() != null) {
                        NumismaticNetworking.sendWithdrawRevenue(ClientShopState.getPos());
                    }
                })
                .bounds(leftPos + BG_W - 200, topPos + BG_H - 24, 190, 20)
                .build());
    }

    // ---------------- CLIENT tab (buyer purchase) ----------------

    private void initClientTab() {
        OfferList offers = ClientShopState.getOffers();
        maxScroll = Math.max(0, ((offers.size() + COLS - 1) / COLS) - getVisibleRows());
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        renderBackground(g);

        // Background panel
        g.fill(leftPos, topPos, leftPos + BG_W, topPos + BG_H, 0xC0101010);
        g.fill(leftPos, topPos, leftPos + BG_W, topPos + 16, 0xFF2A2A2A);
        g.drawString(this.font, this.title, leftPos + 6, topPos + 4, 0xFFFFFF, false);

        ShopMenuMode mode = ClientShopState.getMode();
        if (mode == ShopMenuMode.OFFERS) renderOffersTab(g, mouseX, mouseY);
        else if (mode == ShopMenuMode.STOCK) renderStockTab(g, mouseX, mouseY);
        else renderClientTab(g, mouseX, mouseY);

        super.render(g, mouseX, mouseY, partialTick);
    }

    private void renderOffersTab(GuiGraphics g, int mouseX, int mouseY) {
        OfferList offers = ClientShopState.getOffers();
        int gridLeft = leftPos + 8;
        int gridTop = topPos + 22;
        int rows = getVisibleRows();
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < COLS; c++) {
                int idx = (scroll + r) * COLS + c;
                if (idx >= offers.size()) continue;
                ShopOffer offer = offers.get(idx);
                if (offer == null) continue;
                int x = gridLeft + c * (CELL_W + 4);
                int y = gridTop + r * (CELL_H + 2);
                renderOfferCell(g, x, y, offer, idx, mouseX, mouseY, true);
            }
        }
    }

    private void renderStockTab(GuiGraphics g, int mouseX, int mouseY) {
        // Render stock as a 9x3 grid of items (read-only here; full inventory
        // edit happens via the chest-style container if we add it later).
        int gridLeft = leftPos + 8;
        int gridTop = topPos + 22;
        int slot = 0;
        for (int r = 0; r < 3; r++) {
            for (int c = 0; c < 9; c++) {
                int x = gridLeft + c * 18;
                int y = gridTop + r * 18;
                g.fill(x, y, x + 16, y + 16, 0xFF333333);
                ItemStack s = ClientShopState.getStock().get(slot);
                if (!s.isEmpty()) {
                    g.renderItem(s, x, y);
                    g.renderItemDecorations(this.font, s, x, y);
                    if (mouseX >= x && mouseX < x + 16 && mouseY >= y && mouseY < y + 16) {
                        g.renderTooltip(this.font, s, mouseX, mouseY);
                    }
                }
                slot++;
            }
        }
        g.drawString(this.font,
                Component.translatable("gui.numismatic_reimagined.shop.revenue",
                        String.format("%,d", ClientShopState.getRevenue())),
                leftPos + 8, topPos + 22 + 3 * 18 + 8, 0xFFD700, false);
        g.drawString(this.font,
                Component.translatable("gui.numismatic_reimagined.shop.stock_hint"),
                leftPos + 8, topPos + 22 + 3 * 18 + 22, 0xAAAAAA, false);
    }

    private void renderClientTab(GuiGraphics g, int mouseX, int mouseY) {
        OfferList offers = ClientShopState.getOffers();
        int gridLeft = leftPos + 8;
        int gridTop = topPos + 22;
        int rows = getVisibleRows();
        long playerMoney = ShopPaymentHelper.countCoinsAndBags(Minecraft.getInstance().player);
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < COLS; c++) {
                int idx = (scroll + r) * COLS + c;
                if (idx >= offers.size()) continue;
                ShopOffer offer = offers.get(idx);
                if (offer == null) continue;
                int x = gridLeft + c * (CELL_W + 4);
                int y = gridTop + r * (CELL_H + 2);
                renderClientOfferCell(g, x, y, offer, idx, mouseX, mouseY, playerMoney);
            }
        }
    }

    private void renderOfferCell(GuiGraphics g, int x, int y, ShopOffer offer, int idx,
                                 int mouseX, int mouseY, boolean editable) {
        g.fill(x, y, x + CELL_W, y + CELL_H, 0xFF444444);
        ItemStack template = offer.template();
        g.renderItem(template, x + 2, y + 3);
        g.renderItemDecorations(this.font, template, x + 2, y + 3);
        String text = offer.quantityPerPurchase() + "x : " + String.format("%,d", offer.priceBronze());
        g.drawString(this.font, text, x + 22, y + 7, 0xFFFFFF, false);
        if (editable && mouseX >= x && mouseX < x + CELL_W && mouseY >= y && mouseY < y + CELL_H) {
            g.fill(x, y, x + CELL_W, y + CELL_H, 0x40FFFFFF);
            g.renderTooltip(this.font, template, mouseX, mouseY);
        }
    }

    private void renderClientOfferCell(GuiGraphics g, int x, int y, ShopOffer offer, int idx,
                                       int mouseX, int mouseY, long playerMoney) {
        boolean stockOk = ClientShopState.isAdmin() ||
                countMatchingClientStock(offer) >= offer.quantityPerPurchase();
        boolean fundsOk = playerMoney >= offer.priceBronze();
        int bg = (stockOk && fundsOk) ? 0xFF2A4D2A : 0xFF4D2A2A;
        g.fill(x, y, x + CELL_W, y + CELL_H, bg);
        ItemStack template = offer.template();
        g.renderItem(template, x + 2, y + 3);
        g.renderItemDecorations(this.font, template, x + 2, y + 3);
        ChatFormatting priceColor = fundsOk ? ChatFormatting.GREEN : ChatFormatting.RED;
        String text = offer.quantityPerPurchase() + "x : "
                + String.format("%,d", offer.priceBronze());
        g.drawString(this.font, Component.literal(text).withStyle(priceColor),
                x + 22, y + 7, 0xFFFFFF, false);
        if (!stockOk) {
            g.fill(x, y + CELL_H / 2, x + CELL_W, y + CELL_H / 2 + 1, 0xFFFF5555);
        }
        if (mouseX >= x && mouseX < x + CELL_W && mouseY >= y && mouseY < y + CELL_H) {
            g.fill(x, y, x + CELL_W, y + CELL_H, 0x40FFFFFF);
            g.renderTooltip(this.font, template, mouseX, mouseY);
        }
    }

    private int countMatchingClientStock(ShopOffer offer) {
        int total = 0;
        for (ItemStack s : ClientShopState.getStock()) {
            if (s.isEmpty()) continue;
            if (s.is(offer.template().getItem())) total += s.getCount();
        }
        return total;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        ShopMenuMode mode = ClientShopState.getMode();
        OfferList offers = ClientShopState.getOffers();
        int gridLeft = leftPos + 8;
        int gridTop = topPos + 22;
        int rows = getVisibleRows();
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < COLS; c++) {
                int idx = (scroll + r) * COLS + c;
                if (idx >= offers.size()) continue;
                int x = gridLeft + c * (CELL_W + 4);
                int y = gridTop + r * (CELL_H + 2);
                if (mouseX >= x && mouseX < x + CELL_W && mouseY >= y && mouseY < y + CELL_H) {
                    ShopOffer offer = offers.get(idx);
                    if (offer == null) continue;
                    if (mode == ShopMenuMode.OFFERS) {
                        if (button == 0) {
                            openEditScreen(idx, offer.template(), offer.priceBronze(), offer.quantityPerPurchase());
                            return true;
                        } else if (button == 1) {
                            if (ClientShopState.getPos() != null) {
                                NumismaticNetworking.sendRemoveOffer(ClientShopState.getPos(), idx);
                            }
                            return true;
                        }
                    } else if (mode == ShopMenuMode.CLIENT) {
                        if (button == 0 && ClientShopState.getPos() != null) {
                            NumismaticNetworking.sendPurchaseOffer(ClientShopState.getPos(), idx);
                            return true;
                        }
                    }
                }
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    private void openEditScreen(int slotIndex, ItemStack template, long price, int qty) {
        if (Minecraft.getInstance() != null) {
            Minecraft.getInstance().setScreen(new OfferEditScreen(this, slotIndex, template, price, qty));
        }
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        if (delta > 0 && scroll > 0) scroll--;
        else if (delta < 0 && scroll < maxScroll) scroll++;
        return true;
    }

    @Override
    public boolean isPauseScreen() { return false; }

    @Override
    public void onClose() {
        super.onClose();
        ClientShopState.clear();
    }
}
