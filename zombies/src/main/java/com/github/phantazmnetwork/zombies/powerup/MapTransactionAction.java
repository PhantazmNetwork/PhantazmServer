package com.github.phantazmnetwork.zombies.powerup;

import com.github.phantazmnetwork.zombies.coin.Transaction;
import com.github.phantazmnetwork.zombies.coin.TransactionModifierSource;
import com.github.phantazmnetwork.zombies.player.ZombiesPlayer;
import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.NotNull;

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
    public void activate(@NotNull ZombiesPlayer player, long time) {
        super.activate(player, time);
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
