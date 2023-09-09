package org.phantazm.zombies.equipment.gun.shoot.handler;

import com.github.steanky.element.core.annotation.*;
import net.minestom.server.entity.Entity;
import net.minestom.server.instance.Instance;
import org.jetbrains.annotations.NotNull;
import org.phantazm.core.particle.ParticleWrapper;
import org.phantazm.zombies.equipment.gun.Gun;
import org.phantazm.zombies.equipment.gun.GunState;
import org.phantazm.zombies.equipment.gun2.shoot.GunHit;
import org.phantazm.zombies.equipment.gun2.shoot.GunShot;

import java.util.Collection;
import java.util.UUID;

@Model("zombies.gun.shot_handler.particle_point")
@Cache
public class ParticlePointShotHandler implements ShotHandler {
    private final ParticleWrapper wrapper;

    @FactoryMethod
    public ParticlePointShotHandler(@NotNull @Child("particle") ParticleWrapper wrapper) {
        this.wrapper = wrapper;
    }

    @Override
    public void handle(@NotNull Gun gun, @NotNull GunState state, @NotNull Entity attacker,
        @NotNull Collection<UUID> previousHits, @NotNull GunShot shot) {
        Instance instance = attacker.getInstance();
        if (instance == null) {
            return;
        }

        for (GunHit hit : shot.regularTargets()) {
            wrapper.sendTo(instance, hit.location());
        }

        for (GunHit hit : shot.headshotTargets()) {
            wrapper.sendTo(instance, hit.location());
        }
    }

    @Override
    public void tick(@NotNull GunState state, long time) {

    }

    @DataObject
    public record Data(@NotNull @ChildPath("particle") String particle) {
    }
}
