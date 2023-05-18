package org.phantazm.zombies.equipment.gun.audience;

import net.kyori.adventure.audience.Audience;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

/**
 * Provides an {@link Audience}.
 */
@FunctionalInterface
public interface AudienceProvider {

    /**
     * Provides an {@link Audience}.
     *
     * @return An {@link Optional} of an {@link Audience}
     */
    @NotNull Optional<? extends Audience> provideAudience();

}
