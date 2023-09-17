package org.phantazm.zombies.listener;

import com.github.steanky.element.core.key.KeyParser;
import com.github.steanky.vector.Bounds3I;
import it.unimi.dsi.fastutil.Pair;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.minestom.server.collision.BoundingBox;
import net.minestom.server.collision.CollisionUtils;
import net.minestom.server.collision.PhysicsResult;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.damage.Damage;
import net.minestom.server.event.EventDispatcher;
import net.minestom.server.event.entity.EntityDeathEvent;
import net.minestom.server.instance.Chunk;
import net.minestom.server.instance.Instance;
import org.intellij.lang.annotations.Subst;
import org.jetbrains.annotations.NotNull;
import org.phantazm.core.player.PlayerView;
import org.phantazm.core.tracker.BoundedTracker;
import org.phantazm.mob2.Mob;
import org.phantazm.zombies.ExtraNodeKeys;
import org.phantazm.zombies.Tags;
import org.phantazm.zombies.event.PhantazmMobDeathEvent;
import org.phantazm.zombies.map.MapSettingsInfo;
import org.phantazm.zombies.map.Room;
import org.phantazm.zombies.map.Round;
import org.phantazm.zombies.map.Window;
import org.phantazm.zombies.player.ZombiesPlayer;
import org.phantazm.zombies.powerup.PowerupHandler;
import org.phantazm.zombies.scene2.ZombiesScene;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;

public class PhantazmMobDeathListener extends PhantazmMobEventListener<EntityDeathEvent> {
    private static final Logger LOGGER = LoggerFactory.getLogger(PhantazmMobDeathListener.class);
    private static final BoundingBox POWERUP_BOUNDING_BOX = new BoundingBox(0.25, 0.0625, 0.25);
    private static final Vec DOWNWARD_SEARCH_VECTOR = new Vec(0, -10, 0);
    private static final Vec OFFSET = new Vec(0.5, 0, 0.5);
    private static final double ROOM_PENETRATION_DEPTH = 1.5;

    private final KeyParser keyParser;
    private final Supplier<Optional<Round>> roundSupplier;
    private final PowerupHandler powerupHandler;

    private final BoundedTracker<Room> roomTracker;
    private final BoundedTracker<Window> windowTracker;
    private final Map<PlayerView, ZombiesPlayer> playerMap;

    private final MapSettingsInfo settingsInfo;

    public PhantazmMobDeathListener(@NotNull KeyParser keyParser, @NotNull Instance instance,
        @NotNull Supplier<Optional<Round>> roundSupplier,
        @NotNull PowerupHandler powerupHandler, @NotNull BoundedTracker<Room> roomTracker,
        @NotNull BoundedTracker<Window> windowTracker,
        @NotNull Map<PlayerView, ZombiesPlayer> playerMap, @NotNull MapSettingsInfo settingsInfo,
        @NotNull Supplier<ZombiesScene> scene) {
        super(instance, scene);
        this.keyParser = Objects.requireNonNull(keyParser);
        this.roundSupplier = Objects.requireNonNull(roundSupplier);
        this.powerupHandler = Objects.requireNonNull(powerupHandler);

        this.roomTracker = Objects.requireNonNull(roomTracker);
        this.windowTracker = Objects.requireNonNull(windowTracker);
        this.playerMap = Objects.requireNonNull(playerMap);
        this.settingsInfo = Objects.requireNonNull(settingsInfo);
    }

    @Override
    public void accept(@NotNull ZombiesScene scene, @NotNull Mob mob, @NotNull EntityDeathEvent event) {
        EventDispatcher.call(new PhantazmMobDeathEvent(mob));

        roundSupplier.get().ifPresent(round -> {
            round.removeMob(mob);
        });

        trySpawnPowerups(event.getEntity());

        Damage damage = mob.getLastDamageSource();
        if (damage == null) {
            return;
        }

        Entity attacker = damage.getAttacker();
        if (attacker == null) {
            return;
        }

        ZombiesPlayer player = playerMap.get(PlayerView.lookup(attacker.getUuid()));
        if (player == null) {
            return;
        }

        player.getPlayer().ifPresent(actualPlayer -> {
            player.module().getKills().onKill(mob);

            if (!mob.data().extra().getBooleanOrDefault(false, ExtraNodeKeys.ANNOUNCE_KILL)) {
                return;
            }

            TagResolver mobTag = Placeholder.component("mob", mob.name());
            TagResolver playerTag = Placeholder.component("player", actualPlayer.getName());

            instance.sendMessage(MiniMessage.miniMessage().deserialize(settingsInfo.killMobFormat(), mobTag, playerTag));
        });
    }

    private void trySpawnPowerups(Entity entity) {
        for (String powerup : entity.getTag(Tags.POWERUP_TAG)) {
            spawnPowerup(entity, powerup);
        }
    }

    private void spawnPowerup(Entity entity, @Subst("a") String powerup) {
        if (!keyParser.isValidKey(powerup)) {
            LOGGER.warn("Cannot spawn invalid powerup key " + powerup);
            return;
        }

        Key key = keyParser.parseKey(powerup);
        if (!powerupHandler.canSpawnType(key)) {
            LOGGER.warn("Cannot spawn nonexistent powerup key " + key);
            return;
        }

        Point position = entity.getPosition();

        Optional<Room> roomOptional = roomTracker.atPoint(position);
        if (roomOptional.isPresent()) {
            powerupHandler.spawn(key, seekDown(position));
            return;
        }

        Optional<Window> windowOptional = windowTracker.closestInRangeToBounds(position, POWERUP_BOUNDING_BOX.width(),
            POWERUP_BOUNDING_BOX.height(), 10);
        if (windowOptional.isEmpty()) {
            Optional<Pair<Room, Vec>> nearestRoomOptional =
                roomTracker.closestInRangeToBoundsWithVec(position, POWERUP_BOUNDING_BOX.width(),
                    POWERUP_BOUNDING_BOX.height(), 15);
            if (nearestRoomOptional.isEmpty()) {
                Point targetPoint = seekDown(position);
                LOGGER.warn("Failed to find nearby room or window for powerup spawn at " + targetPoint);
                powerupHandler.spawn(key, targetPoint);
                return;
            }

            Pair<Room, Vec> nearestRoom = nearestRoomOptional.get();
            Vec roomVec = nearestRoom.right();
            powerupHandler.spawn(key,
                seekDown(roomVec.add(roomVec.sub(position).normalize().mul(ROOM_PENETRATION_DEPTH))));
            return;
        }

        Window nearestWindow = windowOptional.get();
        Bounds3I frameRegion = nearestWindow.getWindowInfo().frameRegion();
        Point center = nearestWindow.center();

        boolean xSmaller = frameRegion.lengthX() < frameRegion.lengthZ();

        Vec normal = new Vec(xSmaller ? 1 : 0, 0, xSmaller ? 0 : 1);

        normal = normal.mul(xSmaller ? frameRegion.lengthX() / 2.0 : frameRegion.lengthZ() / 2.0)
            .add(OFFSET.mul(normal));

        Vec otherNormal = normal.mul(-1);

        Vec targetNormal;
        if (roomTracker.atPoint(center.add(normal)).isPresent()) {
            targetNormal = normal;
        } else if (roomTracker.atPoint(center.add(otherNormal)).isPresent()) {
            targetNormal = otherNormal;
        } else {
            targetNormal = normal;
            LOGGER.warn("Unable to find matching room at window near " + center);
        }

        Point test = seekDown(center.add(targetNormal));
        if (roomTracker.atPoint(test).isEmpty()) {
            LOGGER.warn("Spawning powerup outside of a room");
        }

        powerupHandler.spawn(key, test);
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
