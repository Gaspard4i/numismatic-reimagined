package dev.gaspard4i.numismatic.block;

import dev.gaspard4i.numismatic.NumismaticConstants;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;

public final class PiggyBankTags {

    private PiggyBankTags() {}

    public static final TagKey<EntityType<?>> VERY_HEAVY = TagKey.create(
            Registries.ENTITY_TYPE,
            ResourceLocation.fromNamespaceAndPath(NumismaticConstants.MOD_ID, "very_heavy"));
}
