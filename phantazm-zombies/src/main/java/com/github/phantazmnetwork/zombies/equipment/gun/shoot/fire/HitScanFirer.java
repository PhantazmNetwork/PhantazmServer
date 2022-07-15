package com.github.phantazmnetwork.zombies.equipment.gun.shoot.fire;

import com.github.phantazmnetwork.commons.AdventureConfigProcessors;
import com.github.phantazmnetwork.commons.Namespaces;
import com.github.phantazmnetwork.zombies.equipment.gun.GunState;
import com.github.phantazmnetwork.zombies.equipment.gun.shoot.GunHit;
import com.github.phantazmnetwork.zombies.equipment.gun.shoot.GunShot;
import com.github.phantazmnetwork.zombies.equipment.gun.shoot.endpoint.ShotEndpointSelector;
import com.github.phantazmnetwork.zombies.equipment.gun.shoot.handler.ShotHandler;
import com.github.phantazmnetwork.zombies.equipment.gun.target.TargetFinder;
import com.github.steanky.ethylene.core.ConfigElement;
import com.github.steanky.ethylene.core.collection.ConfigNode;
import com.github.steanky.ethylene.core.collection.LinkedConfigNode;
import com.github.steanky.ethylene.core.processor.ConfigProcessException;
import com.github.steanky.ethylene.core.processor.ConfigProcessor;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.key.Keyed;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Entity;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

/**
 * A {@link Firer} that fires using "hit scan". Targets will be immediately found and shot.
 */
public class HitScanFirer implements Firer {

    private final Supplier<Optional<? extends Entity>> entitySupplier;
    private final ShotEndpointSelector endSelector;
    private final TargetFinder targetFinder;
    private final Collection<ShotHandler> shotHandlers;

    /**
     * Creates a {@link HitScanFirer}.
     *
     * @param entitySupplier A {@link Supplier} for the {@link Entity} shooter
     * @param endSelector    The {@link HitScanFirer}'s {@link ShotEndpointSelector}
     * @param targetFinder   The {@link HitScanFirer}'s {@link TargetFinder}
     * @param shotHandlers   The {@link HitScanFirer}'s {@link ShotHandler}s
     */
    public HitScanFirer(@NotNull Supplier<Optional<? extends Entity>> entitySupplier,
                        @NotNull ShotEndpointSelector endSelector, @NotNull TargetFinder targetFinder,
                        @NotNull Collection<ShotHandler> shotHandlers) {
        this.entitySupplier = Objects.requireNonNull(entitySupplier, "entitySupplier");
        this.endSelector = Objects.requireNonNull(endSelector, "endSelector");
        this.targetFinder = Objects.requireNonNull(targetFinder, "targetFinder");
        this.shotHandlers = List.copyOf(shotHandlers);
    }

    /**
     * Creates a {@link ConfigProcessor} for {@link Data}s.
     *
     * @return A {@link ConfigProcessor} for {@link Data}s
     */
    public static @NotNull ConfigProcessor<Data> processor() {
        ConfigProcessor<Key> keyProcessor = AdventureConfigProcessors.key();
        ConfigProcessor<Collection<Key>> collectionProcessor = keyProcessor.collectionProcessor();

        return new ConfigProcessor<>() {

            @Override
            public @NotNull Data dataFromElement(@NotNull ConfigElement element) throws ConfigProcessException {
                Key endSelectorKey = keyProcessor.dataFromElement(element.getElementOrThrow("endSelectorKey"));
                Key targetFinderKey = keyProcessor.dataFromElement(element.getElementOrThrow("targetFinderKey"));
                Collection<Key> shotHandlerKeys =
                        collectionProcessor.dataFromElement(element.getElementOrThrow("shotHandlerKeys"));

                return new Data(endSelectorKey, targetFinderKey, shotHandlerKeys);
            }

            @Override
            public @NotNull ConfigElement elementFromData(@NotNull Data data) throws ConfigProcessException {
                ConfigNode node = new LinkedConfigNode(3);
                node.put("endSelectorKey", keyProcessor.elementFromData(data.endSelectorKey()));
                node.put("targetFinderKey", keyProcessor.elementFromData(data.targetFinderKey()));
                node.put("shotHandlerKeys", collectionProcessor.elementFromData(data.shotHandlerKeys()));

                return node;
            }
        };
    }

    /**
     * Creates a dependency consumer for {@link Data}s.
     *
     * @return A dependency consumer for {@link Data}s
     */
    public static @NotNull BiConsumer<Data, Collection<Key>> dependencyConsumer() {
        return (data, keys) -> {
            keys.add(data.endSelectorKey());
            keys.add(data.targetFinderKey());
            keys.addAll(data.shotHandlerKeys());
        };
    }

    @Override
    public void fire(@NotNull GunState state, @NotNull Pos start, @NotNull Collection<UUID> previousHits) {
        entitySupplier.get().ifPresent(player -> {
            Optional<Point> endOptional = endSelector.getEnd(start);
            if (endOptional.isEmpty()) {
                return;
            }
            Point end = endOptional.get();

            TargetFinder.Result target = targetFinder.findTarget(player, start, end, previousHits);
            for (GunHit hit : target.regular()) {
                previousHits.add(hit.entity().getUuid());
            }
            for (GunHit hit : target.headshot()) {
                previousHits.add(hit.entity().getUuid());
            }
            for (ShotHandler shotHandler : shotHandlers) {
                shotHandler.handle(state, player, previousHits,
                                   new GunShot(start, end, target.regular(), target.headshot())
                );
            }
        });
    }

    @Override
    public void tick(@NotNull GunState state, long time) {
        for (ShotHandler handler : shotHandlers) {
            handler.tick(state, time);
        }
    }

    /**
     * Data for a {@link HitScanFirer}.
     *
     * @param endSelectorKey  A {@link Key} to the {@link HitScanFirer}'s {@link ShotEndpointSelector}
     * @param targetFinderKey A {@link Key} to the {@link HitScanFirer}'s {@link TargetFinder}
     * @param shotHandlerKeys A {@link Key} to the {@link HitScanFirer}'s {@link ShotHandler}s
     */
    public record Data(@NotNull Key endSelectorKey,
                       @NotNull Key targetFinderKey,
                       @NotNull Collection<Key> shotHandlerKeys) implements Keyed {

        /**
         * The serial {@link Key} of this {@link Data}.
         */
        public static final Key SERIAL_KEY = Key.key(Namespaces.PHANTAZM, "gun.firer.hit_scan");

        /**
         * Creates a {@link Data}.
         *
         * @param endSelectorKey  A {@link Key} to the {@link HitScanFirer}'s {@link ShotEndpointSelector}
         * @param targetFinderKey A {@link Key} to the {@link HitScanFirer}'s {@link TargetFinder}
         * @param shotHandlerKeys A {@link Key} to the {@link HitScanFirer}'s {@link ShotHandler}s
         */
        public Data {
            Objects.requireNonNull(endSelectorKey, "endSelectorKey");
            Objects.requireNonNull(targetFinderKey, "targetFinderKey");
            Objects.requireNonNull(shotHandlerKeys, "shotHandlerKeys");
        }

        @Override
        public @NotNull Key key() {
            return SERIAL_KEY;
        }
    }

}
