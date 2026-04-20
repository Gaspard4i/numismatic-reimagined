package dev.gaspard4i.numismatic.shop;

import dev.gaspard4i.numismatic.testutil.McBootstrap;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RequestFulfillLogicTest {

    @BeforeAll
    static void setup() { McBootstrap.ensure(); }

    private static RequestOffer diamondOffer(int qty, long price, boolean strict) {
        return new RequestOffer(new ItemStack(Items.DIAMOND), qty, 0, price, strict);
    }

    @Test
    void emptySubmissionRejected() {
        RequestOffer o = diamondOffer(10, 100, false);
        assertFalse(RequestFulfillLogic.matches(ItemStack.EMPTY, o));
    }

    @Test
    void wrongItemRejected() {
        RequestOffer o = diamondOffer(10, 100, false);
        assertFalse(RequestFulfillLogic.matches(new ItemStack(Items.IRON_INGOT), o));
    }

    @Test
    void strictNbtRequiresEqualTags() {
        ItemStack tpl = new ItemStack(Items.DIAMOND);
        CompoundTag tag = tpl.getOrCreateTag();
        tag.putString("Custom", "A");
        RequestOffer o = new RequestOffer(tpl, 10, 0, 100, true);

        ItemStack submittedMatching = tpl.copy();
        assertTrue(RequestFulfillLogic.matches(submittedMatching, o));

        ItemStack submittedDiff = new ItemStack(Items.DIAMOND);
        submittedDiff.getOrCreateTag().putString("Custom", "B");
        assertFalse(RequestFulfillLogic.matches(submittedDiff, o));
    }

    @Test
    void looseNbtIgnoresTags() {
        ItemStack tpl = new ItemStack(Items.DIAMOND);
        tpl.getOrCreateTag().putString("X", "Y");
        RequestOffer o = new RequestOffer(tpl, 10, 0, 100, false);
        assertTrue(RequestFulfillLogic.matches(new ItemStack(Items.DIAMOND), o));
    }

    @Test
    void damageableAtMaxDurabilityAccepted() {
        RequestOffer o = new RequestOffer(new ItemStack(Items.DIAMOND_SWORD), 1, 0, 500, false);
        ItemStack sword = new ItemStack(Items.DIAMOND_SWORD);
        assertTrue(RequestFulfillLogic.matches(sword, o));
    }

    @Test
    void damageableUsedRejected() {
        RequestOffer o = new RequestOffer(new ItemStack(Items.DIAMOND_SWORD), 1, 0, 500, false);
        ItemStack sword = new ItemStack(Items.DIAMOND_SWORD);
        sword.setDamageValue(100);
        assertFalse(RequestFulfillLogic.matches(sword, o));
    }

    @Test
    void fulfillReturnsNoneForNoMatch() {
        RequestOffer o = diamondOffer(10, 100, false);
        RequestFulfillLogic.FulfillResult r =
                RequestFulfillLogic.fulfill(new ItemStack(Items.IRON_INGOT), o, 10_000);
        assertTrue(r.isNone());
    }

    @Test
    void fulfillBoundedByNeed() {
        RequestOffer o = diamondOffer(3, 100, false);
        ItemStack submission = new ItemStack(Items.DIAMOND, 10);
        RequestFulfillLogic.FulfillResult r =
                RequestFulfillLogic.fulfill(submission, o, 10_000);
        assertEquals(3, r.consumed());
        assertEquals(300, r.payout());
        assertTrue(r.updatedOffer().isComplete());
    }

    @Test
    void fulfillBoundedByStack() {
        RequestOffer o = diamondOffer(10, 100, false);
        ItemStack submission = new ItemStack(Items.DIAMOND, 2);
        RequestFulfillLogic.FulfillResult r =
                RequestFulfillLogic.fulfill(submission, o, 10_000);
        assertEquals(2, r.consumed());
        assertEquals(200, r.payout());
        assertFalse(r.updatedOffer().isComplete());
    }

    @Test
    void fulfillBoundedByFund() {
        RequestOffer o = diamondOffer(10, 100, false);
        ItemStack submission = new ItemStack(Items.DIAMOND, 10);
        RequestFulfillLogic.FulfillResult r =
                RequestFulfillLogic.fulfill(submission, o, 350);
        assertEquals(3, r.consumed()); // 350 / 100 = 3
        assertEquals(300, r.payout());
    }

    @Test
    void fulfillNoneWhenFundInsufficientForOneUnit() {
        RequestOffer o = diamondOffer(10, 100, false);
        RequestFulfillLogic.FulfillResult r =
                RequestFulfillLogic.fulfill(new ItemStack(Items.DIAMOND, 5), o, 99);
        assertTrue(r.isNone());
    }

    @Test
    void fulfillNoneWhenOfferAlreadyComplete() {
        RequestOffer complete = new RequestOffer(new ItemStack(Items.DIAMOND), 5, 5, 100, false);
        RequestFulfillLogic.FulfillResult r =
                RequestFulfillLogic.fulfill(new ItemStack(Items.DIAMOND, 10), complete, 10_000);
        assertTrue(r.isNone());
    }
}
