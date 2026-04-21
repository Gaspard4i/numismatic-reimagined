package dev.gaspard4i.numismatic.block;

import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import dev.gaspard4i.numismatic.NumismaticConstants;
import dev.gaspard4i.numismatic.item.NumismaticItems;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;

import java.util.EnumMap;
import java.util.Map;

public final class PiggyBankBlocks {

    private PiggyBankBlocks() {}

    public static final DeferredRegister<Block> BLOCKS =
            DeferredRegister.create(NumismaticConstants.MOD_ID, Registries.BLOCK);
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(NumismaticConstants.MOD_ID, Registries.BLOCK_ENTITY_TYPE);

    private static final Map<PiggyBankTier, RegistrySupplier<Block>> BLOCKS_BY_TIER = new EnumMap<>(PiggyBankTier.class);
    private static final Map<PiggyBankTier, RegistrySupplier<BlockEntityType<PiggyBankBlockEntity>>> BES_BY_TIER =
            new EnumMap<>(PiggyBankTier.class);

    static {
        for (PiggyBankTier tier : PiggyBankTier.values()) {
            RegistrySupplier<Block> block = BLOCKS.register(
                    tier.id(),
                    () -> new PiggyBankBlock(
                            BlockBehaviour.Properties.of()
                                    .mapColor(MapColor.COLOR_PINK)
                                    .strength(1.5f, 1.0f),
                            tier)
            );
            BLOCKS_BY_TIER.put(tier, block);

            NumismaticItems.ITEMS.register(tier.id(),
                    () -> new PiggyBankBlockItem(block.get(), new Item.Properties(), tier));

            RegistrySupplier<BlockEntityType<PiggyBankBlockEntity>> beType = BLOCK_ENTITIES.register(
                    tier.id(),
                    () -> BlockEntityType.Builder.of(
                            (pos, state) -> new PiggyBankBlockEntity(BES_BY_TIER.get(tier).get(), pos, state, tier),
                            block.get()
                    ).build(null)
            );
            BES_BY_TIER.put(tier, beType);
        }
    }

    public static BlockEntityType<PiggyBankBlockEntity> blockEntityTypeFor(PiggyBankTier tier) {
        return BES_BY_TIER.get(tier).get();
    }

    public static Block blockFor(PiggyBankTier tier) {
        return BLOCKS_BY_TIER.get(tier).get();
    }

    public static void register() {
        BLOCKS.register();
        BLOCK_ENTITIES.register();
    }
}
