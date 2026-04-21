package dev.gaspard4i.numismatic.client;

import dev.architectury.event.events.client.ClientGuiEvent;
import dev.gaspard4i.numismatic.NumismaticConstants;
import dev.gaspard4i.numismatic.item.NumismaticItems;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.resources.ResourceLocation;

public final class NumismaticClient {

    private NumismaticClient() {}

    public static void init() {
        ClientGuiEvent.INIT_POST.register((screen, access) -> {
            if (!(screen instanceof InventoryScreen inventoryScreen)) return;
            PurseScreenHook.attach(inventoryScreen, access);
        });

        // Dynamic money bag texture based on stored value (empty / silver / gold / netherite tiers).
        // The model predicate `numismatic_reimagined:bag_tier` is referenced by
        // assets/numismatic_reimagined/models/item/money_bag.json overrides.
        ItemProperties.register(
                NumismaticItems.MONEY_BAG.get(),
                ResourceLocation.fromNamespaceAndPath(NumismaticConstants.MOD_ID, "bag_tier"),
                (stack, level, entity, seed) -> MoneyBagPredicates.tierFor(stack)
        );

        // Coin stack size predicate (upstream : count / 100.0, thresholds 0.09/0.27/0.45/0.63).
        // Drives the 4 coin pile texture variants (*_0, _1, _2, _3) per denomination.
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

        // Tooltip component registration is platform-specific :
        //  - fabric: NumismaticFabricClient.onInitializeClient → TooltipComponentCallback
        //  - neoforge: NumismaticNeoForgeClient.onRegisterClientTooltipComponentFactories
    }
}
