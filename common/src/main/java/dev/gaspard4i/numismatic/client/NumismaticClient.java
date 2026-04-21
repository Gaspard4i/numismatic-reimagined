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

        // Tooltip component registration is platform-specific :
        //  - fabric: NumismaticFabricClient.onInitializeClient → TooltipComponentCallback
        //  - neoforge: NumismaticNeoForgeClient.onRegisterClientTooltipComponentFactories
    }
}
