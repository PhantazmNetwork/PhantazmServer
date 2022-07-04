package com.github.phantazmnetwork.zombies.equipment.gun.shoot.handler;

import net.kyori.adventure.key.Keyed;
import com.github.phantazmnetwork.commons.Namespaces;
import com.github.phantazmnetwork.mob.PhantazmMob;
import com.github.phantazmnetwork.zombies.equipment.gun.shoot.GunHit;
import com.github.phantazmnetwork.zombies.equipment.gun.shoot.GunShot;
import com.github.phantazmnetwork.zombies.equipment.gun.GunState;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.minestom.server.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Objects;

public class FeedbackShotHandler implements ShotHandler {

    public record Data(@NotNull Component message, @NotNull Component headshotMessage) implements Keyed {

        public static final Key SERIAL_KEY = Key.key(Namespaces.PHANTAZM, "gun.hit_handler.feedback");

        @Override
        public @NotNull Key key() {
            return SERIAL_KEY;
        }
    }

    private final Data data;

    public FeedbackShotHandler(@NotNull Data data) {
        this.data = Objects.requireNonNull(data, "data");
    }

    @Override
    public void handle(@NotNull GunState state, @NotNull Player attacker, @NotNull Collection<PhantazmMob> previousHits, @NotNull GunShot shot) {
        for (GunHit ignored : shot.regularTargets()) {
            attacker.sendMessage(data.message());
        }
        for (GunHit ignored : shot.headshotTargets()) {
            attacker.sendMessage(data.headshotMessage());
        }
    }

    @Override
    public void tick(@NotNull GunState state, long time) {

    }

    @Override
    public @NotNull Keyed getData() {
        return data;
    }


}
