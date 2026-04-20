package dev.gaspard4i.numismatic.fabric;

import dev.architectury.registry.menu.MenuRegistry;
import dev.gaspard4i.numismatic.NumismaticConstants;
import dev.gaspard4i.numismatic.client.PurseHudOverlay;
import dev.gaspard4i.numismatic.client.screen.ShopScreen;
import dev.gaspard4i.numismatic.client.tooltip.CurrencyTooltipComponent;
import dev.gaspard4i.numismatic.client.tooltip.CurrencyTooltipData;
import net.fabricmc.fabric.api.client.rendering.v1.TooltipComponentCallback;
import dev.gaspard4i.numismatic.fabric.client.FabricPurseInventoryHook;
import dev.gaspard4i.numismatic.item.MoneyBagItem;
import dev.gaspard4i.numismatic.item.NumismaticItems;
import dev.gaspard4i.numismatic.network.ClientCurrencyData;
import dev.gaspard4i.numismatic.shop.ShopRegistry;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.resources.ResourceLocation;

public class NumismaticReimaginedFabricClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        // Register client-side packet receivers
        ClientCurrencyData.registerClientReceivers();

        // Register purse icon on the vanilla inventory screen
        FabricPurseInventoryHook.register();

        // Register shop screen factory for the SHOP_MENU type
        MenuRegistry.registerScreenFactory(ShopRegistry.SHOP_MENU.get(), ShopScreen::new);

        // Custom tooltip: coin/money-bag show icon+count stack
        TooltipComponentCallback.EVENT.register(data ->
                data instanceof CurrencyTooltipData d ? new CurrencyTooltipComponent(d) : null);

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
