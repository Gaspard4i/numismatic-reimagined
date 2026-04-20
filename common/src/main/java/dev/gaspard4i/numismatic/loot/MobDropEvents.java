package dev.gaspard4i.numismatic.loot;

import dev.architectury.event.events.common.EntityEvent;
import dev.gaspard4i.numismatic.currency.Currency;
import dev.gaspard4i.numismatic.currency.CurrencyResolver;
import dev.gaspard4i.numismatic.item.NumismaticItems;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import java.util.Random;

public final class MobDropEvents {

    private MobDropEvents() {}

    private static final Random RNG = new Random();
    private static final double DEFAULT_DROP_PERCENT = 0.30;
    private static final double DEFAULT_VARIANCE = 0.40;

    public static void register() {
        EntityEvent.LIVING_DEATH.register((entity, source) -> {
            if (!(entity instanceof Mob mob)) return dev.architectury.event.EventResult.pass();
            Level level = mob.level();
            if (level.isClientSide()) return dev.architectury.event.EventResult.pass();
            long base = baseValueFor(mob);
            long drop = MobDropLogic.computeDrop(base, DEFAULT_DROP_PERCENT, DEFAULT_VARIANCE, RNG);
            if (drop <= 0) return dev.architectury.event.EventResult.pass();
            spawnAsCoins(level, mob, drop);
            return dev.architectury.event.EventResult.pass();
        });
    }

    private static long baseValueFor(Mob mob) {
        float maxHealth = mob.getMaxHealth();
        return Math.round(maxHealth * 2.0f);
    }

    private static void spawnAsCoins(Level level, Mob mob, long rawValue) {
        long[] split = CurrencyResolver.splitValues(rawValue);
        for (int i = 0; i < split.length; i++) {
            long count = split[i];
            if (count <= 0) continue;
            Currency c = Currency.values()[i];
            while (count > 0) {
                int stackSize = (int) Math.min(count, 64L);
                ItemStack coins = new ItemStack(NumismaticItems.getCoinItem(c), stackSize);
                ItemEntity item = new ItemEntity(level, mob.getX(), mob.getY(), mob.getZ(), coins);
                level.addFreshEntity(item);
                count -= stackSize;
            }
        }
    }
}
