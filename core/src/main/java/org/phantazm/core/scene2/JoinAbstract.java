package org.phantazm.core.scene2;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;
import org.phantazm.core.player.PlayerView;

import java.util.Collection;
import java.util.Objects;
import java.util.Set;

public abstract class JoinAbstract<T extends Scene> implements Join<T> {
    private final Set<@NotNull PlayerView> players;
    private final Class<T> targetType;

    public JoinAbstract(@NotNull Collection<@NotNull PlayerView> players, @NotNull Class<T> targetType) {
        this.players = Set.copyOf(players);
        this.targetType = Objects.requireNonNull(targetType);
    }

    @Override
    public final @NotNull @Unmodifiable Set<@NotNull PlayerView> players() {
        return players;
    }

    @Override
    public final @NotNull Class<T> targetType() {
        return targetType;
    }
}
