package dev.gaspard4i.numismatic.shop;

import dev.gaspard4i.numismatic.testutil.McBootstrap;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ShopStockOpsTest {

    @BeforeAll
    static void bootstrapMc() {
        McBootstrap.ensure();
    }

    private NonNullList<ItemStack> stock;

    @BeforeEach
    void freshStock() {
        stock = NonNullList.withSize(27, ItemStack.EMPTY);
    }

    @Test
    void countMatchingEmptyTemplateReturnsZero() {
        stock.set(0, new ItemStack(Items.APPLE, 5));
        assertEquals(0, ShopStockOps.countMatching(stock, ItemStack.EMPTY));
    }

    @Test
    void countMatchingSingleSlot() {
        stock.set(0, new ItemStack(Items.APPLE, 5));
        assertEquals(5, ShopStockOps.countMatching(stock, new ItemStack(Items.APPLE)));
    }

    @Test
    void countMatchingMultipleSlots() {
        stock.set(0, new ItemStack(Items.APPLE, 64));
        stock.set(5, new ItemStack(Items.APPLE, 32));
        stock.set(10, new ItemStack(Items.APPLE, 1));
        assertEquals(97, ShopStockOps.countMatching(stock, new ItemStack(Items.APPLE)));
    }

    @Test
    void countMatchingIgnoresOtherItems() {
        stock.set(0, new ItemStack(Items.APPLE, 5));
        stock.set(1, new ItemStack(Items.DIAMOND, 10));
        assertEquals(5, ShopStockOps.countMatching(stock, new ItemStack(Items.APPLE)));
    }

    @Test
    void countMatchingNbtStrict() {
        ItemStack stackNoNbt = new ItemStack(Items.APPLE, 3);
        ItemStack stackWithNbt = new ItemStack(Items.APPLE, 3);
        stackWithNbt.getOrCreateTag().putInt("X", 1);
        stock.set(0, stackNoNbt);
        stock.set(1, stackWithNbt);

        ItemStack templateNoNbt = new ItemStack(Items.APPLE);
        ItemStack templateWithNbt = new ItemStack(Items.APPLE);
        templateWithNbt.getOrCreateTag().putInt("X", 1);

        assertEquals(3, ShopStockOps.countMatching(stock, templateNoNbt));
        assertEquals(3, ShopStockOps.countMatching(stock, templateWithNbt));
    }

    @Test
    void countMatchingDifferentNbtNoMatch() {
        ItemStack stack = new ItemStack(Items.APPLE, 3);
        stack.getOrCreateTag().putInt("X", 1);
        stock.set(0, stack);

        ItemStack template = new ItemStack(Items.APPLE);
        template.getOrCreateTag().putInt("X", 2);

        assertEquals(0, ShopStockOps.countMatching(stock, template));
    }

    @Test
    void consumeZeroAmountReturnsTrue() {
        assertTrue(ShopStockOps.consume(stock, new ItemStack(Items.APPLE), 0));
    }

    @Test
    void consumeNegativeAmountReturnsTrue() {
        assertTrue(ShopStockOps.consume(stock, new ItemStack(Items.APPLE), -1));
    }

    @Test
    void consumeInsufficientReturnsFalseAndDoesNotMutate() {
        stock.set(0, new ItemStack(Items.APPLE, 3));
        assertFalse(ShopStockOps.consume(stock, new ItemStack(Items.APPLE), 5));
        assertEquals(3, stock.get(0).getCount());
    }

    @Test
    void consumeExactSingleSlot() {
        stock.set(0, new ItemStack(Items.APPLE, 5));
        assertTrue(ShopStockOps.consume(stock, new ItemStack(Items.APPLE), 5));
        assertEquals(0, stock.get(0).getCount());
    }

    @Test
    void consumeAcrossMultipleSlots() {
        stock.set(0, new ItemStack(Items.APPLE, 3));
        stock.set(2, new ItemStack(Items.APPLE, 4));
        stock.set(5, new ItemStack(Items.APPLE, 10));
        assertTrue(ShopStockOps.consume(stock, new ItemStack(Items.APPLE), 12));
        // 3 from slot 0 + 4 from slot 2 + 5 from slot 5 = 12
        assertEquals(0, stock.get(0).getCount());
        assertEquals(0, stock.get(2).getCount());
        assertEquals(5, stock.get(5).getCount());
    }

    @Test
    void consumeIgnoresOtherItems() {
        stock.set(0, new ItemStack(Items.DIAMOND, 64));
        stock.set(1, new ItemStack(Items.APPLE, 5));
        assertTrue(ShopStockOps.consume(stock, new ItemStack(Items.APPLE), 5));
        assertEquals(64, stock.get(0).getCount());
        assertEquals(0, stock.get(1).getCount());
    }

    @Test
    void matchesEmptyStackReturnsFalse() {
        assertFalse(ShopStockOps.matches(ItemStack.EMPTY, new ItemStack(Items.APPLE)));
    }

    @Test
    void matchesDifferentItemReturnsFalse() {
        assertFalse(ShopStockOps.matches(new ItemStack(Items.APPLE), new ItemStack(Items.DIAMOND)));
    }

    @Test
    void matchesBothNoNbtReturnsTrue() {
        assertTrue(ShopStockOps.matches(new ItemStack(Items.APPLE, 1), new ItemStack(Items.APPLE)));
    }

    @Test
    void matchesOneSideNbtReturnsFalse() {
        ItemStack a = new ItemStack(Items.APPLE);
        ItemStack b = new ItemStack(Items.APPLE);
        b.getOrCreateTag().putInt("X", 1);
        assertFalse(ShopStockOps.matches(a, b));
        assertFalse(ShopStockOps.matches(b, a));
    }

    @Test
    void matchesEqualNbtReturnsTrue() {
        ItemStack a = new ItemStack(Items.APPLE);
        a.getOrCreateTag().putInt("X", 42);
        ItemStack b = new ItemStack(Items.APPLE);
        b.getOrCreateTag().putInt("X", 42);
        assertTrue(ShopStockOps.matches(a, b));
    }
}
