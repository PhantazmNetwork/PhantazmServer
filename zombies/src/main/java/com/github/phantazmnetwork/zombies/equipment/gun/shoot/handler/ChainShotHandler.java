package com.github.phantazmnetwork.zombies.equipment.gun.shoot.handler;

import com.github.phantazmnetwork.zombies.equipment.gun.GunState;
import com.github.phantazmnetwork.zombies.equipment.gun.shoot.GunHit;
import com.github.phantazmnetwork.zombies.equipment.gun.shoot.GunShot;
import com.github.phantazmnetwork.zombies.equipment.gun.shoot.fire.Firer;
import com.github.phantazmnetwork.zombies.equipment.gun.target.entityfinder.positional.PositionalEntityFinder;
import com.github.steanky.element.core.annotation.*;
import com.github.steanky.ethylene.core.ConfigElement;
import com.github.steanky.ethylene.core.collection.ConfigNode;
import com.github.steanky.ethylene.core.collection.LinkedConfigNode;
import com.github.steanky.ethylene.core.processor.ConfigProcessException;
import com.github.steanky.ethylene.core.processor.ConfigProcessor;
import net.kyori.adventure.key.Key;
import net.minestom.server.collision.BoundingBox;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import net.minestom.server.instance.Instance;
import org.jetbrains.annotations.NotNull;

import java.util.*;

/**
 * A {@link ShotHandler} which fires its own shots based on the entities that are hit.
 */
@Model("zombies.gun.shot_handler.chain")
public class ChainShotHandler implements ShotHandler {

    private final Data data;
    private final PositionalEntityFinder finder;
    private final Firer firer;

    /**
     * Creates a new {@link ChainShotHandler}.
     *
     * @param data   The {@link Data} for this {@link ChainShotHandler}
     * @param finder The {@link PositionalEntityFinder} used to find {@link Entity}s to shoot in the direction of
     * @param firer  The {@link Firer} used to shoot new shots
     */
    @FactoryMethod
    public ChainShotHandler(@NotNull Data data, @NotNull @DataName("finder") PositionalEntityFinder finder,
            @NotNull @DataName("firer") Firer firer) {
        this.data = Objects.requireNonNull(data, "data");
        this.finder = Objects.requireNonNull(finder, "finder");
        this.firer = Objects.requireNonNull(firer, "firer");
    }

    /**
     * Creates a {@link ConfigProcessor} for {@link Data}s.
     *
     * @return A {@link ConfigProcessor} for {@link Data}s
     */
    public static @NotNull ConfigProcessor<Data> processor() {
        return new ConfigProcessor<>() {

            @Override
            public @NotNull Data dataFromElement(@NotNull ConfigElement element) throws ConfigProcessException {
                String finderKey = element.getStringOrThrow("entityFinderPath");
                String firerKey = element.getStringOrThrow("firerPath");
                boolean ignorePreviousHits = element.getBooleanOrThrow("ignorePreviousHits");
                int fireAttempts = element.getNumberOrThrow("fireAttempts").intValue();
                if (fireAttempts < 0) {
                    throw new ConfigProcessException("fireAttempts must be greater than or equal to 0");
                }

                return new Data(finderKey, firerKey, ignorePreviousHits, fireAttempts);
            }

            @Override
            public @NotNull ConfigElement elementFromData(@NotNull Data data) {
                ConfigNode node = new LinkedConfigNode(4);
                node.putString("entityFinderPath", data.finderPath());
                node.putString("firerPath", data.firerPath());
                node.putBoolean("ignorePreviousHits", data.ignorePreviousHits());
                node.putNumber("fireAttempts", data.fireAttempts());

                return node;
            }
        };
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
                Vec direction =
                        Vec.fromPoint(entity.getPosition().add(0, boundingBox.height() / 2, 0).sub(hit.location()));
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

    /**
     * Data for a {@link ChainShotHandler}.
     *
     * @param finderPath         A path to the {@link ChainShotHandler}'s {@link PositionalEntityFinder} which finds
     *                           {@link Entity}s to shoot in the direction of
     * @param firerPath          A path to the {@link ChainShotHandler}'s {@link Firer} used to shoot new shots
     * @param ignorePreviousHits Whether the {@link ChainShotHandler} should shoot in the direction of previously hit targets
     * @param fireAttempts       The number of times the {@link ChainShotHandler} should try to shoot at new targets
     */
    @DataObject
    public record Data(@NotNull @DataPath("finder") String finderPath,
                       @NotNull @DataPath("firer") String firerPath,
                       boolean ignorePreviousHits,
                       int fireAttempts) {

        /**
         * Creates a {@link Data}.
         *
         * @param finderPath         A path to the {@link ChainShotHandler}'s {@link PositionalEntityFinder} which finds
         *                           {@link Entity}s to shoot in the direction of
         * @param firerPath          A path to the {@link ChainShotHandler}'s {@link Firer} used to shoot new shots
         * @param ignorePreviousHits Whether the {@link ChainShotHandler} should shoot in the direction of previously hit targets
         * @param fireAttempts       The number of times the {@link ChainShotHandler} should try to shoot at new targets
         */
        public Data {
            Objects.requireNonNull(finderPath, "finderPath");
            Objects.requireNonNull(firerPath, "firerPath");
        }

    }

}
