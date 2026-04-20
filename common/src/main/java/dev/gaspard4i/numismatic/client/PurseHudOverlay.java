package dev.gaspard4i.numismatic.client;

import dev.gaspard4i.numismatic.item.NumismaticItems;
import dev.gaspard4i.numismatic.network.ClientCurrencyData;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.item.ItemStack;

/**
 * HUD overlay that shows the purse balance as a money-bag icon + formatted
 * amount in the top-right corner whenever a container screen is open.
 */
public final class PurseHudOverlay {

    private static final int PADDING = 6;
    private static final int ICON_SIZE = 16;
    private static final ItemStack ICON = new ItemStack(NumismaticItems.MONEY_BAG.get());

    private PurseHudOverlay() {}

    public static void render(GuiGraphics guiGraphics, float partialTick) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;
        if (!(mc.screen instanceof AbstractContainerScreen<?>)) return;

        long balance = ClientCurrencyData.getBalance();
        String text = String.format("%,d", balance);

        int screenWidth = mc.getWindow().getGuiScaledWidth();
        int textWidth = mc.font.width(text);
        int boxW = ICON_SIZE + 4 + textWidth + 8;

        int x = screenWidth - boxW - PADDING;
        int y = PADDING;

        guiGraphics.fill(x, y, x + boxW, y + ICON_SIZE + 2, 0x80000000);
        guiGraphics.renderFakeItem(ICON, x + 2, y - 1);
        guiGraphics.drawString(mc.font, text,
                x + ICON_SIZE + 4, y + 4, 0xFFD700, true);
    }
}
