package dev.gaspard4i.numismatic.villager;

import dev.architectury.registry.level.entity.trade.TradeRegistry;
import dev.gaspard4i.numismatic.item.NumismaticItems;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.entity.npc.VillagerTrades;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.trading.MerchantOffer;

import java.util.function.Supplier;

public final class NumismaticTrades {

    private NumismaticTrades() {}

    public static void register() {
        // Lazy-resolve item references at trade time, not at register time,
        // so registry sync has already populated NumismaticItems.*.get().
        TradeRegistry.registerVillagerTrade(
                VillagerProfession.LIBRARIAN,
                1,
                buyCoin(() -> Items.EMERALD, 1, () -> NumismaticItems.SILVER_COIN.get(), 1, 12, 1)
        );
        TradeRegistry.registerVillagerTrade(
                VillagerProfession.LIBRARIAN,
                3,
                buyCoin(() -> Items.EMERALD, 1, () -> NumismaticItems.GOLD_COIN.get(), 1, 12, 5)
        );
    }

    private static VillagerTrades.ItemListing buyCoin(
            Supplier<Item> input, int inputCount,
            Supplier<Item> output, int outputCount,
            int maxUses, int xp
    ) {
        return (entity, source) -> new MerchantOffer(
                new net.minecraft.world.item.trading.ItemCost(input.get(), inputCount),
                new ItemStack(output.get(), outputCount),
                maxUses, xp, 0.05f
        );
    }
}
