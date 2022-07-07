package com.github.phantazmnetwork.zombies.equipment.gun.target.headshot;

import com.github.phantazmnetwork.commons.Namespaces;
import com.github.steanky.ethylene.core.ConfigElement;
import com.github.steanky.ethylene.core.collection.ConfigNode;
import com.github.steanky.ethylene.core.collection.LinkedConfigNode;
import com.github.steanky.ethylene.core.processor.ConfigProcessException;
import com.github.steanky.ethylene.core.processor.ConfigProcessor;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.key.Keyed;
import net.minestom.server.coordinate.Point;
import net.minestom.server.entity.Entity;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class StaticHeadshotTester implements HeadshotTester {

    public record Data(boolean shouldHeadshot) implements Keyed {

        public static final Key SERIAL_KEY = Key.key(Namespaces.PHANTAZM,"gun.headshot_tester.static");

        @Override
        public @NotNull Key key() {
            return SERIAL_KEY;
        }
    }

    public static @NotNull ConfigProcessor<Data> processor() {
        return new ConfigProcessor<>() {

            @Override
            public @NotNull Data dataFromElement(@NotNull ConfigElement element) throws ConfigProcessException {
                return new Data(element.getBooleanOrThrow("shouldHeadshot"));
            }

            @Override
            public @NotNull ConfigElement elementFromData(@NotNull Data data) {
                ConfigNode node = new LinkedConfigNode(1);
                node.putBoolean("shouldHeadshot", data.shouldHeadshot());

                return node;
            }
        };
    }

    private final Data data;

    public StaticHeadshotTester(@NotNull Data data) {
        this.data = Objects.requireNonNull(data, "data");
    }

    @Override
    public boolean isHeadshot(@NotNull Entity shooter, @NotNull Entity entity, @NotNull Point intersection) {
        return data.shouldHeadshot();
    }

}
