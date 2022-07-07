package com.github.phantazmnetwork.zombies.equipment.gun.visual;

import com.github.phantazmnetwork.commons.AdventureConfigProcessors;
import com.github.phantazmnetwork.commons.Namespaces;
import com.github.phantazmnetwork.zombies.equipment.gun.GunState;
import com.github.phantazmnetwork.zombies.equipment.gun.GunStats;
import com.github.phantazmnetwork.zombies.equipment.gun.reload.ReloadTester;
import com.github.steanky.ethylene.core.ConfigElement;
import com.github.steanky.ethylene.core.collection.ConfigNode;
import com.github.steanky.ethylene.core.collection.LinkedConfigNode;
import com.github.steanky.ethylene.core.processor.ConfigProcessException;
import com.github.steanky.ethylene.core.processor.ConfigProcessor;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.key.Keyed;
import net.minestom.server.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Objects;
import java.util.function.BiConsumer;

public class ReloadStackMapper implements GunStackMapper {

    public record Data(@NotNull Key statsKey, @NotNull Key reloadTesterKey) implements Keyed {

        public static final Key SERIAL_KEY = Key.key(Namespaces.PHANTAZM, "gun.stack_mapper.reload.durability");

        public Data {
            Objects.requireNonNull(statsKey, "statsKey");
            Objects.requireNonNull(reloadTesterKey, "reloadTesterKey");
        }

        @Override
        public @NotNull Key key() {
            return SERIAL_KEY;
        }
    }

    public static @NotNull ConfigProcessor<Data> processor() {
        ConfigProcessor<Key> keyProcessor = AdventureConfigProcessors.key();

        return new ConfigProcessor<>() {

            @Override
            public @NotNull Data dataFromElement(@NotNull ConfigElement element) throws ConfigProcessException {
                Key statsKey = keyProcessor.dataFromElement(element.getElementOrThrow("statsKey"));
                Key reloadTesterKey = keyProcessor.dataFromElement(element.getElementOrThrow("reloadTesterKey"));

                return new Data(statsKey, reloadTesterKey);
            }

            @Override
            public @NotNull ConfigElement elementFromData(@NotNull Data data) throws ConfigProcessException {
                ConfigNode node = new LinkedConfigNode(2);
                node.put("statsKey", keyProcessor.elementFromData(data.statsKey()));
                node.put("reloadTesterKey", keyProcessor.elementFromData(data.reloadTesterKey()));

                return node;
            }
        };
    }

    public static @NotNull BiConsumer<Data, Collection<Key>> dependencyConsumer() {
        return (data, keys) -> {
            keys.add(data.statsKey());
            keys.add(data.reloadTesterKey());
        };
    }

    private final GunStats stats;

    private final ReloadTester reloadTester;

    public ReloadStackMapper(@NotNull GunStats stats, @NotNull ReloadTester reloadTester) {
        this.stats = Objects.requireNonNull(stats, "stats");
        this.reloadTester = Objects.requireNonNull(reloadTester, "reloadTester");
    }

    @Override
    public @NotNull ItemStack map(@NotNull GunState state, @NotNull ItemStack intermediate) {
        if (reloadTester.isReloading(state)) {
            long reloadSpeed = stats.reloadSpeed();
            int maxDamage = intermediate.material().registry().maxDamage();
            int damage = maxDamage - (int) (maxDamage * ((double) state.ticksSinceLastReload() / reloadSpeed));

            return intermediate.withMeta(builder -> builder.damage(damage));
        }

        return intermediate;
    }

}