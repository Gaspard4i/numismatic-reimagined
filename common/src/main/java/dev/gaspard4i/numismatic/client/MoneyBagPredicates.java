package dev.gaspard4i.numismatic.client;

import dev.gaspard4i.numismatic.currency.Currency;
import dev.gaspard4i.numismatic.item.MoneyBagItem;

/**
 * Pure logic for the {@code numismatic_reimagined:bag_tier} item model predicate.
 * Maps the bag's stored value to a [0, 1) float so the JSON model {@code overrides}
 * switch between the 4 texture variants (empty / low / mid / high).
 *
 * <p>Thresholds match the legacy 1.20.1 layout :
 * <ul>
 *   <li>{@code 0.00} → empty bag (money_bag_0)</li>
 *   <li>{@code 0.25} → &ge; 1 silver raw value (money_bag_1 / silver skin)</li>
 *   <li>{@code 0.50} → &ge; 1 gold raw value (money_bag_2 / gold skin)</li>
 *   <li>{@code 0.75} → &ge; 1 netherite raw value (money_bag_3 / netherite skin)</li>
 * </ul>
 */
public final class MoneyBagPredicates {

    public static final float EMPTY     = 0.0f;
    public static final float SILVER    = 0.25f;
    public static final float GOLD      = 0.5f;
    public static final float NETHERITE = 0.75f;

    private MoneyBagPredicates() {}

    public static float tierFor(long value) {
        if (value >= Currency.NETHERITE.getValue()) return NETHERITE;
        if (value >= Currency.GOLD.getValue()) return GOLD;
        if (value >= Currency.SILVER.getValue()) return SILVER;
        return EMPTY;
    }

    public static float tierFor(net.minecraft.world.item.ItemStack stack) {
        return tierFor(MoneyBagItem.getValue(stack));
    }
}
