package dev.gaspard4i.numismatic.shop;

import dev.gaspard4i.numismatic.testutil.McBootstrap;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Note: branches that check {@code instanceof CoinItem / MoneyBagItem} cannot
 * be exercised in JUnit (item registries freeze after bootstrap, no way to
 * construct new instances). Those paths are validated via the in-game
 * checklist in STATUS.md Phase 4 ("refus vente coins").
 */
class ShopOfferTest {

    @BeforeAll
    static void bootstrapMc() {
        McBootstrap.ensure();
    }

    @Test
    void rejectsEmptyTemplate() {
        assertThrows(IllegalArgumentException.class,
                () -> new ShopOffer(ItemStack.EMPTY, 100, 1));
    }

    @Test
    void rejectsZeroPrice() {
        assertThrows(IllegalArgumentException.class,
                () -> new ShopOffer(new ItemStack(Items.APPLE), 0, 1));
    }

    @Test
    void rejectsNegativePrice() {
        assertThrows(IllegalArgumentException.class,
                () -> new ShopOffer(new ItemStack(Items.APPLE), -5, 1));
    }

    @Test
    void rejectsZeroQuantity() {
        assertThrows(IllegalArgumentException.class,
                () -> new ShopOffer(new ItemStack(Items.APPLE), 100, 0));
    }

    @Test
    void rejectsNegativeQuantity() {
        assertThrows(IllegalArgumentException.class,
                () -> new ShopOffer(new ItemStack(Items.APPLE), 100, -1));
    }

    @Test
    void acceptsValidVanillaTemplate() {
        ShopOffer offer = new ShopOffer(new ItemStack(Items.APPLE), 100, 5);
        assertEquals(100, offer.priceBronze());
        assertEquals(5, offer.quantityPerPurchase());
        assertEquals(Items.APPLE, offer.template().getItem());
    }

    @Test
    void isTemplateAllowedRejectsEmpty() {
        assertFalse(ShopOffer.isTemplateAllowed(ItemStack.EMPTY));
    }

    @Test
    void isTemplateAllowedAcceptsVanilla() {
        assertTrue(ShopOffer.isTemplateAllowed(new ItemStack(Items.DIAMOND)));
    }

    @Test
    void nbtRoundtrip() {
        ItemStack tpl = new ItemStack(Items.APPLE);
        tpl.getOrCreateTag().putInt("CustomKey", 42);
        ShopOffer original = new ShopOffer(tpl, 1234, 7);
        CompoundTag tag = original.toTag();
        ShopOffer restored = ShopOffer.fromTag(tag);
        assertEquals(original.priceBronze(), restored.priceBronze());
        assertEquals(original.quantityPerPurchase(), restored.quantityPerPurchase());
        assertEquals(Items.APPLE, restored.template().getItem());
        assertEquals(42, restored.template().getOrCreateTag().getInt("CustomKey"));
    }

    @Test
    void createPurchasedStackProducesCorrectCount() {
        ShopOffer offer = new ShopOffer(new ItemStack(Items.DIAMOND), 100, 5);
        ItemStack purchased = offer.createPurchasedStack();
        assertEquals(5, purchased.getCount());
        assertEquals(Items.DIAMOND, purchased.getItem());
    }

    @Test
    void createPurchasedStackDoesNotMutateTemplate() {
        ItemStack tpl = new ItemStack(Items.APPLE, 1);
        ShopOffer offer = new ShopOffer(tpl, 100, 64);
        ItemStack purchased = offer.createPurchasedStack();
        assertEquals(64, purchased.getCount());
        assertEquals(1, offer.template().getCount(), "Template count must not be mutated");
    }
}
