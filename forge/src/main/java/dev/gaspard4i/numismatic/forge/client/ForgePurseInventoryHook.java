package dev.gaspard4i.numismatic.forge.client;

import dev.gaspard4i.numismatic.NumismaticConstants;
import dev.gaspard4i.numismatic.client.PurseInventoryWidget;
import dev.gaspard4i.numismatic.forge.mixin.AbstractContainerScreenAccessor;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.WeakHashMap;

/**
 * Forge counterpart of {@code FabricPurseInventoryHook}: attach a
 * {@link PurseInventoryWidget} to the inventory screen and intercept
 * mouse clicks so the widget's popup buttons get priority over vanilla
 * slots.
 */
@Mod.EventBusSubscriber(modid = NumismaticConstants.MOD_ID,
        bus = Mod.EventBusSubscriber.Bus.FORGE,
        value = Dist.CLIENT)
public final class ForgePurseInventoryHook {

    private ForgePurseInventoryHook() {}

    /** Per-screen widget instance so we can route click events back to it. */
    private static final WeakHashMap<InventoryScreen, PurseInventoryWidget> WIDGETS =
            new WeakHashMap<>();

    @SubscribeEvent
    public static void onScreenInit(ScreenEvent.Init.Post event) {
        if (!(event.getScreen() instanceof InventoryScreen inv)) return;
        AbstractContainerScreenAccessor acc = (AbstractContainerScreenAccessor) inv;
        int left = acc.numismatic$getLeftPos();
        int top = acc.numismatic$getTopPos();

        PurseInventoryWidget widget = new PurseInventoryWidget(left + 152, top + 6);
        event.addListener(widget);
        WIDGETS.put(inv, widget);
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void onMousePre(ScreenEvent.MouseButtonPressed.Pre event) {
        if (!(event.getScreen() instanceof InventoryScreen inv)) return;
        PurseInventoryWidget widget = WIDGETS.get(inv);
        if (widget == null) return;
        if (widget.onInventoryClick(event.getMouseX(), event.getMouseY(), event.getButton())) {
            event.setCanceled(true);
        }
    }
}
