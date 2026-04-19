package dev.gaspard4i.numismatic.shop;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ShopRevenueTest {

    @Test
    void newRevenueIsZero() {
        assertEquals(0, new ShopRevenue().get());
    }

    @Test
    void initialNegativeIsClampedToZero() {
        assertEquals(0, new ShopRevenue(-100).get());
    }

    @Test
    void initialPositiveIsKept() {
        assertEquals(500, new ShopRevenue(500).get());
    }

    @Test
    void addPositiveAccumulates() {
        ShopRevenue r = new ShopRevenue();
        assertEquals(100, r.add(100));
        assertEquals(100, r.get());
        assertEquals(50, r.add(50));
        assertEquals(150, r.get());
    }

    @Test
    void addZeroIsNoop() {
        ShopRevenue r = new ShopRevenue(50);
        assertEquals(0, r.add(0));
        assertEquals(50, r.get());
    }

    @Test
    void addNegativeIsNoop() {
        ShopRevenue r = new ShopRevenue(50);
        assertEquals(0, r.add(-100));
        assertEquals(50, r.get());
    }

    @Test
    void withdrawReturnsAndResets() {
        ShopRevenue r = new ShopRevenue(1234);
        assertEquals(1234, r.withdraw());
        assertEquals(0, r.get());
    }

    @Test
    void withdrawZeroWhenEmpty() {
        ShopRevenue r = new ShopRevenue();
        assertEquals(0, r.withdraw());
    }
}
