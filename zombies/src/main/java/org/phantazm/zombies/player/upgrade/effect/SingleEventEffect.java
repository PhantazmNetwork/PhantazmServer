package org.phantazm.zombies.player.upgrade.effect;

import org.jetbrains.annotations.NotNull;
import org.phantazm.zombies.player.ZombiesPlayer;
import org.phantazm.zombies.player.upgrade.PlayerUpgrade;
import org.phantazm.zombies.player.upgrade.trigger.TriggerData;

import java.util.Objects;

public abstract class SingleEventEffect<T> implements UpgradeEffect {
    private final Class<T> type;

    protected SingleEventEffect(Class<T> type) {
        this.type = Objects.requireNonNull(type);
    }

    @Override
    public final void apply(@NotNull PlayerUpgrade upgrade, @NotNull ZombiesPlayer zombiesPlayer,
        @NotNull TriggerData triggerData) {
        triggerData.consume(type, event -> applyEvent(upgrade, zombiesPlayer, triggerData, event));
    }

    protected abstract void applyEvent(@NotNull PlayerUpgrade upgrade, @NotNull ZombiesPlayer zombiesPlayer,
        @NotNull TriggerData triggerData, @NotNull T t);
}
