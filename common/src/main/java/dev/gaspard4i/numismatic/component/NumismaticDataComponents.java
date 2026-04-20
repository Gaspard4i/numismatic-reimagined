package dev.gaspard4i.numismatic.component;

import com.mojang.serialization.Codec;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import dev.gaspard4i.numismatic.NumismaticConstants;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.core.component.DataComponentType;

public final class NumismaticDataComponents {

    private NumismaticDataComponents() {}

    public static final DeferredRegister<DataComponentType<?>> COMPONENTS =
            DeferredRegister.create(NumismaticConstants.MOD_ID, Registries.DATA_COMPONENT_TYPE);

    public static final RegistrySupplier<DataComponentType<Long>> MONEY_BAG_VALUE = COMPONENTS.register(
            "money_bag_value",
            () -> DataComponentType.<Long>builder()
                    .persistent(Codec.LONG)
                    .networkSynchronized(ByteBufCodecs.VAR_LONG)
                    .build()
    );

    public static void register() {
        COMPONENTS.register();
    }

    // Force-load CustomData class to ensure it's available at runtime (silences unused-import warnings).
    @SuppressWarnings("unused")
    private static final Class<?> CUSTOM_DATA_CLASS = CustomData.class;
}
