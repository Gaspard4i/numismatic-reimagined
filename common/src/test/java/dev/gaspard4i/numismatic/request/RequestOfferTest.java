package dev.gaspard4i.numismatic.request;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RequestOfferTest {

    @Test
    void constructorRejectsEmptyTemplate() {
        assertThrows(IllegalArgumentException.class, () -> new RequestOffer("", 10, 0, 5));
        assertThrows(IllegalArgumentException.class, () -> new RequestOffer(null, 10, 0, 5));
    }

    @Test
    void constructorRejectsNegativeRequested() {
        assertThrows(IllegalArgumentException.class, () -> new RequestOffer("apple", -1, 0, 5));
    }

    @Test
    void constructorRejectsFulfilledOutOfBounds() {
        assertThrows(IllegalArgumentException.class, () -> new RequestOffer("apple", 10, -1, 5));
        assertThrows(IllegalArgumentException.class, () -> new RequestOffer("apple", 10, 11, 5));
    }

    @Test
    void constructorRejectsNegativePrice() {
        assertThrows(IllegalArgumentException.class, () -> new RequestOffer("apple", 10, 0, -1));
    }

    @Test
    void remainingReflectsFulfilled() {
        assertEquals(7, new RequestOffer("apple", 10, 3, 5).remaining());
    }

    @Test
    void isClosedWhenFulfilledReachesRequested() {
        assertTrue(new RequestOffer("apple", 10, 10, 5).isClosed());
        assertFalse(new RequestOffer("apple", 10, 9, 5).isClosed());
    }

    @Test
    void advanceClampsToRemaining() {
        RequestOffer start = new RequestOffer("apple", 10, 3, 5);
        RequestOffer next = start.advance(20);
        assertEquals(10, next.fulfilled());
        assertTrue(next.isClosed());
    }

    @Test
    void advanceRejectsNegative() {
        RequestOffer o = new RequestOffer("apple", 10, 0, 5);
        assertThrows(IllegalArgumentException.class, () -> o.advance(-1));
    }
}
