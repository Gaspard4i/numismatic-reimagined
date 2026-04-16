package dev.gaspard4i.numismatic.client;

import dev.gaspard4i.numismatic.network.ClientCurrencyData;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;

/**
 * Renders the player's purse balance as a HUD overlay
 * in the top-right corner of the inventory screen.
 */
public final class PurseHudOverlay {

    private PurseHudOverlay() {}

    /**
     * Renders the purse balance overlay when an inventory screen is open.
     * Called from platform-specific render events.
     */
    public static void render(GuiGraphics guiGraphics, float partialTick) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;

        // Only show when an inventory/container screen is open
        if (!(mc.screen instanceof AbstractContainerScreen<?>)) return;

        long balance = ClientCurrencyData.getBalance();
        String text = String.format("%,d coins", balance);

        int screenWidth = mc.getWindow().getGuiScaledWidth();
        int textWidth = mc.font.width(text);

        int x = screenWidth - textWidth - 6;
        int y = 6;

        // Background
        guiGraphics.fill(x - 4, y - 2, x + textWidth + 4, y + 12, 0x80000000);

        // Text
        guiGraphics.drawString(mc.font, text, x, y, 0xFFD700, true);
    }
}
