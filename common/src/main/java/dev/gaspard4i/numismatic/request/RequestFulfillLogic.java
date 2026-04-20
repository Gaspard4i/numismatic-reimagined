package dev.gaspard4i.numismatic.request;

public final class RequestFulfillLogic {

    private RequestFulfillLogic() {}

    public record Outcome(RequestOffer offer, int delivered, long payout, long fundsRemaining) {}

    public static Outcome fulfill(RequestOffer offer, int available, long boardFunds) {
        if (offer == null) throw new IllegalArgumentException("offer must not be null");
        if (available < 0) throw new IllegalArgumentException("available must be non-negative");
        if (boardFunds < 0) throw new IllegalArgumentException("boardFunds must be non-negative");

        if (offer.isClosed() || available == 0 || offer.pricePerItem() == 0) {
            return new Outcome(offer, 0, 0L, boardFunds);
        }

        int canDeliver = Math.min(available, offer.remaining());
        long costCap = offer.pricePerItem() == 0 ? Long.MAX_VALUE : boardFunds / offer.pricePerItem();
        int delivered = (int) Math.min(canDeliver, costCap);
        if (delivered <= 0) {
            return new Outcome(offer, 0, 0L, boardFunds);
        }
        long payout = delivered * offer.pricePerItem();
        RequestOffer next = offer.advance(delivered);
        return new Outcome(next, delivered, payout, boardFunds - payout);
    }
}
