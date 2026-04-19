package dev.gaspard4i.numismatic.shop;

/**
 * UI mode of a shop menu. Owners (and OP for admin shops) can switch between
 * tabs; buyers always see {@link #CLIENT}.
 */
public enum ShopMenuMode {
    /** Owner: list and edit offers (price, quantity, template). */
    OFFERS,
    /** Owner: manage stock (27 slots) and withdraw accumulated revenue. */
    STOCK,
    /** Buyer (or owner test mode): see offers, click to purchase. */
    CLIENT
}
