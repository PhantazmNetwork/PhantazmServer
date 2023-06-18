package org.phantazm.zombies.equipment.gun.shoot.handler;

import com.github.steanky.element.core.annotation.FactoryMethod;
import com.github.steanky.element.core.annotation.Model;
import net.minestom.server.entity.Entity;
import org.jetbrains.annotations.NotNull;
import org.phantazm.zombies.equipment.gun.Gun;
import org.phantazm.zombies.equipment.gun.GunState;
import org.phantazm.zombies.equipment.gun.shoot.GunShot;
import org.phantazm.stats.zombies.ZombiesPlayerMapStats;

import java.util.Collection;
import java.util.Objects;
import java.util.UUID;

@Model("zombies.gun.shot_handler.zombies_player_map_stats")
public class ZombiesPlayerMapStatsShotHandler implements ShotHandler {

    private final ZombiesPlayerMapStats stats;

    @FactoryMethod
    public ZombiesPlayerMapStatsShotHandler(@NotNull ZombiesPlayerMapStats stats) {
        this.stats = Objects.requireNonNull(stats, "stats");
    }

    @Override
    public void tick(@NotNull GunState state, long time) {

    }

    @Override
    public void handle(@NotNull Gun gun, @NotNull GunState state, @NotNull Entity attacker,
            @NotNull Collection<UUID> previousHits, @NotNull GunShot shot) {
        stats.setRegularShots(stats.getRegularShots() + shot.regularTargets().size());
        stats.setHeadshots(stats.getHeadshots() + shot.headshotTargets().size());
    }
}
