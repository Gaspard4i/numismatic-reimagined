package dev.gaspard4i.numismatic.fabric;

import dev.gaspard4i.numismatic.NumismaticConstants;
import dev.gaspard4i.numismatic.client.PurseHudOverlay;
import dev.gaspard4i.numismatic.item.MoneyBagItem;
import dev.gaspard4i.numismatic.item.NumismaticItems;
import dev.gaspard4i.numismatic.network.ClientCurrencyData;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.resources.ResourceLocation;

public class NumismaticReimaginedFabricClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        // Register client-side packet receivers
        ClientCurrencyData.registerClientReceivers();

        // Register model predicate for money bag tiers
        ResourceLocation bagTierId = new ResourceLocation(NumismaticConstants.MOD_ID, "bag_tier");
        ItemProperties.register(NumismaticItems.MONEY_BAG.get(), bagTierId,
                (stack, level, entity, seed) -> MoneyBagItem.getTierFloat(stack));

        // Register purse HUD overlay
        HudRenderCallback.EVENT.register((guiGraphics, tickDelta) ->
                PurseHudOverlay.render(guiGraphics, tickDelta)
        );

    }
}
