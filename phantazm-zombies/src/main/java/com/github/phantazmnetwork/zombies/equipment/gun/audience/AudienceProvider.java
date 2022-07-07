package com.github.phantazmnetwork.zombies.equipment.gun.audience;

import net.kyori.adventure.audience.Audience;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

@FunctionalInterface
public interface AudienceProvider {

    @NotNull Optional<? extends Audience> provideAudience();

}
