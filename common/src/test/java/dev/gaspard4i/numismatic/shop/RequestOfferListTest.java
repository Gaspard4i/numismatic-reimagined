package dev.gaspard4i.numismatic.shop;

import dev.gaspard4i.numismatic.testutil.McBootstrap;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RequestOfferListTest {

    @BeforeAll
    static void setup() { McBootstrap.ensure(); }

    private static RequestOffer sample(int qty) {
        return new RequestOffer(new ItemStack(Items.DIAMOND), qty, 0, 100, false);
    }

    @Test
    void newListIsEmpty() {
        RequestOfferList l = new RequestOfferList();
        assertTrue(l.isEmpty());
        assertFalse(l.isFull());
        assertEquals(0, l.size());
        assertNull(l.get(0));
    }

    @Test
    void addReturnsIndex() {
        RequestOfferList l = new RequestOfferList();
        assertEquals(0, l.add(sample(5)));
        assertEquals(1, l.add(sample(6)));
        assertEquals(2, l.size());
    }

    @Test
    void addRejectsNull() {
        RequestOfferList l = new RequestOfferList();
        assertEquals(-1, l.add(null));
    }

    @Test
    void addRejectsWhenFull() {
        RequestOfferList l = new RequestOfferList();
        for (int i = 0; i < RequestOfferList.MAX_OFFERS; i++) l.add(sample(i + 1));
        assertTrue(l.isFull());
        assertEquals(-1, l.add(sample(100)));
    }

    @Test
    void replaceAtValidIndex() {
        RequestOfferList l = new RequestOfferList();
        l.add(sample(5));
        RequestOffer replacement = sample(10);
        assertTrue(l.replace(0, replacement));
        assertEquals(replacement, l.get(0));
    }

    @Test
    void replaceOutOfBoundsRejected() {
        RequestOfferList l = new RequestOfferList();
        assertFalse(l.replace(0, sample(5)));
        assertFalse(l.replace(-1, sample(5)));
    }

    @Test
    void replaceNullRejected() {
        RequestOfferList l = new RequestOfferList();
        l.add(sample(5));
        assertFalse(l.replace(0, null));
    }

    @Test
    void removeAtValidIndex() {
        RequestOfferList l = new RequestOfferList();
        l.add(sample(5));
        assertTrue(l.remove(0));
        assertTrue(l.isEmpty());
    }

    @Test
    void removeOutOfBoundsRejected() {
        RequestOfferList l = new RequestOfferList();
        assertFalse(l.remove(0));
    }

    @Test
    void asListIsReadOnly() {
        RequestOfferList l = new RequestOfferList();
        l.add(sample(5));
        var view = l.asList();
        assertThrows(UnsupportedOperationException.class, () -> view.add(sample(6)));
    }

    @Test
    void nbtRoundTrip() {
        RequestOfferList l = new RequestOfferList();
        l.add(sample(5));
        l.add(sample(10));
        CompoundTag tag = l.toTag();
        RequestOfferList reloaded = RequestOfferList.fromTag(tag);
        assertEquals(2, reloaded.size());
        assertEquals(5, reloaded.get(0).requested());
        assertEquals(10, reloaded.get(1).requested());
    }

    @Test
    void fromTagEmpty() {
        RequestOfferList reloaded = RequestOfferList.fromTag(new CompoundTag());
        assertTrue(reloaded.isEmpty());
    }
}
