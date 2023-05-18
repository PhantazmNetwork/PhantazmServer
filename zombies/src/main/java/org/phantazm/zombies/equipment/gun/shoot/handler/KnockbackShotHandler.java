package org.phantazm.zombies.equipment.gun.shoot.handler;

import com.github.steanky.element.core.annotation.Cache;
import com.github.steanky.element.core.annotation.DataObject;
import com.github.steanky.element.core.annotation.FactoryMethod;
import com.github.steanky.element.core.annotation.Model;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import org.jetbrains.annotations.NotNull;
import org.phantazm.zombies.equipment.gun.Gun;
import org.phantazm.zombies.equipment.gun.GunState;
import org.phantazm.zombies.equipment.gun.shoot.GunHit;
import org.phantazm.zombies.equipment.gun.shoot.GunShot;

import java.util.Collection;
import java.util.Objects;
import java.util.UUID;

/**
 * A {@link ShotHandler} which applies knocback to {@link Entity}s.
 */
@Model("zombies.gun.shot_handler.knockback")
@Cache
public class KnockbackShotHandler implements ShotHandler {

    private final Data data;

    /**
     * Creates a {@link KnockbackShotHandler}.
     *
     * @param data The {@link KnockbackShotHandler}'s {@link Data}
     */
    @FactoryMethod
    public KnockbackShotHandler(@NotNull Data data) {
        this.data = Objects.requireNonNull(data, "data");
    }

    @Override
    public void handle(@NotNull Gun gun, @NotNull GunState state, @NotNull Entity attacker,
            @NotNull Collection<UUID> previousHits, @NotNull GunShot shot) {
        Pos start = attacker.getPosition().add(0, attacker.getEyeHeight(), 0);
        for (GunHit target : shot.regularTargets()) {
            Entity entity = target.entity();
            Vec knockbackVec = target.location().sub(start).normalize().mul(data.knockback());
            entity.setVelocity(entity.getVelocity().add(knockbackVec));
        }
        for (GunHit target : shot.headshotTargets()) {
            Entity entity = target.entity();
            Vec knockbackVec = target.location().sub(start).normalize().mul(data.headshotKnockback());
            entity.setVelocity(entity.getVelocity().add(knockbackVec));
        }
    }

    @Override
    public void tick(@NotNull GunState state, long time) {

    }

    /**
     * Data for a {@link KnockbackShotHandler}.
     *
     * @param knockback         The knockback to apply to regular targets
     * @param headshotKnockback The knockback to apply to headshots
     */
    @DataObject
    public record Data(double knockback, double headshotKnockback) {

    }

}
