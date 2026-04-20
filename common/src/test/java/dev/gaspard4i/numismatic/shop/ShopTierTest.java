package dev.gaspard4i.numismatic.shop;

import dev.gaspard4i.numismatic.currency.Currency;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ShopTierTest {

    @Test
    void stockSizesIncreaseWithTier() {
        assertEquals(9, ShopTier.BRONZE.stockSize());
        assertEquals(18, ShopTier.SILVER.stockSize());
        assertEquals(27, ShopTier.GOLD.stockSize());
        assertEquals(36, ShopTier.NETHERITE.stockSize());
    }

    @Test
    void maxOffersIncreaseWithTier() {
        assertTrue(ShopTier.BRONZE.maxOffers() < ShopTier.SILVER.maxOffers());
        assertTrue(ShopTier.SILVER.maxOffers() < ShopTier.GOLD.maxOffers());
        assertTrue(ShopTier.GOLD.maxOffers() < ShopTier.NETHERITE.maxOffers());
        assertTrue(ShopTier.NETHERITE.maxOffers() < ShopTier.ADMIN.maxOffers());
    }

    @Test
    void adminFlagged() {
        assertTrue(ShopTier.ADMIN.isAdmin());
        for (ShopTier t : ShopTier.values()) {
            if (t != ShopTier.ADMIN) assertFalse(t.isAdmin(), "tier not admin: " + t);
        }
    }

    @Test
    void playerTiersHaveCoin() {
        assertEquals(Currency.BRONZE, ShopTier.BRONZE.coin());
        assertEquals(Currency.SILVER, ShopTier.SILVER.coin());
        assertEquals(Currency.GOLD, ShopTier.GOLD.coin());
        assertEquals(Currency.NETHERITE, ShopTier.NETHERITE.coin());
    }

    @Test
    void adminHasNoCoin() {
        assertNull(ShopTier.ADMIN.coin());
    }

    @Test
    void blockIdsForEachTier() {
        assertEquals("bronze_shop", ShopTier.BRONZE.blockId());
        assertEquals("silver_shop", ShopTier.SILVER.blockId());
        // Gold/Admin keep legacy names for save compat.
        assertEquals("shop_block", ShopTier.GOLD.blockId());
        assertEquals("admin_shop_block", ShopTier.ADMIN.blockId());
        assertEquals("netherite_shop", ShopTier.NETHERITE.blockId());
    }

    @Test
    void maxOffersRespectAbsoluteCap() {
        for (ShopTier t : ShopTier.values()) {
            assertTrue(t.maxOffers() <= OfferList.ABSOLUTE_MAX,
                    "tier exceeds absolute cap: " + t);
        }
    }

    @Test
    void stockSizesAreStandardMinecraftMultiples() {
        // 9 cols → stock must be a multiple of 9 so the grid stays regular.
        for (ShopTier t : ShopTier.values()) {
            assertEquals(0, t.stockSize() % 9,
                    "stock not multiple of 9 for " + t);
        }
    }
}
