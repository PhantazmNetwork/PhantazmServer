package org.phantazm.zombies.equipment.gun.shoot.handler;

import com.github.steanky.element.core.annotation.*;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import net.minestom.server.instance.Instance;
import org.jetbrains.annotations.NotNull;
import org.phantazm.core.particle.ParticleWrapper;
import org.phantazm.zombies.equipment.gun.Gun;
import org.phantazm.zombies.equipment.gun.GunState;
import org.phantazm.zombies.equipment.gun.shoot.GunShot;

import java.util.Collection;
import java.util.UUID;

/**
 * A {@link ShotHandler} that creates a trail of particles.
 */
@Model("zombies.gun.shot_handler.particle_trail")
@Cache
public class ParticleTrailShotHandler implements ShotHandler {
    private final Data data;
    private final ParticleWrapper wrapper;

    @FactoryMethod
    public ParticleTrailShotHandler(@NotNull Data data, @NotNull @Child("particle") ParticleWrapper wrapper) {
        this.data = data;
        this.wrapper = wrapper;
    }

    @Override
    public void handle(@NotNull Gun gun, @NotNull GunState state, @NotNull Entity attacker,
        @NotNull Collection<UUID> previousHits, @NotNull GunShot shot) {
        Instance instance = attacker.getInstance();
        if (instance == null) {
            return;
        }

        Pos start = shot.start();
        Vec direction = Vec.fromPoint(shot.end().sub(start)).normalize();
        for (int i = 0; i < data.trailCount(); i++) {
            start = start.add(direction);
            wrapper.sendTo(instance, start);
        }
    }

    @Override
    public void tick(@NotNull GunState state, long time) {

    }

    @DataObject
    public record Data(int trailCount) {
    }
}
