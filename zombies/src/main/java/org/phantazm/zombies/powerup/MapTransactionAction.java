package org.phantazm.zombies.powerup;

import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.NotNull;
import org.phantazm.zombies.coin.Transaction;
import org.phantazm.zombies.coin.TransactionModifierSource;
import org.phantazm.zombies.player.ZombiesPlayer;

import java.util.Objects;

public abstract class MapTransactionAction extends PowerupActionBase {
    private final TransactionModifierSource transactionModifierSource;
    private final Key modifierGroup;

    private Transaction.Modifier activeModifier;

    public MapTransactionAction(@NotNull DeactivationPredicate deactivationPredicate,
            @NotNull TransactionModifierSource transactionModifierSource, @NotNull Key modifierGroup) {
        super(deactivationPredicate);
        this.transactionModifierSource = Objects.requireNonNull(transactionModifierSource, "transactionModifierSource");
        this.modifierGroup = Objects.requireNonNull(modifierGroup, "modifierGroup");
    }

    @Override
    public void activate(@NotNull Powerup powerup, @NotNull ZombiesPlayer player, long time) {
        super.activate(powerup, player, time);
        transactionModifierSource.addModifier(modifierGroup, activeModifier = getModifier());
    }

    @Override
    public void deactivate(@NotNull ZombiesPlayer player) {
        if (activeModifier != null) {
            transactionModifierSource.removeModifier(modifierGroup, activeModifier);
        }
    }

    protected abstract Transaction.@NotNull Modifier getModifier();
}
