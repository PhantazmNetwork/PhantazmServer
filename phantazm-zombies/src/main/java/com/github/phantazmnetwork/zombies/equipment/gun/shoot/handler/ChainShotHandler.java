package com.github.phantazmnetwork.zombies.equipment.gun.shoot.handler;

import net.kyori.adventure.key.Keyed;
import com.github.phantazmnetwork.commons.Namespaces;
import com.github.phantazmnetwork.mob.PhantazmMob;
import com.github.phantazmnetwork.zombies.equipment.gun.GunState;
import com.github.phantazmnetwork.zombies.equipment.gun.shoot.GunHit;
import com.github.phantazmnetwork.zombies.equipment.gun.shoot.GunShot;
import com.github.phantazmnetwork.zombies.equipment.gun.shoot.fire.Firer;
import com.github.phantazmnetwork.zombies.equipment.gun.target.entityfinder.positional.PositionalEntityFinder;
import net.kyori.adventure.key.Key;
import net.minestom.server.collision.BoundingBox;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.Instance;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class ChainShotHandler implements ShotHandler {

    public record Data(@NotNull Key finderKey, @NotNull Key firerKey, boolean ignorePreviousHits, int fireAttempts)
            implements Keyed {

        public static final Key SERIAL_KEY = Key.key(Namespaces.PHANTAZM,"gun.shot_handler.chain");

        @Override
        public @NotNull Key key() {
            return SERIAL_KEY;
        }
    }

    private final Data data;

    private final PositionalEntityFinder finder;

    private final Firer firer;

    public ChainShotHandler(@NotNull Data data, @NotNull PositionalEntityFinder finder, @NotNull Firer firer) {
        this.data = Objects.requireNonNull(data, "data");
        this.finder = Objects.requireNonNull(finder, "finder");
        this.firer = Objects.requireNonNull(firer, "firer");
    }

    @Override
    public void handle(@NotNull GunState state, @NotNull Player attacker, @NotNull Collection<PhantazmMob> previousHits,
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
            previousUUIDs.add(hit.mob().entity().getUuid());
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
                for (PhantazmMob mob : previousHits) {
                    previousUUIDs.add(mob.entity().getUuid());
                }
            }
        }
    }
    @Override
    public void tick(@NotNull GunState state, long time) {
        firer.tick(state, time);
    }

    @Override
    public @NotNull Keyed getData() {
        return data;
    }
}
