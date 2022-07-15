package com.github.phantazmnetwork.mob.goal;

import com.github.phantazmnetwork.commons.Namespaces;
import com.github.phantazmnetwork.mob.PhantazmMob;
import com.github.phantazmnetwork.mob.target.TargetSelector;
import net.kyori.adventure.key.Key;
import net.minestom.server.entity.Player;
import org.jetbrains.annotations.NotNull;

/**
 * A {@link Goal} that makes a {@link PhantazmMob} follow {@link Player}s.
 */
public class FollowPlayerGoal extends FollowEntityGoal<Player> {

    /**
     * The serial {@link Key} for {@link FollowPlayerGoal}s.
     */
    public static final Key SERIAL_KEY = Key.key(Namespaces.PHANTAZM, "goal.follow_player");

    /**
     * Creates a new {@link FollowPlayerGoal}.
     *
     * @param selector The {@link TargetSelector} used to select {@link Player}s
     */
    public FollowPlayerGoal(@NotNull TargetSelector<Player> selector) {
        super(selector);
    }

    @Override
    public @NotNull Key key() {
        return SERIAL_KEY;
    }
}
