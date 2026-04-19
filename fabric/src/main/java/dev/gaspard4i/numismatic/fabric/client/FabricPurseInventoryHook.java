package dev.gaspard4i.numismatic.fabric.client;

import dev.gaspard4i.numismatic.client.screen.PurseScreen;
import dev.gaspard4i.numismatic.fabric.mixin.AbstractContainerScreenAccessor;
import dev.gaspard4i.numismatic.item.NumismaticItems;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.Screens;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

/**
 * Adds a clickable purse icon to the vanilla {@link InventoryScreen}.
 * Implementation lives in the fabric module so we can use the Fabric
 * Screen API ({@link ScreenEvents#AFTER_INIT}) to add the button without
 * a mixin and access the screen's protected {@code leftPos} / {@code topPos}
 * via {@link AbstractContainerScreen#getGuiLeft()} which is visible here
 * because Loom resolves Mojang mappings at compile time for the Fabric
 * module.
 */
public final class FabricPurseInventoryHook {

    private FabricPurseInventoryHook() {}

    public static void register() {
        ScreenEvents.AFTER_INIT.register((client, screen, scaledWidth, scaledHeight) -> {
            if (!(screen instanceof InventoryScreen inv)) return;
            AbstractContainerScreenAccessor acc = (AbstractContainerScreenAccessor) inv;
            int left = acc.numismatic$getLeftPos();
            int top = acc.numismatic$getTopPos();
            Screens.getButtons(inv).add(new PurseIconButton(left + 152, top + 60, screen));
        });
    }

    private static final class PurseIconButton extends Button {
        private static final ItemStack ICON = new ItemStack(NumismaticItems.MONEY_BAG.get());

        PurseIconButton(int x, int y, net.minecraft.client.gui.screens.Screen parent) {
            super(x, y, 18, 18,
                    Component.translatable("gui.numismatic_reimagined.purse"),
                    b -> Minecraft.getInstance().setScreen(new PurseScreen(parent)),
                    DEFAULT_NARRATION);
        }

        @Override
        public void renderWidget(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
            g.fill(getX(), getY(), getX() + width, getY() + height, 0xFF000000);
            g.fill(getX() + 1, getY() + 1, getX() + width - 1, getY() + height - 1,
                    isHovered() ? 0xFF555555 : 0xFF222222);
            g.renderItem(ICON, getX() + 1, getY() + 1);
        }
    }
}
