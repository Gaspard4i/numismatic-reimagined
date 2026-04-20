package dev.gaspard4i.numismatic.request;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class RequestFulfillLogicTest {

    @Test
    void fulfillDeliversAllAvailableWhenFundsSuffice() {
        RequestOffer offer = new RequestOffer("apple", 10, 0, 5);
        RequestFulfillLogic.Outcome out = RequestFulfillLogic.fulfill(offer, 7, 1000);
        assertEquals(7, out.delivered());
        assertEquals(35, out.payout());
        assertEquals(965, out.fundsRemaining());
        assertEquals(7, out.offer().fulfilled());
    }

    @Test
    void fulfillClampsToOfferRemaining() {
        RequestOffer offer = new RequestOffer("apple", 5, 3, 2);
        RequestFulfillLogic.Outcome out = RequestFulfillLogic.fulfill(offer, 100, 1000);
        assertEquals(2, out.delivered());
        assertEquals(4, out.payout());
    }

    @Test
    void fulfillClampsByFunds() {
        RequestOffer offer = new RequestOffer("apple", 100, 0, 10);
        RequestFulfillLogic.Outcome out = RequestFulfillLogic.fulfill(offer, 100, 25);
        assertEquals(2, out.delivered());
        assertEquals(20, out.payout());
        assertEquals(5, out.fundsRemaining());
    }

    @Test
    void fulfillReturnsZeroWhenOfferClosed() {
        RequestOffer offer = new RequestOffer("apple", 5, 5, 1);
        RequestFulfillLogic.Outcome out = RequestFulfillLogic.fulfill(offer, 10, 1000);
        assertEquals(0, out.delivered());
        assertEquals(1000, out.fundsRemaining());
    }

    @Test
    void fulfillReturnsZeroOnZeroAvailable() {
        RequestOffer offer = new RequestOffer("apple", 10, 0, 5);
        RequestFulfillLogic.Outcome out = RequestFulfillLogic.fulfill(offer, 0, 1000);
        assertEquals(0, out.delivered());
    }

    @Test
    void fulfillReturnsZeroOnZeroPrice() {
        RequestOffer offer = new RequestOffer("apple", 10, 0, 0);
        RequestFulfillLogic.Outcome out = RequestFulfillLogic.fulfill(offer, 5, 1000);
        assertEquals(0, out.delivered());
    }

    @Test
    void fulfillRejectsNulls() {
        assertThrows(IllegalArgumentException.class,
                () -> RequestFulfillLogic.fulfill(null, 10, 1000));
    }

    @Test
    void fulfillRejectsNegativeArgs() {
        RequestOffer offer = new RequestOffer("apple", 10, 0, 5);
        assertThrows(IllegalArgumentException.class,
                () -> RequestFulfillLogic.fulfill(offer, -1, 1000));
        assertThrows(IllegalArgumentException.class,
                () -> RequestFulfillLogic.fulfill(offer, 10, -1));
    }
}
