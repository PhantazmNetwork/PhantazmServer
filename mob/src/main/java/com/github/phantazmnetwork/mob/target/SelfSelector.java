package com.github.phantazmnetwork.mob.target;

import com.github.phantazmnetwork.commons.Namespaces;
import com.github.phantazmnetwork.core.target.TargetSelectorInstance;
import com.github.phantazmnetwork.mob.PhantazmMob;
import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

/**
 * A {@link TargetSelector} that selects itself.
 */
public class SelfSelector implements TargetSelector<PhantazmMob> {

    /**
     * The serial {@link Key} for {@link SelfSelector}s.
     */
    public static final Key SERIAL_KEY = Key.key(Namespaces.PHANTAZM, "selector.self");

    @Override
    public @NotNull TargetSelectorInstance<PhantazmMob> createSelector(@NotNull PhantazmMob mob) {
        return () -> Optional.of(mob);
    }

    @Override
    public @NotNull Key key() {
        return SERIAL_KEY;
    }
}
