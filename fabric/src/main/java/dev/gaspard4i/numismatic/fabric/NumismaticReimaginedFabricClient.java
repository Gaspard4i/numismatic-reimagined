package dev.gaspard4i.numismatic.fabric;

import dev.architectury.registry.menu.MenuRegistry;
import dev.gaspard4i.numismatic.NumismaticConstants;
import dev.gaspard4i.numismatic.client.PurseHudOverlay;
import dev.gaspard4i.numismatic.client.screen.ShopScreen;
import dev.gaspard4i.numismatic.item.MoneyBagItem;
import dev.gaspard4i.numismatic.item.NumismaticItems;
import dev.gaspard4i.numismatic.network.ClientCurrencyData;
import dev.gaspard4i.numismatic.shop.NumismaticShop;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.resources.ResourceLocation;

public class NumismaticReimaginedFabricClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        // Register client-side packet receivers
        ClientCurrencyData.registerClientReceivers();

        // Register model predicates for stack-count textures
        ResourceLocation coinsId = new ResourceLocation(NumismaticConstants.MOD_ID, "coins");

        ItemProperties.register(NumismaticItems.BRONZE_COIN.get(), coinsId,
                (stack, level, entity, seed) -> stack.getCount() / 99.0f);
        ItemProperties.register(NumismaticItems.SILVER_COIN.get(), coinsId,
                (stack, level, entity, seed) -> stack.getCount() / 99.0f);
        ItemProperties.register(NumismaticItems.GOLD_COIN.get(), coinsId,
                (stack, level, entity, seed) -> stack.getCount() / 99.0f);
        ItemProperties.register(NumismaticItems.NETHERITE_COIN.get(), coinsId,
                (stack, level, entity, seed) -> stack.getCount() / 99.0f);
        ItemProperties.register(NumismaticItems.STAR_COIN.get(), coinsId,
                (stack, level, entity, seed) -> stack.getCount() / 99.0f);

        // Register model predicate for money bag tiers
        ResourceLocation bagTierId = new ResourceLocation(NumismaticConstants.MOD_ID, "bag_tier");
        ItemProperties.register(NumismaticItems.MONEY_BAG.get(), bagTierId,
                (stack, level, entity, seed) -> MoneyBagItem.getTierFloat(stack));

        // Register the shop screen factory for the shop menu type.
        MenuRegistry.registerScreenFactory(NumismaticShop.SHOP_MENU.get(), ShopScreen::new);

        // Register purse HUD overlay
        HudRenderCallback.EVENT.register((guiGraphics, tickDelta) ->
                PurseHudOverlay.render(guiGraphics, tickDelta)
        );

    }
}
