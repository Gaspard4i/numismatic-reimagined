package dev.gaspard4i.numismatic.request;

/**
 * Describes an open bounty in a request board :
 * "I want `requested` units of item `templateId` (key), I will pay `pricePerItem` per unit."
 * `fulfilled` tracks how many have been delivered.
 */
public record RequestOffer(
        String templateId,
        int requested,
        int fulfilled,
        long pricePerItem
) {
    public RequestOffer {
        if (templateId == null || templateId.isEmpty()) {
            throw new IllegalArgumentException("templateId must be non-empty");
        }
        if (requested < 0) throw new IllegalArgumentException("requested must be non-negative");
        if (fulfilled < 0 || fulfilled > requested) {
            throw new IllegalArgumentException("fulfilled must be in [0, requested]");
        }
        if (pricePerItem < 0) throw new IllegalArgumentException("pricePerItem must be non-negative");
    }

    public int remaining() { return requested - fulfilled; }
    public boolean isClosed() { return fulfilled >= requested; }

    public RequestOffer advance(int delivered) {
        if (delivered < 0) throw new IllegalArgumentException("delivered must be non-negative");
        int capped = Math.min(delivered, remaining());
        return new RequestOffer(templateId, requested, fulfilled + capped, pricePerItem);
    }
}
