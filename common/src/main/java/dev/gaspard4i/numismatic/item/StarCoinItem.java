package dev.gaspard4i.numismatic.item;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;

public class StarCoinItem extends Item {

    public StarCoinItem(Properties properties) {
        super(properties.rarity(Rarity.EPIC).fireResistant().stacksTo(1));
    }
}
