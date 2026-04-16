package dev.gaspard4i.numismatic.currency;

/**
 * Interface for player currency storage (the "purse").
 * Stores a single long value representing the total bronze value.
 */
public interface CurrencyStorage {

    /**
     * @return the current balance in bronze units
     */
    long getValue();

    /**
     * Sets the balance to an exact value.
     *
     * @param value the new balance in bronze units
     * @throws IllegalArgumentException if value is negative
     */
    void setValue(long value);

    /**
     * Adds the given amount to the current balance.
     *
     * @param amount the amount to add (must be positive)
     * @return the new balance
     * @throws IllegalArgumentException if amount is negative or would cause overflow
     */
    long add(long amount);

    /**
     * Subtracts the given amount from the current balance.
     *
     * @param amount the amount to subtract (must be positive)
     * @return true if the subtraction was successful (enough funds)
     */
    boolean subtract(long amount);

    /**
     * @param amount the amount to check
     * @return true if the balance is at least the given amount
     */
    boolean canAfford(long amount);
}
