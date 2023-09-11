package org.phantazm.core.scene2;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;
import org.phantazm.core.player.PlayerView;

import java.util.Collection;
import java.util.Objects;
import java.util.Set;

/**
 * Simple abstract implementation of {@link Join}. Maintains an immutable set of joining players, and the type of the
 * scene this Join instance should target.
 *
 * @param <T> the type of scene to join
 */
public abstract class JoinAbstract<T extends Scene> implements Join<T> {
    private final Set<PlayerView> players;
    private final Class<T> targetType;

    /**
     * Creates a new instance of this class. {@code players} will be used to create an immutable set, which will remove
     * duplicate elements.
     *
     * @param players    the players participating in this join
     * @param targetType the type of scene to join
     */
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
