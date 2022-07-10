package com.github.phantazmnetwork.zombies.equipment.gun.shoot.handler;

import com.github.phantazmnetwork.commons.AdventureConfigProcessors;
import com.github.phantazmnetwork.commons.Namespaces;
import com.github.phantazmnetwork.zombies.equipment.gun.GunState;
import com.github.phantazmnetwork.zombies.equipment.gun.shoot.GunHit;
import com.github.phantazmnetwork.zombies.equipment.gun.shoot.GunShot;
import com.github.phantazmnetwork.zombies.equipment.gun.shoot.fire.Firer;
import com.github.phantazmnetwork.zombies.equipment.gun.target.entityfinder.positional.PositionalEntityFinder;
import com.github.steanky.ethylene.core.ConfigElement;
import com.github.steanky.ethylene.core.collection.ConfigNode;
import com.github.steanky.ethylene.core.collection.LinkedConfigNode;
import com.github.steanky.ethylene.core.processor.ConfigProcessException;
import com.github.steanky.ethylene.core.processor.ConfigProcessor;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.key.Keyed;
import net.minestom.server.collision.BoundingBox;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import net.minestom.server.instance.Instance;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.BiConsumer;

/**
 * A {@link ShotHandler} which fires its own shots based on the entities that are hit.
 */
public class ChainShotHandler implements ShotHandler {

    /**
     * Data for a {@link ChainShotHandler}.
     * @param finderKey A {@link Key} to the {@link ChainShotHandler}'s {@link PositionalEntityFinder} which finds
     *                  {@link Entity}s to shoot in the direction of
     * @param firerKey A {@link Key} to the {@link ChainShotHandler}'s {@link Firer} used to shoot new shots
     * @param ignorePreviousHits Whether the {@link ChainShotHandler} should shoot in the direction of previously hit targets
     * @param fireAttempts The number of times the {@link ChainShotHandler} should try to shoot at new targets
     */
    public record Data(@NotNull Key finderKey, @NotNull Key firerKey, boolean ignorePreviousHits, int fireAttempts)
            implements Keyed {

        /**
         * The serial {@link Key} for this {@link Data}.
         */
        public static final Key SERIAL_KEY = Key.key(Namespaces.PHANTAZM,"gun.shot_handler.chain");

        /**
         * Creates a {@link Data}.
         * @param finderKey A {@link Key} to the {@link ChainShotHandler}'s {@link PositionalEntityFinder} which finds
         *                  {@link Entity}s to shoot in the direction of
         * @param firerKey A {@link Key} to the {@link ChainShotHandler}'s {@link Firer} used to shoot new shots
         * @param ignorePreviousHits Whether the {@link ChainShotHandler} should shoot in the direction of previously hit targets
         * @param fireAttempts The number of times the {@link ChainShotHandler} should try to shoot at new targets
         */
        public Data {
            Objects.requireNonNull(finderKey, "finderKey");
            Objects.requireNonNull(firerKey, "firerKey");
        }

        @Override
        public @NotNull Key key() {
            return SERIAL_KEY;
        }
    }

    /**
     * Creates a {@link ConfigProcessor} for {@link Data}s.
     * @return A {@link ConfigProcessor} for {@link Data}s
     */
    public static @NotNull ConfigProcessor<Data> processor() {
        ConfigProcessor<Key> keyProcessor = AdventureConfigProcessors.key();

        return new ConfigProcessor<>() {

            @Override
            public @NotNull Data dataFromElement(@NotNull ConfigElement element) throws ConfigProcessException {
                Key finderKey = keyProcessor.dataFromElement(element.getElementOrThrow("entityFinderKey"));
                Key firerKey = keyProcessor.dataFromElement(element.getElementOrThrow("firerKey"));
                boolean ignorePreviousHits = element.getBooleanOrThrow("ignorePreviousHits");
                int fireAttempts = element.getNumberOrThrow("fireAttempts").intValue();
                if (fireAttempts < 0) {
                    throw new ConfigProcessException("fireAttempts must be greater than or equal to 0");
                }

                return new Data(finderKey, firerKey, ignorePreviousHits, fireAttempts);
            }

            @Override
            public @NotNull ConfigElement elementFromData(@NotNull Data data) throws ConfigProcessException {
                ConfigNode node = new LinkedConfigNode(4);
                node.put("entityFinderKey", keyProcessor.elementFromData(data.finderKey()));
                node.put("firerKey", keyProcessor.elementFromData(data.firerKey()));
                node.putBoolean("ignorePreviousHits", data.ignorePreviousHits());
                node.putNumber("fireAttempts", data.fireAttempts());

                return node;
            }
        };
    }

    /**
     * Creates a dependency consumer for {@link Data}s.
     * @return A dependency consumer for {@link Data}s
     */
    public static @NotNull BiConsumer<Data, Collection<Key>> dependencyConsumer() {
        return (data, keys) -> {
            keys.add(data.finderKey());
            keys.add(data.firerKey());
        };
    }

    private final Data data;

    private final PositionalEntityFinder finder;

    private final Firer firer;

    /**
     * Creates a new {@link ChainShotHandler}.
     * @param data The {@link Data} for this {@link ChainShotHandler}
     * @param finder The {@link PositionalEntityFinder} used to find {@link Entity}s to shoot in the direction of
     * @param firer The {@link Firer} used to shoot new shots
     */
    public ChainShotHandler(@NotNull Data data, @NotNull PositionalEntityFinder finder, @NotNull Firer firer) {
        this.data = Objects.requireNonNull(data, "data");
        this.finder = Objects.requireNonNull(finder, "finder");
        this.firer = Objects.requireNonNull(firer, "firer");
    }

    @Override
    public void handle(@NotNull GunState state, @NotNull Entity attacker, @NotNull Collection<UUID> previousHits,
                       @NotNull GunShot shot) {
        Instance instance = attacker.getInstance();
        if (instance == null) {
            return;
        }

        int attempts = data.fireAttempts();
        int combinedSize = shot.headshotTargets().size() + shot.regularTargets().size();
        Collection<GunHit> combinedHits = new ArrayList<>(combinedSize);
        combinedHits.addAll(shot.regularTargets());
        combinedHits.addAll(shot.headshotTargets());

        Set<UUID> previousUUIDs = new HashSet<>(combinedSize);
        for (GunHit hit : combinedHits) {
            previousUUIDs.add(hit.entity().getUuid());
        }

        for (GunHit hit : combinedHits) {
            Collection<Entity> entities = finder.findEntities(instance, hit.location());

            for (Entity entity : entities) {
                if (data.ignorePreviousHits() && previousUUIDs.contains(entity.getUuid())) {
                    continue;
                }

                BoundingBox boundingBox = entity.getBoundingBox();
                Vec direction = Vec.fromPoint(entity.getPosition().add(0,
                        boundingBox.height() / 2, 0).sub(hit.location()));
                int initialSize = previousHits.size();
                firer.fire(state, new Pos(hit.location()).withDirection(direction), previousHits);
                if (previousHits.size() > initialSize && --attempts <= 0) {
                    return;
                }
                previousUUIDs.addAll(previousHits);
            }
        }
    }
    @Override
    public void tick(@NotNull GunState state, long time) {
        firer.tick(state, time);
    }

}