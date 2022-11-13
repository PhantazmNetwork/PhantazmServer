package com.github.phantazmnetwork.zombies.player;

import com.github.phantazmnetwork.zombies.map.Flaggable;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.Optional;

public class BasicZombiesPlayer implements ZombiesPlayer {

    private final ZombiesPlayerModule module;

    public BasicZombiesPlayer(@NotNull ZombiesPlayerModule module) {
        this.module = Objects.requireNonNull(module, "module");
    }

    @Override
    public void tick(long time) {
        Optional<Player> playerOptional = module.getPlayerView().getPlayer();
        if (playerOptional.isPresent()) {
            module.getMeta().setCrouching(playerOptional.get().getPose() == Entity.Pose.SLEEPING);
        }
        else {
            module.getMeta().setCrouching(false);
        }

        module.getStateSwitcher().tick(time);
    }

    @Override
    public @NotNull ZombiesPlayerModule getModule() {
        return module;
    }

    @Override
    public long getReviveTime() {
        return 30L;// todo: fast revive
    }

    @Override
    public void start() {
        module.getStateSwitcher().start();
    }

    @Override
    public void end() {
        module.getStateSwitcher().end();
    }

    @Override
    public @NotNull Flaggable flags() {
        return module.flaggable();
    }
}
