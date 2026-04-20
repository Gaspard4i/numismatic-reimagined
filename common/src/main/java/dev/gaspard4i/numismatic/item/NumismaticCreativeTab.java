package dev.gaspard4i.numismatic.item;

import dev.architectury.registry.CreativeTabRegistry;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import dev.gaspard4i.numismatic.NumismaticConstants;
import dev.gaspard4i.numismatic.block.PiggyBankBlocks;
import dev.gaspard4i.numismatic.block.PiggyBankTier;
import dev.gaspard4i.numismatic.currency.Currency;
import dev.gaspard4i.numismatic.request.RequestBoardBlocks;
import dev.gaspard4i.numismatic.shop.NumismaticShops;
import dev.gaspard4i.numismatic.shop.ShopTier;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;

import java.util.function.Supplier;

public final class NumismaticCreativeTab {

    private NumismaticCreativeTab() {}

    public static final DeferredRegister<CreativeModeTab> TABS =
            DeferredRegister.create(NumismaticConstants.MOD_ID, Registries.CREATIVE_MODE_TAB);

    public static final RegistrySupplier<CreativeModeTab> MAIN = TABS.register(
            "main",
            () -> CreativeTabRegistry.create(builder -> builder
                    .title(Component.translatable("itemGroup.numismatic_reimagined"))
                    .icon(() -> new ItemStack(NumismaticItems.GOLD_COIN.get()))
                    .displayItems((params, output) -> {
                        add(output, NumismaticItems.BRONZE_COIN);
                        add(output, NumismaticItems.SILVER_COIN);
                        add(output, NumismaticItems.GOLD_COIN);
                        add(output, NumismaticItems.NETHERITE_COIN);
                        add(output, NumismaticItems.MONEY_BAG);
                        add(output, () -> PiggyBankBlocks.blockFor(PiggyBankTier.BASE));
                        add(output, () -> PiggyBankBlocks.blockFor(PiggyBankTier.GOLDEN));
                        add(output, () -> PiggyBankBlocks.blockFor(PiggyBankTier.NETHERITE));
                        add(output, () -> NumismaticShops.blockFor(ShopTier.BRONZE));
                        add(output, () -> NumismaticShops.blockFor(ShopTier.SILVER));
                        add(output, () -> NumismaticShops.blockFor(ShopTier.GOLD));
                        add(output, () -> NumismaticShops.blockFor(ShopTier.NETHERITE));
                        add(output, () -> NumismaticShops.blockFor(ShopTier.ADMIN));
                        add(output, RequestBoardBlocks.REQUEST_BOARD);
                        add(output, NumismaticItems.STAR_COIN);
                    })
            )
    );

    private static void add(CreativeModeTab.Output output, Supplier<? extends ItemLike> supplier) {
        output.accept(new ItemStack(supplier.get()));
    }

    public static void register() {
        TABS.register();
        // Silence unused warning while keeping reference for future dynamic colors.
        var _unused = Currency.BRONZE;
    }
}
