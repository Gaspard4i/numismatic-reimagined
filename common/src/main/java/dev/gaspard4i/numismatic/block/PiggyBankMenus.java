package dev.gaspard4i.numismatic.block;

import dev.architectury.registry.menu.MenuRegistry;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import dev.gaspard4i.numismatic.NumismaticConstants;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.MenuType;

public final class PiggyBankMenus {

    private PiggyBankMenus() {}

    public static final DeferredRegister<MenuType<?>> MENUS =
            DeferredRegister.create(NumismaticConstants.MOD_ID, Registries.MENU);

    public static final RegistrySupplier<MenuType<PiggyBankMenu>> PIGGY_BANK_MENU =
            MENUS.register("piggy_bank",
                    () -> MenuRegistry.ofExtended((syncId, inventory, buf) ->
                            new PiggyBankMenu(syncId, inventory)));

    public static void register() {
        MENUS.register();
    }
}
