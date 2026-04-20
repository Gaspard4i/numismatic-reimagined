package dev.gaspard4i.numismatic.shop;

import dev.gaspard4i.numismatic.testutil.McBootstrap;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RequestOfferTest {

    @BeforeAll
    static void setup() { McBootstrap.ensure(); }

    @Test
    void rejectsEmptyTemplate() {
        assertThrows(IllegalArgumentException.class,
                () -> new RequestOffer(ItemStack.EMPTY, 1, 0, 100, false));
    }

    @Test
    void rejectsNullTemplate() {
        assertThrows(IllegalArgumentException.class,
                () -> new RequestOffer(null, 1, 0, 100, false));
    }

    @Test
    void rejectsZeroRequested() {
        assertThrows(IllegalArgumentException.class,
                () -> new RequestOffer(new ItemStack(Items.DIAMOND), 0, 0, 100, false));
    }

    @Test
    void rejectsNegativeFulfilled() {
        assertThrows(IllegalArgumentException.class,
                () -> new RequestOffer(new ItemStack(Items.DIAMOND), 5, -1, 100, false));
    }

    @Test
    void rejectsFulfilledExceedingRequested() {
        assertThrows(IllegalArgumentException.class,
                () -> new RequestOffer(new ItemStack(Items.DIAMOND), 5, 6, 100, false));
    }

    @Test
    void rejectsZeroPrice() {
        assertThrows(IllegalArgumentException.class,
                () -> new RequestOffer(new ItemStack(Items.DIAMOND), 5, 0, 0, false));
    }

    @Test
    void remainingDecreasesWithFulfillment() {
        RequestOffer o = new RequestOffer(new ItemStack(Items.DIAMOND), 10, 3, 100, false);
        assertEquals(7, o.remaining());
        assertFalse(o.isComplete());
    }

    @Test
    void completedWhenFulfilledEqualsRequested() {
        RequestOffer o = new RequestOffer(new ItemStack(Items.DIAMOND), 5, 5, 100, false);
        assertEquals(0, o.remaining());
        assertTrue(o.isComplete());
    }

    @Test
    void withFulfilledIncrementClampsAtRequested() {
        RequestOffer o = new RequestOffer(new ItemStack(Items.DIAMOND), 10, 8, 100, false);
        RequestOffer next = o.withFulfilledIncrement(5);
        assertEquals(10, next.fulfilled());
        assertTrue(next.isComplete());
    }

    @Test
    void withFulfilledIncrementIgnoresNegative() {
        RequestOffer o = new RequestOffer(new ItemStack(Items.DIAMOND), 10, 4, 100, false);
        RequestOffer next = o.withFulfilledIncrement(-5);
        assertEquals(4, next.fulfilled());
    }

    @Test
    void ntbRoundTrip() {
        ItemStack sword = new ItemStack(Items.DIAMOND_SWORD);
        sword.getOrCreateTag().putString("Custom", "Hello");
        RequestOffer original = new RequestOffer(sword, 3, 1, 5000, true);
        CompoundTag tag = original.toTag();
        RequestOffer restored = RequestOffer.fromTag(tag);
        assertEquals(original.requested(), restored.requested());
        assertEquals(original.fulfilled(), restored.fulfilled());
        assertEquals(original.pricePerItem(), restored.pricePerItem());
        assertEquals(original.strictNbt(), restored.strictNbt());
        assertTrue(ItemStack.isSameItemSameTags(original.template(), restored.template()));
    }
}
