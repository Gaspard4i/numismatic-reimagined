package dev.gaspard4i.numismatic.shop;

import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import dev.gaspard4i.numismatic.NumismaticConstants;
import dev.gaspard4i.numismatic.item.NumismaticItems;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;

import java.util.EnumMap;
import java.util.Map;

public final class NumismaticShops {

    private NumismaticShops() {}

    public static final DeferredRegister<Block> BLOCKS =
            DeferredRegister.create(NumismaticConstants.MOD_ID, Registries.BLOCK);

    private static final Map<ShopTier, RegistrySupplier<Block>> SHOPS = new EnumMap<>(ShopTier.class);

    static {
        for (ShopTier tier : ShopTier.values()) {
            RegistrySupplier<Block> block = BLOCKS.register(
                    tier.id(),
                    () -> new ShopBlock(
                            BlockBehaviour.Properties.ofFullCopy(Blocks.CHEST).strength(2.5f, 3.0f).noOcclusion(),
                            tier)
            );
            SHOPS.put(tier, block);
            NumismaticItems.ITEMS.register(tier.id(), () -> new BlockItem(block.get(), new Item.Properties()));
        }
    }

    public static Block blockFor(ShopTier tier) {
        return SHOPS.get(tier).get();
    }

    public static void register() {
        BLOCKS.register();
    }
}
