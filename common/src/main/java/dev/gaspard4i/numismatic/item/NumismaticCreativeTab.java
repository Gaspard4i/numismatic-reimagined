package dev.gaspard4i.numismatic.item;

import dev.architectury.registry.CreativeTabRegistry;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import dev.gaspard4i.numismatic.NumismaticConstants;
import dev.gaspard4i.numismatic.block.PiggyBankBlocks;
import dev.gaspard4i.numismatic.block.PiggyBankTier;
import dev.gaspard4i.numismatic.currency.Currency;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;

public final class NumismaticCreativeTab {

    private NumismaticCreativeTab() {}

    public static final DeferredRegister<CreativeModeTab> TABS =
            DeferredRegister.create(NumismaticConstants.MOD_ID, Registries.CREATIVE_MODE_TAB);

    public static final RegistrySupplier<CreativeModeTab> MAIN = TABS.register(
            "main",
            () -> CreativeTabRegistry.create(
                    Component.translatable("itemGroup.numismatic_reimagined"),
                    () -> new ItemStack(NumismaticItems.GOLD_COIN.get())
            )
    );

    public static void registerContents() {
        // Architectury 13 uses MODIFY_ENTRIES_ALL for adding items; the simplest
        // approach for a small tab is to populate it via Fabric-style builder.
        CreativeTabRegistry.appendStack(
                MAIN,
                new ItemStack(NumismaticItems.BRONZE_COIN.get()),
                new ItemStack(NumismaticItems.SILVER_COIN.get()),
                new ItemStack(NumismaticItems.GOLD_COIN.get()),
                new ItemStack(NumismaticItems.NETHERITE_COIN.get()),
                new ItemStack(NumismaticItems.MONEY_BAG.get()),
                new ItemStack(PiggyBankBlocks.blockFor(PiggyBankTier.BASE)),
                new ItemStack(PiggyBankBlocks.blockFor(PiggyBankTier.GOLDEN)),
                new ItemStack(PiggyBankBlocks.blockFor(PiggyBankTier.NETHERITE))
        );
        // Silence unused warning while keeping reference for future dynamic colors.
        var _unused = Currency.BRONZE;
    }

    public static void register() {
        TABS.register();
        registerContents();
    }
}
