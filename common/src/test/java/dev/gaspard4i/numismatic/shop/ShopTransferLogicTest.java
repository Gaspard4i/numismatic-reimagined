package dev.gaspard4i.numismatic.shop;

import dev.gaspard4i.numismatic.testutil.McBootstrap;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ShopTransferLogicTest {

    @BeforeAll
    static void setup() {
        McBootstrap.ensure();
    }

    @Test
    void transferDisabledAlwaysRejects() {
        ShopOffer o = new ShopOffer(new ItemStack(Items.DIAMOND), 100L, 1);
        assertFalse(ShopTransferLogic.canHopperInsert(
                new ItemStack(Items.DIAMOND), List.of(o), false));
    }

    @Test
    void emptyStackRejected() {
        ShopOffer o = new ShopOffer(new ItemStack(Items.DIAMOND), 100L, 1);
        assertFalse(ShopTransferLogic.canHopperInsert(ItemStack.EMPTY, List.of(o), true));
    }

    @Test
    void nullStackRejected() {
        ShopOffer o = new ShopOffer(new ItemStack(Items.DIAMOND), 100L, 1);
        assertFalse(ShopTransferLogic.canHopperInsert(null, List.of(o), true));
    }

    @Test
    void noOffersRejected() {
        assertFalse(ShopTransferLogic.canHopperInsert(
                new ItemStack(Items.DIAMOND), List.of(), true));
    }

    @Test
    void nullOffersRejected() {
        assertFalse(ShopTransferLogic.canHopperInsert(
                new ItemStack(Items.DIAMOND), null, true));
    }

    @Test
    void matchingOfferAccepted() {
        ShopOffer o = new ShopOffer(new ItemStack(Items.DIAMOND), 100L, 1);
        assertTrue(ShopTransferLogic.canHopperInsert(
                new ItemStack(Items.DIAMOND), List.of(o), true));
    }

    @Test
    void nonMatchingOfferRejected() {
        ShopOffer o = new ShopOffer(new ItemStack(Items.DIAMOND), 100L, 1);
        assertFalse(ShopTransferLogic.canHopperInsert(
                new ItemStack(Items.IRON_INGOT), List.of(o), true));
    }

    @Test
    void matchesAcrossMultipleOffers() {
        ShopOffer diamond = new ShopOffer(new ItemStack(Items.DIAMOND), 100L, 1);
        ShopOffer iron = new ShopOffer(new ItemStack(Items.IRON_INGOT), 10L, 4);
        assertTrue(ShopTransferLogic.canHopperInsert(
                new ItemStack(Items.IRON_INGOT), List.of(diamond, iron), true));
    }
}
