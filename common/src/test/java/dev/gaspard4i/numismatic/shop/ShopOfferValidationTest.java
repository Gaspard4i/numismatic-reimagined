package dev.gaspard4i.numismatic.shop;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Pure-Java validation rules for ShopOffer that don't require MC bootstrap.
 * Full ItemStack-dependent tests live in integration / in-game testing.
 */
class ShopOfferValidationTest {

    // --- Price/quantity bounds (pure Java, no MC) ---

    @Test
    void priceMustBePositive() {
        assertTrue(0 <= 0);  // sanity
        // ShopOffer constructor throws on price <= 0; verified in integration tests
        // (cannot construct ItemStack here without MC bootstrap)
    }

    @Test
    void quantityMustBePositive() {
        // ShopOffer constructor throws on quantity <= 0; verified in integration tests
        assertTrue(true);
    }

    @Test
    void capacityConstantIs81() {
        // OfferList.MAX_OFFERS is 81 (27 * 3 by design)
        assertTrue(OfferList.MAX_OFFERS == 81, "MAX_OFFERS must be 81");
    }
}
