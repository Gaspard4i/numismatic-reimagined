package dev.gaspard4i.numismatic.advancement;

import com.google.gson.JsonObject;
import dev.gaspard4i.numismatic.NumismaticConstants;
import net.minecraft.advancements.critereon.AbstractCriterionTriggerInstance;
import net.minecraft.advancements.critereon.ContextAwarePredicate;
import net.minecraft.advancements.critereon.DeserializationContext;
import net.minecraft.advancements.critereon.SimpleCriterionTrigger;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.NotNull;

/**
 * Criterion trigger fired when a player's total accumulated currency
 * (server-wide, in bronze units) reaches the configured threshold. See
 * {@link dev.gaspard4i.numismatic.currency.AccumulationTracker}.
 */
public class CollectNetheriteTrigger extends SimpleCriterionTrigger<CollectNetheriteTrigger.TriggerInstance> {

    public static final ResourceLocation ID =
            new ResourceLocation(NumismaticConstants.MOD_ID, "accumulate_netherite");

    @NotNull
    @Override
    public ResourceLocation getId() { return ID; }

    @NotNull
    @Override
    protected TriggerInstance createInstance(JsonObject json,
                                             @NotNull ContextAwarePredicate predicate,
                                             @NotNull DeserializationContext ctx) {
        long threshold = json.has("threshold") ? json.get("threshold").getAsLong() : 0L;
        return new TriggerInstance(predicate, threshold);
    }

    /** Fire with the player's current accumulated total (bronze). */
    public void trigger(ServerPlayer player, long accumulated) {
        this.trigger(player, inst -> inst.matches(accumulated));
    }

    public static class TriggerInstance extends AbstractCriterionTriggerInstance {
        private final long threshold;

        public TriggerInstance(ContextAwarePredicate predicate, long threshold) {
            super(ID, predicate);
            this.threshold = threshold;
        }

        public boolean matches(long accumulated) {
            return accumulated >= threshold;
        }
    }
}
