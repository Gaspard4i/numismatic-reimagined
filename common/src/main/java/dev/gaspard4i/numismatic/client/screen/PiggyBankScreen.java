package dev.gaspard4i.numismatic.client.screen;

import dev.gaspard4i.numismatic.NumismaticConstants;
import dev.gaspard4i.numismatic.block.PiggyBankMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

/**
 * Screen rendered on top of the {@link PiggyBankMenu}.
 * Uses the legacy 1.20.1 piggy_bank.png GUI texture, layout mirrors upstream :
 * 3 validating coin slots at (62, 26), (80, 26), (98, 26) plus player inventory at (8, 63).
 */
public class PiggyBankScreen extends AbstractContainerScreen<PiggyBankMenu> {

    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(NumismaticConstants.MOD_ID, "textures/gui/piggy_bank.png");

    public PiggyBankScreen(PiggyBankMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageWidth = 176;
        this.imageHeight = 166;
        this.inventoryLabelY = this.imageHeight - 94;
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        int x = (this.width - this.imageWidth) / 2;
        int y = (this.height - this.imageHeight) / 2;
        graphics.blit(TEXTURE, x, y, 0, 0, this.imageWidth, this.imageHeight);
    }
}
