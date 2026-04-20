package dev.gaspard4i.numismatic.shop;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ShopTransferLogicTest {

    @Test
    void canAcceptWhenTransferEnabledAndTemplateMatches() {
        assertTrue(ShopTransferLogic.canAcceptTemplate(true, "apple", "apple"));
    }

    @Test
    void cannotAcceptWhenTransferDisabled() {
        assertFalse(ShopTransferLogic.canAcceptTemplate(false, "apple", "apple"));
    }

    @Test
    void cannotAcceptDifferentTemplate() {
        assertFalse(ShopTransferLogic.canAcceptTemplate(true, "apple", "bread"));
    }

    @Test
    void cannotAcceptNullTemplate() {
        assertFalse(ShopTransferLogic.canAcceptTemplate(true, null, "apple"));
        assertFalse(ShopTransferLogic.canAcceptTemplate(true, "apple", null));
    }

    @Test
    void sellReturnsPriceWhenStockIsSufficient() {
        assertEquals(100L, ShopTransferLogic.trySellWithStockAndPrice(100, 20, 5));
    }

    @Test
    void sellReturnsZeroOnInsufficientStock() {
        assertEquals(0L, ShopTransferLogic.trySellWithStockAndPrice(100, 3, 5));
    }

    @Test
    void sellReturnsZeroOnInvalidPriceOrQuantity() {
        assertEquals(0L, ShopTransferLogic.trySellWithStockAndPrice(0, 10, 5));
        assertEquals(0L, ShopTransferLogic.trySellWithStockAndPrice(-1, 10, 5));
        assertEquals(0L, ShopTransferLogic.trySellWithStockAndPrice(100, 10, 0));
    }

    @Test
    void remainingStockDecreasesOnValidSale() {
        assertEquals(15, ShopTransferLogic.remainingStockAfterSale(20, 5));
    }

    @Test
    void remainingStockUnchangedOnInvalidSale() {
        assertEquals(3, ShopTransferLogic.remainingStockAfterSale(3, 5));
        assertEquals(10, ShopTransferLogic.remainingStockAfterSale(10, 0));
    }
}
