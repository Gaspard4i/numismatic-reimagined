package dev.gaspard4i.numismatic.client;

import dev.architectury.event.EventResult;
import dev.architectury.event.events.client.ClientGuiEvent;
import dev.architectury.event.events.client.ClientScreenInputEvent;
import dev.gaspard4i.numismatic.NumismaticConstants;
import dev.gaspard4i.numismatic.item.NumismaticItems;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.gui.screens.inventory.MerchantScreen;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.resources.ResourceLocation;

public final class NumismaticClient {

    private NumismaticClient() {}

    public static void init() {
        ClientGuiEvent.INIT_POST.register((screen, access) -> {
            if (screen instanceof InventoryScreen
                    || screen instanceof CreativeModeInventoryScreen
                    || screen instanceof MerchantScreen) {
                PurseScreenHook.attach(screen, access);
            }
        });

        // Upstream parity : hide the purse button on creative tabs other than "Inventory".
        ClientGuiEvent.RENDER_POST.register((screen, graphics, mouseX, mouseY, tickDelta) -> {
            if (screen instanceof CreativeModeInventoryScreen creative) {
                PurseScreenHook.updateCreativeVisibility(creative);
            }
        });

        // Block clicks from reaching inventory slots behind the purse popup.
        ClientScreenInputEvent.MOUSE_CLICKED_PRE.register((mc, screen, mx, my, button) ->
                PurseScreenHook.shouldConsumeClick(screen, mx, my) ? EventResult.interruptFalse() : EventResult.pass());
        ClientScreenInputEvent.MOUSE_RELEASED_PRE.register((mc, screen, mx, my, button) ->
                PurseScreenHook.shouldConsumeClick(screen, mx, my) ? EventResult.interruptFalse() : EventResult.pass());

        // Money bag tier predicate : empty / silver / gold / netherite skins.
        ItemProperties.register(
                NumismaticItems.MONEY_BAG.get(),
                ResourceLocation.fromNamespaceAndPath(NumismaticConstants.MOD_ID, "bag_tier"),
                (stack, level, entity, seed) -> MoneyBagPredicates.tierFor(stack)
        );

        // Coin stack size predicate : drives the 4 coin-pile variants per denomination.
        ResourceLocation coinsPredicate =
                ResourceLocation.fromNamespaceAndPath(NumismaticConstants.MOD_ID, "coins");
        ItemProperties.register(NumismaticItems.BRONZE_COIN.get(), coinsPredicate,
                (stack, level, entity, seed) -> stack.getCount() / 100.0f);
        ItemProperties.register(NumismaticItems.SILVER_COIN.get(), coinsPredicate,
                (stack, level, entity, seed) -> stack.getCount() / 100.0f);
        ItemProperties.register(NumismaticItems.GOLD_COIN.get(), coinsPredicate,
                (stack, level, entity, seed) -> stack.getCount() / 100.0f);
        ItemProperties.register(NumismaticItems.NETHERITE_COIN.get(), coinsPredicate,
                (stack, level, entity, seed) -> stack.getCount() / 100.0f);
    }
}
