package com.github.phantazmnetwork.zombies.equipment.gun.shot.handler;

import com.github.phantazmnetwork.commons.Namespaces;
import com.github.phantazmnetwork.mob.PhantazmMob;
import com.github.phantazmnetwork.zombies.equipment.gun.Gun;
import com.github.phantazmnetwork.zombies.equipment.gun.shot.GunShot;
import it.unimi.dsi.fastutil.Pair;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class FeedbackShotHandler implements ShotHandler {

    public static final Key SERIAL_KEY = Key.key(Namespaces.PHANTAZM, "gun.hit_handler.feedback");

    private final Component message;

    private final Component headshotMessage;

    public FeedbackShotHandler(@NotNull Component message, @NotNull Component headshotMessage) {
        this.message = Objects.requireNonNull(message, "message");
        this.headshotMessage = Objects.requireNonNull(headshotMessage, "headshotMessage");
    }

    @Override
    public void handle(@NotNull Gun gun, @NotNull Player attacker, @NotNull GunShot shot) {
        for (Pair<PhantazmMob, Vec> ignored : shot.getRegularTargets()) {
            attacker.sendMessage(message);
        }
        for (Pair<PhantazmMob, Vec> ignored : shot.getHeadshotTargets()) {
            attacker.sendMessage(headshotMessage);
        }
    }

    @Override
    public void tick(long time) {

    }

    @Override
    public @NotNull Key getSerialKey() {
        return SERIAL_KEY;
    }

}
