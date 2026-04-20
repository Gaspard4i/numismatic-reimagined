package dev.gaspard4i.numismatic.shop;

import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import dev.gaspard4i.numismatic.NumismaticConstants;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;

import java.util.EnumMap;
import java.util.Map;

/**
 * Five-tier shop registry : BRONZE / SILVER / GOLD / NETHERITE / ADMIN,
 * each with its own Block, BlockItem and a shared BlockEntityType.
 *
 * <p>Legacy {@code SHOP_BLOCK} / {@code ADMIN_SHOP_BLOCK} constants remain as
 * aliases so old save data and callers keep working during the transition.
 */
public final class ShopRegistry {

    private static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(
            NumismaticConstants.MOD_ID, Registries.BLOCK);
    private static final DeferredRegister<Item> BLOCK_ITEMS = DeferredRegister.create(
            NumismaticConstants.MOD_ID, Registries.ITEM);
    private static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(
            NumismaticConstants.MOD_ID, Registries.BLOCK_ENTITY_TYPE);
    private static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(
            NumismaticConstants.MOD_ID, Registries.MENU);

    public static final RegistrySupplier<MenuType<ShopMenu>> SHOP_MENU =
            MENUS.register("shop_menu",
                    () -> new MenuType<>(ShopMenu::new, FeatureFlags.VANILLA_SET));

    // Per-tier blocks and block items (EnumMap for compile-time safety).
    private static final Map<ShopTier, RegistrySupplier<Block>> TIER_BLOCKS = new EnumMap<>(ShopTier.class);
    private static final Map<ShopTier, RegistrySupplier<Item>> TIER_BLOCK_ITEMS = new EnumMap<>(ShopTier.class);

    static {
        for (ShopTier tier : ShopTier.values()) {
            BlockBehaviour.Properties props = tier == ShopTier.ADMIN
                    ? BlockBehaviour.Properties.of()
                            .mapColor(MapColor.GOLD)
                            .strength(-1.0f, 3600000.0f)
                            .sound(SoundType.METAL)
                            .noOcclusion()
                            .pushReaction(PushReaction.BLOCK)
                    : BlockBehaviour.Properties.of()
                            .mapColor(MapColor.WOOD)
                            .strength(2.5f, 6.0f)
                            .sound(SoundType.WOOD)
                            .noOcclusion()
                            .pushReaction(PushReaction.BLOCK);

            RegistrySupplier<Block> block = BLOCKS.register(tier.blockId(),
                    () -> new ShopBlock(props, tier));
            TIER_BLOCKS.put(tier, block);

            Item.Properties itemProps = tier == ShopTier.ADMIN
                    ? new Item.Properties().rarity(Rarity.EPIC)
                    : new Item.Properties();
            RegistrySupplier<Item> blockItem = BLOCK_ITEMS.register(tier.blockId(),
                    () -> new BlockItem(block.get(), itemProps));
            TIER_BLOCK_ITEMS.put(tier, blockItem);
        }
    }

    public static RegistrySupplier<Block> block(ShopTier tier) { return TIER_BLOCKS.get(tier); }
    public static RegistrySupplier<Item> blockItem(ShopTier tier) { return TIER_BLOCK_ITEMS.get(tier); }

    /** Backwards-compatible aliases pointing at the GOLD / ADMIN tiers. */
    public static final RegistrySupplier<Block> SHOP_BLOCK = TIER_BLOCKS.get(ShopTier.GOLD);
    public static final RegistrySupplier<Item> SHOP_BLOCK_ITEM = TIER_BLOCK_ITEMS.get(ShopTier.GOLD);
    public static final RegistrySupplier<Block> ADMIN_SHOP_BLOCK = TIER_BLOCKS.get(ShopTier.ADMIN);
    public static final RegistrySupplier<Item> ADMIN_SHOP_BLOCK_ITEM = TIER_BLOCK_ITEMS.get(ShopTier.ADMIN);

    // Single BlockEntityType covering all five blocks. MC accepts Block[]
    // via the Builder#of varargs.
    @SuppressWarnings("ConstantConditions")
    public static final RegistrySupplier<BlockEntityType<ShopBlockEntity>> SHOP_BLOCK_ENTITY =
            BLOCK_ENTITIES.register("shop_block",
                    () -> BlockEntityType.Builder.of(
                            (pos, state) -> new ShopBlockEntity(ShopRegistry.SHOP_BLOCK_ENTITY.get(), pos, state),
                            TIER_BLOCKS.get(ShopTier.BRONZE).get(),
                            TIER_BLOCKS.get(ShopTier.SILVER).get(),
                            TIER_BLOCKS.get(ShopTier.GOLD).get(),
                            TIER_BLOCKS.get(ShopTier.NETHERITE).get(),
                            TIER_BLOCKS.get(ShopTier.ADMIN).get()
                    ).build(null));

    /** Alias — legacy admin code referenced this separately. */
    public static final RegistrySupplier<BlockEntityType<ShopBlockEntity>> ADMIN_SHOP_BLOCK_ENTITY = SHOP_BLOCK_ENTITY;

    public static void register() {
        BLOCKS.register();
        BLOCK_ITEMS.register();
        BLOCK_ENTITIES.register();
        MENUS.register();
    }

    private ShopRegistry() {}
}
