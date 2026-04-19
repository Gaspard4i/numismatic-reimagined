package dev.gaspard4i.numismatic.forge;

import dev.architectury.registry.menu.MenuRegistry;
import dev.gaspard4i.numismatic.NumismaticConstants;
import dev.gaspard4i.numismatic.client.PurseHudOverlay;
import dev.gaspard4i.numismatic.client.screen.ShopScreen;
import dev.gaspard4i.numismatic.item.MoneyBagItem;
import dev.gaspard4i.numismatic.item.NumismaticItems;
import dev.gaspard4i.numismatic.network.ClientCurrencyData;
import dev.gaspard4i.numismatic.shop.NumismaticShop;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

@Mod.EventBusSubscriber(modid = NumismaticConstants.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class NumismaticReimaginedForgeClient {

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        // Register client-side packet receivers
        ClientCurrencyData.registerClientReceivers();

        event.enqueueWork(() -> {
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

            // Money bag tier predicate
            ResourceLocation bagTierId = new ResourceLocation(NumismaticConstants.MOD_ID, "bag_tier");
            ItemProperties.register(NumismaticItems.MONEY_BAG.get(), bagTierId,
                    (stack, level, entity, seed) -> MoneyBagItem.getTierFloat(stack));

            MenuRegistry.registerScreenFactory(NumismaticShop.SHOP_MENU.get(), ShopScreen::new);
        });
    }

    @Mod.EventBusSubscriber(modid = NumismaticConstants.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
    public static class ForgeOverlayEvents {
        @SubscribeEvent
        public static void onRenderOverlay(RenderGuiOverlayEvent.Post event) {
            if (event.getOverlay() == VanillaGuiOverlay.HOTBAR.type()) {
                PurseHudOverlay.render(event.getGuiGraphics(), event.getPartialTick());
            }
        }
    }
}
