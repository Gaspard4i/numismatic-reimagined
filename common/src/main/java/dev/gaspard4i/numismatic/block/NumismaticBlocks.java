package dev.gaspard4i.numismatic.block;

import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import dev.gaspard4i.numismatic.NumismaticConstants;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;

public final class NumismaticBlocks {

    private static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(
            NumismaticConstants.MOD_ID, Registries.BLOCK
    );

    private static final DeferredRegister<Item> BLOCK_ITEMS = DeferredRegister.create(
            NumismaticConstants.MOD_ID, Registries.ITEM
    );

    private static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(
            NumismaticConstants.MOD_ID, Registries.BLOCK_ENTITY_TYPE
    );

    // --- Piggy Bank (terracotta-like, fragile, breakable by hand quickly) ---

    public static final RegistrySupplier<Block> PIGGY_BANK = BLOCKS.register("piggy_bank",
            () -> new PiggyBankBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_PINK)
                    .instabreak()
                    .sound(SoundType.DECORATED_POT)
                    .noOcclusion()
            )
    );

    public static final RegistrySupplier<Item> PIGGY_BANK_ITEM = BLOCK_ITEMS.register("piggy_bank",
            () -> new PiggyBankBlockItem(PIGGY_BANK.get(), new Item.Properties())
    );

    @SuppressWarnings("ConstantConditions")
    public static final RegistrySupplier<BlockEntityType<PiggyBankBlockEntity>> PIGGY_BANK_BLOCK_ENTITY =
            BLOCK_ENTITIES.register("piggy_bank",
                    () -> BlockEntityType.Builder.of(PiggyBankBlockEntity::new, PIGGY_BANK.get()).build(null)
            );

    // --- Golden Piggy Bank (gold block hardness: 3.0, resistance: 6.0, needs pickaxe) ---

    public static final RegistrySupplier<Block> GOLDEN_PIGGY_BANK = BLOCKS.register("golden_piggy_bank",
            () -> new GoldenPiggyBankBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.GOLD)
                    .strength(3.0f, 6.0f)
                    .sound(SoundType.METAL)
                    .noOcclusion()
            )
    );

    public static final RegistrySupplier<Item> GOLDEN_PIGGY_BANK_ITEM = BLOCK_ITEMS.register("golden_piggy_bank",
            () -> new PiggyBankBlockItem(GOLDEN_PIGGY_BANK.get(), new Item.Properties().rarity(Rarity.RARE))
    );

    @SuppressWarnings("ConstantConditions")
    public static final RegistrySupplier<BlockEntityType<GoldenPiggyBankBlockEntity>> GOLDEN_PIGGY_BANK_BLOCK_ENTITY =
            BLOCK_ENTITIES.register("golden_piggy_bank",
                    () -> BlockEntityType.Builder.of(GoldenPiggyBankBlockEntity::new, GOLDEN_PIGGY_BANK.get()).build(null)
            );

    // --- Netherite Piggy Bank (netherite block hardness: 50.0, resistance: 1200.0, needs pickaxe) ---

    public static final RegistrySupplier<Block> NETHERITE_PIGGY_BANK = BLOCKS.register("netherite_piggy_bank",
            () -> new NetheritePiggyBankBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_BLACK)
                    .strength(50.0f, 1200.0f)
                    .sound(SoundType.NETHERITE_BLOCK)
                    .noOcclusion()
            )
    );

    public static final RegistrySupplier<Item> NETHERITE_PIGGY_BANK_ITEM = BLOCK_ITEMS.register("netherite_piggy_bank",
            () -> new PiggyBankBlockItem(NETHERITE_PIGGY_BANK.get(), new Item.Properties().rarity(Rarity.EPIC).fireResistant())
    );

    @SuppressWarnings("ConstantConditions")
    public static final RegistrySupplier<BlockEntityType<NetheritePiggyBankBlockEntity>> NETHERITE_PIGGY_BANK_BLOCK_ENTITY =
            BLOCK_ENTITIES.register("netherite_piggy_bank",
                    () -> BlockEntityType.Builder.of(NetheritePiggyBankBlockEntity::new, NETHERITE_PIGGY_BANK.get()).build(null)
            );

    public static void register() {
        BLOCKS.register();
        BLOCK_ITEMS.register();
        BLOCK_ENTITIES.register();
    }

    private NumismaticBlocks() {}
}
