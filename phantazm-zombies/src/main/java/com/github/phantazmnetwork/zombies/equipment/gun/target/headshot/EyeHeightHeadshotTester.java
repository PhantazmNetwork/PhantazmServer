package com.github.phantazmnetwork.zombies.equipment.gun.target.headshot;

import com.github.phantazmnetwork.commons.Namespaces;
import com.github.phantazmnetwork.mob.PhantazmMob;
import com.github.steanky.ethylene.core.ConfigElement;
import com.github.steanky.ethylene.core.collection.LinkedConfigNode;
import com.github.steanky.ethylene.core.processor.ConfigProcessException;
import com.github.steanky.ethylene.core.processor.ConfigProcessor;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.key.Keyed;
import net.minestom.server.coordinate.Point;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class EyeHeightHeadshotTester implements HeadshotTester {

    public record Data() implements Keyed {

        public static final Key SERIAL_KEY = Key.key(Namespaces.PHANTAZM,"gun.headshot_tester.eye_height");

        @Override
        public @NotNull Key key() {
            return SERIAL_KEY;
        }
    }

    public static @NotNull ConfigProcessor<Data> processor() {
        return new ConfigProcessor<>() {

            @Override
            public @NotNull Data dataFromElement(@NotNull ConfigElement element) throws ConfigProcessException {
                return new Data();
            }

            @Override
            public @NotNull ConfigElement elementFromData(@NotNull Data data) throws ConfigProcessException {
                return new LinkedConfigNode(0);
            }
        };
    }

    private final Data data;

    public EyeHeightHeadshotTester(@NotNull Data data) {
        this.data = Objects.requireNonNull(data, "data");
    }

    @Override
    public boolean isHeadshot(@NotNull Player player, @NotNull PhantazmMob mob, @NotNull Point intersection) {
        Entity entity = mob.entity();
        return intersection.y() >= entity.getPosition().y() + entity.getEyeHeight();
    }

    @Override
    public @NotNull Keyed getData() {
        return data;
    }

}
