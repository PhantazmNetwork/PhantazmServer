package org.phantazm.zombies.listener;

import com.github.steanky.element.core.key.Constants;
import com.github.steanky.element.core.key.KeyParser;
import com.github.steanky.vector.Bounds3I;
import net.kyori.adventure.key.Key;
import net.minestom.server.collision.BoundingBox;
import net.minestom.server.collision.CollisionUtils;
import net.minestom.server.collision.PhysicsResult;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import net.minestom.server.event.entity.EntityDeathEvent;
import net.minestom.server.instance.Chunk;
import net.minestom.server.instance.Instance;
import org.intellij.lang.annotations.Subst;
import org.jetbrains.annotations.NotNull;
import org.phantazm.core.tracker.BoundedTracker;
import org.phantazm.mob.MobStore;
import org.phantazm.mob.PhantazmMob;
import org.phantazm.zombies.Tags;
import org.phantazm.zombies.map.Room;
import org.phantazm.zombies.map.Round;
import org.phantazm.zombies.map.Window;
import org.phantazm.zombies.powerup.PowerupHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;

public class PhantazmMobDeathListener extends PhantazmMobEventListener<EntityDeathEvent> {
    private static final Logger LOGGER = LoggerFactory.getLogger(PhantazmMobDeathListener.class);
    private static final BoundingBox POWERUP_BOUNDING_BOX = new BoundingBox(0.25, 0.5, 0.25);
    private static final Vec DOWNWARD_SEARCH_VECTOR = new Vec(0, -10, 0);
    private static final Vec OFFSET = new Vec(0.5, 0, 0.5);

    private final KeyParser keyParser;
    private final Supplier<? extends Optional<Round>> roundSupplier;
    private final PowerupHandler powerupHandler;

    private final BoundedTracker<Room> roomTracker;
    private final BoundedTracker<Window> windowTracker;

    public PhantazmMobDeathListener(@NotNull KeyParser keyParser, @NotNull Instance instance,
            @NotNull MobStore mobStore, @NotNull Supplier<? extends Optional<Round>> roundSupplier,
            @NotNull PowerupHandler powerupHandler, @NotNull BoundedTracker<Room> roomTracker,
            @NotNull BoundedTracker<Window> windowTracker) {
        super(instance, mobStore);
        this.keyParser = Objects.requireNonNull(keyParser, "keyParser");
        this.roundSupplier = Objects.requireNonNull(roundSupplier, "roundSupplier");
        this.powerupHandler = Objects.requireNonNull(powerupHandler, "powerupHandler");

        this.roomTracker = Objects.requireNonNull(roomTracker, "roomTracker");
        this.windowTracker = Objects.requireNonNull(windowTracker, "windowTracker");
    }

    @Override
    public void accept(@NotNull PhantazmMob mob, @NotNull EntityDeathEvent event) {
        roundSupplier.get().ifPresent(round -> {
            round.removeMob(mob);
        });

        trySpawnPowerup(event.getEntity());
        getMobStore().onMobDeath(event);
    }

    private void trySpawnPowerup(Entity entity) {
        @Subst(Constants.NAMESPACE_OR_KEY)
        String powerup = entity.getTag(Tags.POWERUP_TAG);
        if (powerup != null) {
            if (!keyParser.isValidKey(powerup)) {
                LOGGER.warn("Cannot spawn invalid powerup key " + powerup);
                return;
            }

            Key key = keyParser.parseKey(powerup);

            if (!powerupHandler.typeExists(key)) {
                LOGGER.warn("Cannot spawn nonexistent powerup key " + key);
                return;
            }

            Point position = entity.getPosition();

            Optional<Room> roomOptional = roomTracker.atPoint(position);
            if (roomOptional.isPresent()) {
                powerupHandler.spawn(key, seekDown(position));
                return;
            }

            Optional<Window> windowOptional =
                    windowTracker.closestInRangeToBounds(position, POWERUP_BOUNDING_BOX.width(),
                            POWERUP_BOUNDING_BOX.height(), 10);
            if (windowOptional.isEmpty()) {
                LOGGER.warn("Spawning powerup at location " + position +
                        " that does not have a nearby window and is not in a room");
                powerupHandler.spawn(key, seekDown(position));
                return;
            }

            Window nearestWindow = windowOptional.get();
            Bounds3I frameRegion = nearestWindow.getWindowInfo().frameRegion();
            Point windowCenter = nearestWindow.center();
            Point toWindow = windowCenter.sub(position);

            Vec axis = new Vec(Integer.signum((int)Math.rint(toWindow.x())), 0,
                    Integer.signum((int)Math.rint(toWindow.z())));

            Vec mulVec = new Vec(Math.abs(axis.x()) * (frameRegion.lengthX() / 2D), 0,
                    Math.abs(axis.z()) * (frameRegion.lengthZ() / 2D));

            Point spawnCandidate = windowCenter.add(axis.mul(mulVec)).add(axis.mul(OFFSET));
            if (roomTracker.atPoint(spawnCandidate).isEmpty()) {
                LOGGER.warn("Tried to adjust powerup location to " + spawnCandidate + ", but it is not inside a room");
            }

            powerupHandler.spawn(key, seekDown(spawnCandidate));
        }
    }

    private Point seekDown(Point point) {
        Chunk chunk = instance.getChunkAt(point);
        if (chunk == null) {
            return point;
        }

        PhysicsResult result = CollisionUtils.handlePhysics(instance, chunk, POWERUP_BOUNDING_BOX, Pos.fromPoint(point),
                DOWNWARD_SEARCH_VECTOR, null);

        if (!result.hasCollision()) {
            return point;
        }

        return result.newPosition();
    }
}
