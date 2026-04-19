package dev.gaspard4i.numismatic.item;

import dev.architectury.registry.CreativeTabRegistry;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import dev.gaspard4i.numismatic.NumismaticConstants;
import dev.gaspard4i.numismatic.block.NumismaticBlocks;
import dev.gaspard4i.numismatic.currency.Currency;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.EnumMap;
import java.util.Map;

public final class NumismaticItems {

    private static final DeferredRegister<Item> ITEMS = DeferredRegister.create(
            NumismaticConstants.MOD_ID, Registries.ITEM
    );

    private static final DeferredRegister<CreativeModeTab> TABS = DeferredRegister.create(
            NumismaticConstants.MOD_ID, Registries.CREATIVE_MODE_TAB
    );

    private static final Map<Currency, RegistrySupplier<Item>> COIN_ITEMS = new EnumMap<>(Currency.class);

    // Coins
    public static final RegistrySupplier<Item> BRONZE_COIN = registerCoin(Currency.BRONZE);
    public static final RegistrySupplier<Item> SILVER_COIN = registerCoin(Currency.SILVER);
    public static final RegistrySupplier<Item> GOLD_COIN = registerCoin(Currency.GOLD);
    public static final RegistrySupplier<Item> NETHERITE_COIN = registerCoin(Currency.NETHERITE);

    // Special items
    public static final RegistrySupplier<Item> STAR_COIN = ITEMS.register("star_coin", StarCoinItem::new);
    public static final RegistrySupplier<Item> MONEY_BAG = ITEMS.register("money_bag",
            () -> new MoneyBagItem(new Item.Properties().stacksTo(1))
    );

    // Creative tab
    public static final RegistrySupplier<CreativeModeTab> NUMISMATIC_TAB = TABS.register("numismatic_tab",
            () -> CreativeTabRegistry.create(builder -> builder
                    .title(Component.translatable("itemGroup.numismatic_reimagined"))
                    .icon(() -> new ItemStack(GOLD_COIN.get()))
                    .displayItems((params, output) -> {
                        output.accept(new ItemStack(BRONZE_COIN.get()));
                        output.accept(new ItemStack(SILVER_COIN.get()));
                        output.accept(new ItemStack(GOLD_COIN.get()));
                        output.accept(new ItemStack(NETHERITE_COIN.get()));
                        output.accept(new ItemStack(STAR_COIN.get()));
                        output.accept(MoneyBagItem.createWithValue(0));
                        output.accept(new ItemStack(NumismaticBlocks.PIGGY_BANK_ITEM.get()));
                        output.accept(new ItemStack(NumismaticBlocks.GOLDEN_PIGGY_BANK_ITEM.get()));
                        output.accept(new ItemStack(NumismaticBlocks.NETHERITE_PIGGY_BANK_ITEM.get()));
                    })
            )
    );

    private static RegistrySupplier<Item> registerCoin(Currency currency) {
        RegistrySupplier<Item> supplier = ITEMS.register(currency.getItemId(),
                () -> new CoinItem(currency, new Item.Properties().stacksTo(99))
        );
        COIN_ITEMS.put(currency, supplier);
        return supplier;
    }

    /**
     * Gets the Item instance for a given currency denomination.
     */
    @Nullable
    public static Item getCoinItem(Currency currency) {
        RegistrySupplier<Item> supplier = COIN_ITEMS.get(currency);
        return supplier != null ? supplier.get() : null;
    }

    /**
     * Gets the RegistrySupplier for a given currency denomination.
     */
    @Nullable
    public static RegistrySupplier<Item> getCoinSupplier(Currency currency) {
        return COIN_ITEMS.get(currency);
    }

    public static void register() {
        ITEMS.register();
        TABS.register();
    }

    private NumismaticItems() {}
}
