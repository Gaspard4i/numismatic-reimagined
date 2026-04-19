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

/**
 * Registers shop blocks, their item forms, and their block entity types.
 * Called from the main mod entrypoint.
 */
public final class NumismaticShop {

    private static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(
            NumismaticConstants.MOD_ID, Registries.BLOCK
    );

    private static final DeferredRegister<Item> BLOCK_ITEMS = DeferredRegister.create(
            NumismaticConstants.MOD_ID, Registries.ITEM
    );

    private static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(
            NumismaticConstants.MOD_ID, Registries.BLOCK_ENTITY_TYPE
    );

    private static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(
            NumismaticConstants.MOD_ID, Registries.MENU
    );

    public static final RegistrySupplier<MenuType<ShopStockMenu>> SHOP_STOCK_MENU =
            MENUS.register("shop_stock_menu",
                    () -> new MenuType<>(ShopStockMenu::new, FeatureFlags.VANILLA_SET));

    public static final RegistrySupplier<Block> SHOP_BLOCK = BLOCKS.register("shop_block",
            () -> new ShopBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.WOOD)
                    .strength(2.5f, 6.0f)
                    .sound(SoundType.WOOD)
                    .noOcclusion()
                    .pushReaction(PushReaction.BLOCK)
            )
    );

    public static final RegistrySupplier<Item> SHOP_BLOCK_ITEM = BLOCK_ITEMS.register("shop_block",
            () -> new BlockItem(SHOP_BLOCK.get(), new Item.Properties())
    );

    @SuppressWarnings("ConstantConditions")
    public static final RegistrySupplier<BlockEntityType<ShopBlockEntity>> SHOP_BLOCK_ENTITY =
            BLOCK_ENTITIES.register("shop_block",
                    () -> BlockEntityType.Builder.of(
                            (pos, state) -> new ShopBlockEntity(NumismaticShop.SHOP_BLOCK_ENTITY.get(), pos, state),
                            SHOP_BLOCK.get()
                    ).build(null)
            );

    public static final RegistrySupplier<Block> ADMIN_SHOP_BLOCK = BLOCKS.register("admin_shop_block",
            () -> new AdminShopBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.GOLD)
                    .strength(-1.0f, 3600000.0f) // unbreakable like bedrock
                    .sound(SoundType.METAL)
                    .noOcclusion()
                    .pushReaction(PushReaction.BLOCK)
            )
    );

    public static final RegistrySupplier<Item> ADMIN_SHOP_BLOCK_ITEM = BLOCK_ITEMS.register("admin_shop_block",
            () -> new BlockItem(ADMIN_SHOP_BLOCK.get(), new Item.Properties().rarity(Rarity.EPIC))
    );

    @SuppressWarnings("ConstantConditions")
    public static final RegistrySupplier<BlockEntityType<ShopBlockEntity>> ADMIN_SHOP_BLOCK_ENTITY =
            BLOCK_ENTITIES.register("admin_shop_block",
                    () -> BlockEntityType.Builder.of(
                            (pos, state) -> {
                                ShopBlockEntity be = new ShopBlockEntity(NumismaticShop.ADMIN_SHOP_BLOCK_ENTITY.get(), pos, state);
                                be.setAdmin(true);
                                return be;
                            },
                            ADMIN_SHOP_BLOCK.get()
                    ).build(null)
            );

    public static void register() {
        BLOCKS.register();
        BLOCK_ITEMS.register();
        BLOCK_ENTITIES.register();
        MENUS.register();
    }

    private NumismaticShop() {}
}
