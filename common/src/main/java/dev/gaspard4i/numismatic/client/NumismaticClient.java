package dev.gaspard4i.numismatic.client;

import dev.architectury.event.events.client.ClientGuiEvent;
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
