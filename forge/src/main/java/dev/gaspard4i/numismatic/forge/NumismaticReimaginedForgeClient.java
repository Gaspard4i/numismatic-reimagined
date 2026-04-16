package dev.gaspard4i.numismatic.forge;

import dev.gaspard4i.numismatic.NumismaticConstants;
import dev.gaspard4i.numismatic.item.NumismaticItems;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

@Mod.EventBusSubscriber(modid = NumismaticConstants.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class NumismaticReimaginedForgeClient {

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            ResourceLocation coinsId = new ResourceLocation(NumismaticConstants.MOD_ID, "coins");

            ItemProperties.register(NumismaticItems.BRONZE_COIN.get(), coinsId,
                    (stack, level, entity, seed) -> stack.getCount() / 64.0f);
            ItemProperties.register(NumismaticItems.SILVER_COIN.get(), coinsId,
                    (stack, level, entity, seed) -> stack.getCount() / 64.0f);
            ItemProperties.register(NumismaticItems.GOLD_COIN.get(), coinsId,
                    (stack, level, entity, seed) -> stack.getCount() / 64.0f);
            ItemProperties.register(NumismaticItems.NETHERITE_COIN.get(), coinsId,
                    (stack, level, entity, seed) -> stack.getCount() / 64.0f);
            ItemProperties.register(NumismaticItems.STAR_COIN.get(), coinsId,
                    (stack, level, entity, seed) -> stack.getCount() / 64.0f);
        });
    }
}
