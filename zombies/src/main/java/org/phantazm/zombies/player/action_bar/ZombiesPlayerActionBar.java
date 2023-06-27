package org.phantazm.zombies.player.action_bar;

import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;
import org.phantazm.commons.Tickable;
import org.phantazm.core.player.PlayerView;

import java.util.Objects;

public class ZombiesPlayerActionBar implements Tickable {

    private final PlayerView playerView;

    private Component message = null;

    private int highestPriority = Integer.MIN_VALUE;

    public ZombiesPlayerActionBar(@NotNull PlayerView playerView) {
        this.playerView = Objects.requireNonNull(playerView, "playerView");
    }

    @Override
    public void tick(long time) {
        if (message != null) {
            playerView.getPlayer().ifPresent(player -> {
                player.sendActionBar(message);
            });
        }

        message = null;
        highestPriority = Integer.MIN_VALUE;
    }

    public void sendActionBar(@NotNull Component component, int priority) {
        Objects.requireNonNull(component, "component");
        if (priority > highestPriority) {
            message = component;
            highestPriority = priority;
        }
    }

}
