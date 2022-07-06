package com.github.phantazmnetwork.zombies.equipment.gun.target.tester;

import com.github.phantazmnetwork.commons.Namespaces;
import com.github.phantazmnetwork.mob.MobStore;
import com.github.steanky.ethylene.core.ConfigElement;
import com.github.steanky.ethylene.core.collection.ConfigNode;
import com.github.steanky.ethylene.core.collection.LinkedConfigNode;
import com.github.steanky.ethylene.core.processor.ConfigProcessException;
import com.github.steanky.ethylene.core.processor.ConfigProcessor;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.key.Keyed;
import net.minestom.server.entity.Entity;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Objects;
import java.util.UUID;

public class PhantazmTargetTester implements TargetTester {

    public record Data(boolean ignorePreviousHits) implements Keyed {

        public static final Key SERIAL_KEY = Key.key(Namespaces.PHANTAZM,"gun.target_tester.phantazm");

        @Override
        public @NotNull Key key() {
            return SERIAL_KEY;
        }

    }

    public static @NotNull ConfigProcessor<Data> processor() {
        return new ConfigProcessor<>() {
            @Override
            public @NotNull Data dataFromElement(@NotNull ConfigElement element) throws ConfigProcessException {
                boolean ignorePreviousHits = element.getBooleanOrThrow("ignorePreviousHits");

                return new Data(ignorePreviousHits);
            }

            @Override
            public @NotNull ConfigElement elementFromData(@NotNull Data data) throws ConfigProcessException {
                ConfigNode node = new LinkedConfigNode(1);
                node.putBoolean("ignorePreviousHits", data.ignorePreviousHits());

                return node;
            }
        };
    }

    private final Data data;

    private final MobStore mobStore;

    public PhantazmTargetTester(@NotNull Data data, @NotNull MobStore mobStore) {
        this.data = Objects.requireNonNull(data, "data");
        this.mobStore = Objects.requireNonNull(mobStore, "mobStore");
    }

    @Override
    public boolean useTarget(@NotNull Entity target, @NotNull Collection<UUID> previousHits) {UUID uuid = target.getUuid();
        return !(data.ignorePreviousHits() && previousHits.contains(target.getUuid())) && mobStore.getMob(uuid) != null;
    }
}
