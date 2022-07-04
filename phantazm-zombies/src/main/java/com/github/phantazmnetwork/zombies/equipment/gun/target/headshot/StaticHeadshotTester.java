package com.github.phantazmnetwork.zombies.equipment.gun.target.headshot;

import com.github.phantazmnetwork.api.config.VariantSerializable;
import com.github.phantazmnetwork.commons.Namespaces;
import com.github.phantazmnetwork.mob.PhantazmMob;
import net.kyori.adventure.key.Key;
import net.minestom.server.coordinate.Point;
import net.minestom.server.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class StaticHeadshotTester implements HeadshotTester {

    public record Data(boolean shouldHeadshot) implements VariantSerializable {

        public static final Key SERIAL_KEY = Key.key(Namespaces.PHANTAZM,"gun.headshot_tester.static");

        @Override
        public @NotNull Key getSerialKey() {
            return SERIAL_KEY;
        }
    }

    private final Data data;

    public StaticHeadshotTester(@NotNull Data data) {
        this.data = Objects.requireNonNull(data, "data");
    }

    @Override
    public boolean isHeadshot(@NotNull Player player, @NotNull PhantazmMob mob, @NotNull Point intersection) {
        return data.shouldHeadshot();
    }

    @Override
    public @NotNull VariantSerializable getData() {
        return data;
    }
}
