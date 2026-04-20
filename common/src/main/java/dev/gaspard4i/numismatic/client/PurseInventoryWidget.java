package dev.gaspard4i.numismatic.client;

import dev.gaspard4i.numismatic.client.screen.PurseScreen;
import dev.gaspard4i.numismatic.item.NumismaticItems;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

/**
 * Small money-bag icon button placed on the vanilla inventory screen.
 * Clicking opens {@link PurseScreen}.
 */
public class PurseInventoryWidget extends Button {

    public static final int WIDGET_W = 16;
    public static final int WIDGET_H = 16;

    private static final ItemStack ICON = new ItemStack(NumismaticItems.MONEY_BAG.get());

    public PurseInventoryWidget(int x, int y) {
        super(x, y, WIDGET_W, WIDGET_H,
                Component.translatable("gui.numismatic_reimagined.purse"),
                btn -> {
                    Minecraft mc = Minecraft.getInstance();
                    mc.setScreen(new PurseScreen(mc.screen));
                },
                DEFAULT_NARRATION);
    }

    @Override
    public void renderWidget(GuiGraphics g, int mouseX, int mouseY, float partial) {
        g.renderFakeItem(ICON, getX(), getY());
        if (isHoveredOrFocused()) {
            // Soft white overlay.
            g.fill(getX(), getY(), getX() + width, getY() + height, 0x30FFFFFF);
        }
    }
}
