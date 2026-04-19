package dev.gaspard4i.numismatic.shop;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ShopMenuModeTest {

    @Test
    void hasThreeModes() {
        assertEquals(3, ShopMenuMode.values().length);
    }

    @Test
    void containsExpectedModes() {
        assertNotNull(ShopMenuMode.valueOf("OFFERS"));
        assertNotNull(ShopMenuMode.valueOf("STOCK"));
        assertNotNull(ShopMenuMode.valueOf("CLIENT"));
    }

    @Test
    void ordinalsAreStable() {
        assertEquals(0, ShopMenuMode.OFFERS.ordinal());
        assertEquals(1, ShopMenuMode.STOCK.ordinal());
        assertEquals(2, ShopMenuMode.CLIENT.ordinal());
    }
}
