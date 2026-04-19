package dev.gaspard4i.numismatic.client.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import dev.gaspard4i.numismatic.client.ClientShopState;
import dev.gaspard4i.numismatic.network.NumismaticNetworking;
import dev.gaspard4i.numismatic.shop.ShopMenuMode;
import dev.gaspard4i.numismatic.shop.ShopStockMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

/**
 * Container screen for the shop's STOCK tab. Backed by {@link ShopStockMenu}
 * so drag/drop with the player inventory works natively. Adds tab buttons
 * (Offers / Stock / Client) and a "Withdraw Revenue" button at the bottom.
 */
public class ShopStockScreen extends AbstractContainerScreen<ShopStockMenu> {

    private static final ResourceLocation BG = new ResourceLocation("textures/gui/container/generic_54.png");

    public ShopStockScreen(ShopStockMenu menu, Inventory inv, Component title) {
        super(menu, inv, title);
        this.imageWidth = 176;
        this.imageHeight = 168; // 3 rows * 18 + 30 header + inventory
    }

    @Override
    protected void init() {
        super.init();
        // Tab buttons (top)
        if (ClientShopState.canEdit()) {
            addRenderableWidget(Button.builder(Component.translatable("gui.numismatic_reimagined.shop.tab.offers"),
                    b -> switchMode(ShopMenuMode.OFFERS))
                    .bounds(this.leftPos, this.topPos - 22, 50, 20).build());
            addRenderableWidget(Button.builder(Component.translatable("gui.numismatic_reimagined.shop.tab.stock"),
                    b -> {})
                    .bounds(this.leftPos + 55, this.topPos - 22, 50, 20).build());
            addRenderableWidget(Button.builder(Component.translatable("gui.numismatic_reimagined.shop.tab.client"),
                    b -> switchMode(ShopMenuMode.CLIENT))
                    .bounds(this.leftPos + 110, this.topPos - 22, 50, 20).build());
        }
        // Withdraw revenue button
        long rev = ClientShopState.getRevenue();
        addRenderableWidget(Button.builder(
                Component.translatable("gui.numismatic_reimagined.shop.withdraw_revenue", String.format("%,d", rev)),
                b -> {
                    if (ClientShopState.getPos() != null) {
                        NumismaticNetworking.sendWithdrawRevenue(ClientShopState.getPos());
                    }
                })
                .bounds(this.leftPos, this.topPos + this.imageHeight + 4, this.imageWidth, 20).build());
    }

    private void switchMode(ShopMenuMode mode) {
        if (ClientShopState.getPos() == null) return;
        NumismaticNetworking.sendSwitchShopTab(ClientShopState.getPos(), mode);
    }

    @Override
    protected void renderBg(GuiGraphics g, float partialTick, int mouseX, int mouseY) {
        RenderSystem.setShaderColor(1, 1, 1, 1);
        // Use vanilla generic_54 (large chest) texture, draw the upper portion (3 rows + player inv)
        g.blit(BG, this.leftPos, this.topPos, 0, 0, this.imageWidth, 71);
        g.blit(BG, this.leftPos, this.topPos + 71, 0, 126, this.imageWidth, 96);
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        super.render(g, mouseX, mouseY, partialTick);
        renderTooltip(g, mouseX, mouseY);
    }

    @Override
    protected void renderLabels(GuiGraphics g, int mouseX, int mouseY) {
        // Override to suppress vanilla title for cleaner look
        g.drawString(this.font, this.title, 8, 6, 0x404040, false);
        g.drawString(this.font, this.playerInventoryTitle, 8, this.imageHeight - 94, 0x404040, false);
    }

    @Override
    public boolean isPauseScreen() { return false; }
}
