package dev.gaspard4i.numismatic.forge.client;

import dev.gaspard4i.numismatic.NumismaticConstants;
import dev.gaspard4i.numismatic.client.screen.PurseScreen;
import dev.gaspard4i.numismatic.forge.mixin.AbstractContainerScreenAccessor;
import dev.gaspard4i.numismatic.item.NumismaticItems;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Forge counterpart of {@code FabricPurseInventoryHook}. Adds a purse icon
 * to the vanilla {@link InventoryScreen} via {@link ScreenEvent.Init.Post}.
 */
@Mod.EventBusSubscriber(modid = NumismaticConstants.MOD_ID,
        bus = Mod.EventBusSubscriber.Bus.FORGE,
        value = Dist.CLIENT)
public final class ForgePurseInventoryHook {

    private ForgePurseInventoryHook() {}

    @SubscribeEvent
    public static void onScreenInit(ScreenEvent.Init.Post event) {
        if (!(event.getScreen() instanceof InventoryScreen inv)) return;
        AbstractContainerScreenAccessor acc = (AbstractContainerScreenAccessor) inv;
        int left = acc.numismatic$getLeftPos();
        int top = acc.numismatic$getTopPos();
        event.addListener(new PurseIconButton(left + 152, top + 60, event.getScreen()));
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
