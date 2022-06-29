package com.github.phantazmnetwork.mob.goal;

import com.github.phantazmnetwork.commons.Namespaces;
import com.github.phantazmnetwork.mob.target.TargetSelector;
import net.kyori.adventure.key.Key;
import net.minestom.server.entity.Player;
import org.jetbrains.annotations.NotNull;

public class FollowPlayerGoal extends FollowEntityGoal<Player> {

    public static final Key SERIAL_KEY = Key.key(Namespaces.PHANTAZM, "goal.follow_player");

    public FollowPlayerGoal(@NotNull TargetSelector<Player> selectorCreator) {
        super(selectorCreator);
    }

    @Override
    public @NotNull Key key() {
        return SERIAL_KEY;
    }
}
