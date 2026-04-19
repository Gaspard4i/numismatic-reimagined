package dev.gaspard4i.numismatic.shop;

import dev.gaspard4i.numismatic.testutil.McBootstrap;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class OfferListTest {

    @BeforeAll
    static void bootstrapMc() {
        McBootstrap.ensure();
    }

    private OfferList list;

    @BeforeEach
    void freshList() {
        list = new OfferList();
    }

    private static ShopOffer apple(int qty, int price) {
        return new ShopOffer(new ItemStack(Items.APPLE), price, qty);
    }

    @Test
    void newListIsEmpty() {
        assertEquals(0, list.size());
        assertTrue(list.isEmpty());
        assertFalse(list.isFull());
    }

    @Test
    void capacityIs56() {
        assertEquals(56, OfferList.MAX_OFFERS);
    }

    @Test
    void addReturnsAssignedIndex() {
        assertEquals(0, list.add(apple(1, 100)));
        assertEquals(1, list.add(apple(2, 200)));
    }

    @Test
    void addNullReturnsMinusOne() {
        assertEquals(-1, list.add(null));
    }

    @Test
    void addRejectsBeyondCapacity() {
        for (int i = 0; i < OfferList.MAX_OFFERS; i++) {
            assertEquals(i, list.add(apple(1, 100 + i)));
        }
        assertTrue(list.isFull());
        assertEquals(-1, list.add(apple(99, 99)));
    }

    @Test
    void getOutOfBoundsReturnsNull() {
        assertNull(list.get(-1));
        assertNull(list.get(0));
    }

    @Test
    void replaceUpdatesEntry() {
        list.add(apple(1, 100));
        assertTrue(list.replace(0, apple(99, 9999)));
        assertEquals(9999, list.get(0).priceBronze());
    }

    @Test
    void replaceOutOfBoundsReturnsFalse() {
        assertFalse(list.replace(0, apple(1, 100)));
    }

    @Test
    void removeShiftsIndices() {
        list.add(apple(1, 100));
        list.add(apple(2, 200));
        list.add(apple(3, 300));
        assertTrue(list.remove(1));
        assertEquals(2, list.size());
        assertEquals(100, list.get(0).priceBronze());
        assertEquals(300, list.get(1).priceBronze());
    }

    @Test
    void removeOutOfBoundsReturnsFalse() {
        assertFalse(list.remove(0));
    }

    @Test
    void clearEmptiesList() {
        list.add(apple(1, 100));
        list.clear();
        assertEquals(0, list.size());
    }

    @Test
    void asListIsUnmodifiable() {
        list.add(apple(1, 100));
        assertThrows(UnsupportedOperationException.class,
                () -> list.asList().add(apple(2, 200)));
    }

    @Test
    void nbtRoundtripPreservesOrder() {
        list.add(apple(1, 100));
        list.add(apple(5, 500));
        list.add(apple(10, 1000));
        CompoundTag tag = list.toTag();
        OfferList restored = OfferList.fromTag(tag);
        assertEquals(3, restored.size());
        assertEquals(100, restored.get(0).priceBronze());
        assertEquals(500, restored.get(1).priceBronze());
        assertEquals(1000, restored.get(2).priceBronze());
    }

    @Test
    void fromEmptyTagReturnsEmpty() {
        OfferList restored = OfferList.fromTag(new CompoundTag());
        assertEquals(0, restored.size());
    }
}
