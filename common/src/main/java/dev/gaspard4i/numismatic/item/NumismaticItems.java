package dev.gaspard4i.numismatic.item;

import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import dev.gaspard4i.numismatic.NumismaticConstants;
import dev.gaspard4i.numismatic.currency.Currency;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.Item;

import java.util.EnumMap;
import java.util.Map;

public final class NumismaticItems {

    private NumismaticItems() {}

    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(NumismaticConstants.MOD_ID, Registries.ITEM);

    private static final Map<Currency, RegistrySupplier<Item>> COIN_ITEMS = new EnumMap<>(Currency.class);

    public static final RegistrySupplier<Item> BRONZE_COIN = registerCoin(Currency.BRONZE, "bronze_coin");
    public static final RegistrySupplier<Item> SILVER_COIN = registerCoin(Currency.SILVER, "silver_coin");
    public static final RegistrySupplier<Item> GOLD_COIN = registerCoin(Currency.GOLD, "gold_coin");
    public static final RegistrySupplier<Item> NETHERITE_COIN = registerCoin(Currency.NETHERITE, "netherite_coin");

    public static final RegistrySupplier<Item> MONEY_BAG = ITEMS.register(
            "money_bag",
            () -> new MoneyBagItem(new Item.Properties())
    );

    private static RegistrySupplier<Item> registerCoin(Currency currency, String id) {
        RegistrySupplier<Item> supplier = ITEMS.register(
                id,
                () -> new CoinItem(currency, new Item.Properties())
        );
        COIN_ITEMS.put(currency, supplier);
        return supplier;
    }

    public static Item getCoinItem(Currency currency) {
        return COIN_ITEMS.get(currency).get();
    }

    public static void register() {
        ITEMS.register();
    }
}
