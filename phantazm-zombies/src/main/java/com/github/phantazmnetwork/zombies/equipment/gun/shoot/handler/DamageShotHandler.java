package com.github.phantazmnetwork.zombies.equipment.gun.shoot.handler;

import com.github.phantazmnetwork.api.config.VariantSerializable;
import com.github.phantazmnetwork.commons.Namespaces;
import com.github.phantazmnetwork.mob.PhantazmMob;
import com.github.phantazmnetwork.zombies.equipment.gun.shoot.GunHit;
import com.github.phantazmnetwork.zombies.equipment.gun.shoot.GunShot;
import com.github.phantazmnetwork.zombies.equipment.gun.GunState;
import net.kyori.adventure.key.Key;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.damage.DamageType;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Objects;

public class DamageShotHandler implements ShotHandler {

    public record Data(float damage, float headshotDamage) implements VariantSerializable {

        public static final Key SERIAL_KEY = Key.key(Namespaces.PHANTAZM, "gun.hit_handler.damage");

        @Override
        public @NotNull Key getSerialKey() {
            return SERIAL_KEY;
        }
    }

    private final Data data;

    public DamageShotHandler(@NotNull Data data) {
        this.data = Objects.requireNonNull(data, "data");
    }

    @Override
    public void handle(@NotNull GunState state, @NotNull Player attacker, @NotNull Collection<PhantazmMob> previousHits, @NotNull GunShot shot) {
        for (GunHit target : shot.regularTargets()) {
            target.mob().entity().damage(DamageType.fromPlayer(attacker), data.damage());
        }
        for (GunHit target : shot.headshotTargets()) {
            target.mob().entity().damage(DamageType.fromPlayer(attacker), data.headshotDamage());
        }
    }

    @Override
    public void tick(@NotNull GunState state, long time) {

    }

    @Override
    public @NotNull VariantSerializable getData() {
        return data;
    }

}
