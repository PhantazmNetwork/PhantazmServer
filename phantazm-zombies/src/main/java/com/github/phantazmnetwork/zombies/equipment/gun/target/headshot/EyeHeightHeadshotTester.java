package com.github.phantazmnetwork.zombies.equipment.gun.target.headshot;

import com.github.phantazmnetwork.commons.Namespaces;
import com.github.steanky.ethylene.core.processor.ConfigProcessor;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.key.Keyed;
import net.minestom.server.coordinate.Point;
import net.minestom.server.entity.Entity;
import org.jetbrains.annotations.NotNull;

public class EyeHeightHeadshotTester implements HeadshotTester {

    public record Data() implements Keyed {

        public static final Key SERIAL_KEY = Key.key(Namespaces.PHANTAZM,"gun.headshot_tester.eye_height");

        @Override
        public @NotNull Key key() {
            return SERIAL_KEY;
        }
    }

    public static @NotNull ConfigProcessor<Data> processor() {
        return ConfigProcessor.emptyProcessor(Data::new);
    }

    @Override
    public boolean isHeadshot(@NotNull Entity shooter, @NotNull Entity entity, @NotNull Point intersection) {
        return intersection.y() >= entity.getPosition().y() + entity.getEyeHeight();
    }

}
