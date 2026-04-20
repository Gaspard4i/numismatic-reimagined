package dev.gaspard4i.numismatic.forge;

import com.mojang.blaze3d.platform.InputConstants;
import dev.architectury.registry.menu.MenuRegistry;
import dev.gaspard4i.numismatic.NumismaticConstants;
import dev.gaspard4i.numismatic.client.PurseHudOverlay;
import dev.gaspard4i.numismatic.client.PurseKeybindState;
import dev.gaspard4i.numismatic.client.render.ShopBlockEntityRenderer;
import dev.gaspard4i.numismatic.client.render.ShopTint;
import dev.gaspard4i.numismatic.client.screen.ShopScreen;
import dev.gaspard4i.numismatic.shop.ShopTier;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraftforge.client.event.RegisterColorHandlersEvent;
import dev.gaspard4i.numismatic.client.tooltip.CurrencyTooltipComponent;
import dev.gaspard4i.numismatic.client.tooltip.CurrencyTooltipData;
import net.minecraft.client.KeyMapping;
import net.minecraftforge.client.event.RegisterClientTooltipComponentFactoriesEvent;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.settings.KeyConflictContext;
import dev.gaspard4i.numismatic.item.MoneyBagItem;
import dev.gaspard4i.numismatic.item.NumismaticItems;
import dev.gaspard4i.numismatic.network.ClientCurrencyData;
import dev.gaspard4i.numismatic.shop.ShopRegistry;
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

        // Note: ForgePurseInventoryHook is auto-registered via @Mod.EventBusSubscriber

        event.enqueueWork(() -> {
            ResourceLocation bagTierId = new ResourceLocation(NumismaticConstants.MOD_ID, "bag_tier");
            ItemProperties.register(NumismaticItems.MONEY_BAG.get(), bagTierId,
                    (stack, level, entity, seed) -> MoneyBagItem.getTierFloat(stack));

            MenuRegistry.registerScreenFactory(ShopRegistry.SHOP_MENU.get(), ShopScreen::new);
            BlockEntityRenderers.register(ShopRegistry.SHOP_BLOCK_ENTITY.get(), ShopBlockEntityRenderer::new);
        });
    }

    @SubscribeEvent
    public static void onRegisterClientTooltipFactories(RegisterClientTooltipComponentFactoriesEvent event) {
        event.register(CurrencyTooltipData.class, CurrencyTooltipComponent::new);
    }

    @SubscribeEvent
    public static void onRegisterBlockColors(RegisterColorHandlersEvent.Block event) {
        for (ShopTier tier : ShopTier.values()) {
            event.register((state, level, pos, tintIndex) -> ShopTint.forBlockState(state, tintIndex),
                    ShopRegistry.block(tier).get());
        }
    }

    @SubscribeEvent
    public static void onRegisterItemColors(RegisterColorHandlersEvent.Item event) {
        for (ShopTier tier : ShopTier.values()) {
            event.register((stack, tintIndex) -> ShopTint.forItemStack(stack, tintIndex),
                    ShopRegistry.blockItem(tier).get());
        }
    }

    public static final KeyMapping OPEN_PURSE_KEY = new KeyMapping(
            "key.numismatic_reimagined.open_purse",
            KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYSYM.getOrCreate(InputConstants.KEY_P),
            "key.categories.inventory");

    @SubscribeEvent
    public static void onRegisterKeys(RegisterKeyMappingsEvent event) {
        event.register(OPEN_PURSE_KEY);
    }

    @Mod.EventBusSubscriber(modid = NumismaticConstants.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
    public static class ForgeOverlayEvents {
        @SubscribeEvent
        public static void onRenderOverlay(RenderGuiOverlayEvent.Post event) {
            if (event.getOverlay() == VanillaGuiOverlay.HOTBAR.type()) {
                PurseHudOverlay.render(event.getGuiGraphics(), event.getPartialTick());
            }
        }

        @SubscribeEvent
        public static void onKeyInput(InputEvent.Key event) {
            while (OPEN_PURSE_KEY.consumeClick()) {
                PurseKeybindState.openPurseScreen();
            }
        }
    }
}
