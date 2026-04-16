package dev.gaspard4i.numismatic.fabric;

import dev.gaspard4i.numismatic.NumismaticConstants;
import dev.gaspard4i.numismatic.item.NumismaticItems;
import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.resources.ResourceLocation;

public class NumismaticReimaginedFabricClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
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
    }
}
