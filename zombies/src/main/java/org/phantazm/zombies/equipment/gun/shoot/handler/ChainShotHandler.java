package org.phantazm.zombies.equipment.gun.shoot.handler;

import com.github.steanky.element.core.annotation.*;
import net.minestom.server.collision.BoundingBox;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import net.minestom.server.instance.Instance;
import org.jetbrains.annotations.NotNull;
import org.phantazm.zombies.equipment.gun.Gun;
import org.phantazm.zombies.equipment.gun.GunState;
import org.phantazm.zombies.equipment.gun2.shoot.GunHit;
import org.phantazm.zombies.equipment.gun2.shoot.GunShot;
import org.phantazm.zombies.equipment.gun.shoot.fire.Firer;
import org.phantazm.zombies.equipment.gun.target.entityfinder.positional.PositionalEntityFinder;

import java.util.*;

/**
 * A {@link ShotHandler} which fires its own shots based on the entities that are hit.
 */
@Model("zombies.gun.shot_handler.chain")
@Cache(false)
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
    public ChainShotHandler(@NotNull Data data, @NotNull @Child("finder") PositionalEntityFinder finder,
        @NotNull @Child("firer") Firer firer) {
        this.data = Objects.requireNonNull(data);
        this.finder = Objects.requireNonNull(finder);
        this.firer = Objects.requireNonNull(firer);
    }

    @Override
    public void handle(@NotNull Gun gun, @NotNull GunState state, @NotNull Entity attacker,
        @NotNull Collection<UUID> previousHits, @NotNull GunShot shot) {
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
                firer.fire(gun, state, new Pos(hit.location()).withDirection(direction), previousHits);
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
     * @param finder             A path to the {@link ChainShotHandler}'s {@link PositionalEntityFinder} which finds
     *                           {@link Entity}s to shoot in the direction of
     * @param firer              A path to the {@link ChainShotHandler}'s {@link Firer} used to shoot new shots
     * @param ignorePreviousHits Whether the {@link ChainShotHandler} should shoot in the direction of previously hit
     *                           targets
     * @param fireAttempts       The number of times the {@link ChainShotHandler} should try to shoot at new targets
     */
    @DataObject
    public record Data(
        @NotNull @ChildPath("finder") String finder,
        @NotNull @ChildPath("firer") String firer,
        boolean ignorePreviousHits,
        int fireAttempts) {
    }
}
