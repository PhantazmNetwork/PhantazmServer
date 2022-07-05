package com.github.phantazmnetwork.zombies.equipment.gun.shoot.endpoint;

import com.github.phantazmnetwork.api.player.PlayerView;
import com.github.phantazmnetwork.commons.AdventureConfigProcessors;
import com.github.phantazmnetwork.commons.Namespaces;
import com.github.steanky.ethylene.core.ConfigElement;
import com.github.steanky.ethylene.core.collection.ConfigNode;
import com.github.steanky.ethylene.core.collection.LinkedConfigNode;
import com.github.steanky.ethylene.core.processor.ConfigProcessException;
import com.github.steanky.ethylene.core.processor.ConfigProcessor;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.key.Keyed;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.instance.Instance;
import net.minestom.server.utils.block.BlockIterator;
import org.jetbrains.annotations.NotNull;

import java.util.Iterator;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

public class BasicShotEndpointSelector implements ShotEndpointSelector {

    public record Data(@NotNull Key blockIterationKey, int maxDistance) implements Keyed {

        public static final Key SERIAL_KEY = Key.key(Namespaces.PHANTAZM, "selector.shot.end.basic");

        @Override
        public @NotNull Key key() {
            return SERIAL_KEY;
        }
    }

    public static @NotNull ConfigProcessor<Data> processor(@NotNull Collection<Key> requested) {
        Objects.requireNonNull(requested, "requested");

        ConfigProcessor<Key> keyProcessor = AdventureConfigProcessors.key();
        return new ConfigProcessor<>() {

            @Override
            public @NotNull Data dataFromElement(@NotNull ConfigElement element) throws ConfigProcessException {
                Key blockIterationKey = keyProcessor.dataFromElement(element.getElementOrThrow("blockIterationKey"));
                requested.add(blockIterationKey);

                int maxDistance = element.getNumberOrThrow("maxDistance").intValue();
                return new Data(blockIterationKey, maxDistance);
            }

            @Override
            public @NotNull ConfigElement elementFromData(@NotNull Data data) throws ConfigProcessException {
                ConfigNode node = new LinkedConfigNode(2);
                node.put("blockIterationKey", keyProcessor.elementFromData(data.blockIterationKey()));
                node.putNumber("maxDistance", data.maxDistance());
                return node;
            }
        };
    }

    private final Data data;

    private final PlayerView playerView;

    private final BlockIteration blockIteration;

    public BasicShotEndpointSelector(@NotNull Data data, @NotNull PlayerView playerView,
                                     @NotNull BlockIteration blockIteration) {
        this.data = Objects.requireNonNull(data, "data");
        this.playerView = Objects.requireNonNull(playerView, "playerView");
        this.blockIteration = Objects.requireNonNull(blockIteration, "blockIteration");
    }

    @SuppressWarnings("UnstableApiUsage")
    @Override
    public @NotNull Optional<Point> getEnd(@NotNull Pos start) {
        return playerView.getPlayer().map(player -> {
            Instance instance = player.getInstance();
            if (instance == null) {
                return null;
            }

            Iterator<Point> it = new BlockIterator(start, 0, data.maxDistance());
            return blockIteration.findEnd(instance, player, start, it).orElse(null);
        });
    }

    @Override
    public @NotNull Keyed getData() {
        return data;
    }
}
