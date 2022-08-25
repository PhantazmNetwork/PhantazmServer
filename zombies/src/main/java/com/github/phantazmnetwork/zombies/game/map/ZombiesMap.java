package com.github.phantazmnetwork.zombies.game.map;

import com.github.phantazmnetwork.commons.Tickable;
import com.github.phantazmnetwork.commons.vector.Region3I;
import com.github.phantazmnetwork.commons.vector.Vec3D;
import com.github.phantazmnetwork.commons.vector.Vec3I;
import com.github.phantazmnetwork.core.ClientBlockHandler;
import com.github.phantazmnetwork.mob.spawner.MobSpawner;
import com.github.phantazmnetwork.zombies.game.SpawnDistributor;
import com.github.phantazmnetwork.zombies.game.coin.BasicModifierSource;
import com.github.phantazmnetwork.zombies.game.coin.ModifierSource;
import com.github.phantazmnetwork.zombies.game.map.shop.Shop;
import com.github.phantazmnetwork.zombies.game.player.ZombiesPlayer;
import com.github.phantazmnetwork.zombies.map.*;
import com.github.steanky.element.core.context.ContextManager;
import com.github.steanky.element.core.dependency.DependencyProvider;
import com.github.steanky.element.core.dependency.ModuleDependencyProvider;
import com.github.steanky.element.core.key.KeyParser;
import com.github.steanky.ethylene.core.ConfigElement;
import com.github.steanky.ethylene.core.collection.ConfigList;
import com.github.steanky.ethylene.core.collection.ConfigNode;
import net.kyori.adventure.key.Key;
import net.minestom.server.instance.Instance;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.UnmodifiableView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class ZombiesMap extends PositionalMapObject<MapInfo> implements Tickable {
    private static final Logger LOGGER = LoggerFactory.getLogger(ZombiesMap.class);

    private final ModifierSource modifierSource;

    private final List<Room> unmodifiableRooms;
    private final List<Shop> unmodifiableShops;
    private final List<Door> unmodifiableDoors;
    private final List<Window> unmodifiableWindows;
    private final List<Spawnpoint> unmodifiableSpawnpoints;
    private final List<Round> unmodifiableRounds;
    private final Set<Key> flags;

    private int roundIndex = -1;
    private Round currentRound = null;
    private boolean hasCompleted = false;

    /**
     * Constructs a new instance of this class.
     *
     * @param info     the backing data object
     * @param instance the instance which this MapObject is in
     */
    public ZombiesMap(@NotNull MapInfo info, @NotNull ContextManager contextManager, @NotNull Instance instance,
            @NotNull MobSpawner mobSpawner, @NotNull ClientBlockHandler blockHandler,
            @NotNull SpawnDistributor spawnDistributor, @NotNull Map<UUID, ZombiesPlayer> zombiesPlayers,
            @NotNull KeyParser keyParser) {
        super(info, info.settings().origin(), instance);

        DependencyProvider provider =
                new ModuleDependencyProvider(new ZombiesMapDependencyModule(this, zombiesPlayers), keyParser);

        this.modifierSource = new BasicModifierSource();

        List<RoomInfo> roomData = info.rooms();
        List<ShopInfo> shopData = info.shops();
        List<DoorInfo> doorData = info.doors();
        List<WindowInfo> windowData = info.windows();
        List<SpawnpointInfo> spawnpointData = info.spawnpoints();
        List<SpawnruleInfo> spawnruleData = info.spawnrules();
        List<RoundInfo> roundData = info.rounds();

        List<Room> rooms = new ArrayList<>(roomData.size());
        List<Shop> shops = new ArrayList<>(shopData.size());
        List<Door> doors = new ArrayList<>(doorData.size());
        List<Window> windows = new ArrayList<>(windowData.size());
        List<Spawnpoint> spawnpoints = new ArrayList<>(spawnpointData.size());
        List<Round> rounds = new ArrayList<>(roundData.size());
        Map<Key, SpawnruleInfo> spawnruleMap = new HashMap<>(spawnruleData.size());

        this.unmodifiableRooms = Collections.unmodifiableList(rooms);
        this.unmodifiableShops = Collections.unmodifiableList(shops);
        this.unmodifiableDoors = Collections.unmodifiableList(doors);
        this.unmodifiableWindows = Collections.unmodifiableList(windows);
        this.unmodifiableSpawnpoints = Collections.unmodifiableList(spawnpoints);
        this.unmodifiableRounds = Collections.unmodifiableList(rounds);

        this.flags = new HashSet<>(2);
    }

    private static Collection<ConfigNode> selectNodes(@NotNull ConfigList list) {
        List<ConfigNode> nodes = new ArrayList<>(list.size());
        for (ConfigElement element : list) {
            if (element.isNode()) {
                nodes.add(element.asNode());
            }
        }

        return nodes;
    }

    public @NotNull ModifierSource modifierSource() {
        return modifierSource;
    }

    public @UnmodifiableView @NotNull List<Room> getRooms() {
        return unmodifiableRooms;
    }

    public @UnmodifiableView @NotNull List<Shop> getShops() {
        return unmodifiableShops;
    }

    public @UnmodifiableView @NotNull List<Spawnpoint> getSpawnpoints() {
        return unmodifiableSpawnpoints;
    }

    public @UnmodifiableView @NotNull List<Window> getWindows() {
        return unmodifiableWindows;
    }

    public @UnmodifiableView @NotNull List<Door> getDoors() {
        return unmodifiableDoors;
    }

    public @UnmodifiableView @NotNull List<Round> getRounds() {
        return unmodifiableRounds;
    }

    public boolean hasFlag(@NotNull Key flag) {
        Objects.requireNonNull(flag, "flag");
        return flags.contains(flag);
    }

    public void setFlag(@NotNull Key flag) {
        Objects.requireNonNull(flag, "flag");
        flags.add(flag);
    }

    public void clearFlag(@NotNull Key flag) {
        Objects.requireNonNull(flag, "flag");
        flags.remove(flag);
    }

    public Round currentRound() {
        return currentRound;
    }

    public int getRoundIndex() {
        return roundIndex;
    }

    public void startRound(int roundIndex) {
        Objects.checkIndex(roundIndex, unmodifiableRounds.size());

        this.roundIndex = roundIndex;
        Round newCurrent = unmodifiableRounds.get(roundIndex);
        if (newCurrent == currentRound) {
            currentRound.endRound();
            currentRound.startRound();
            return;
        }

        currentRound = newCurrent;
        currentRound.startRound();
    }

    public @NotNull Optional<Window> nearestWindowInRange(@NotNull Vec3D origin, double distance) {
        double distanceSquared = distance * distance;

        double nearestDistance = Double.POSITIVE_INFINITY;
        Window nearestWindow = null;
        for (Window window : unmodifiableWindows) {
            double currentDistance = window.getCenter().squaredDistance(origin);
            if (currentDistance < nearestDistance) {
                nearestDistance = currentDistance;
                nearestWindow = window;
            }
        }

        if (nearestDistance < distanceSquared) {
            return Optional.of(nearestWindow);
        }

        return Optional.empty();
    }

    public @NotNull Optional<Window> windowAt(@NotNull Vec3I block) {
        for (Window window : unmodifiableWindows) {
            if (window.getData().frameRegion().contains(block)) {
                return Optional.of(window);
            }
        }

        return Optional.empty();
    }

    public @NotNull Optional<Door> doorAt(@NotNull Vec3I block) {
        for (Door door : unmodifiableDoors) {
            Region3I enclosing = door.getEnclosing();
            if (enclosing.contains(block)) {
                for (Region3I subRegion : door.regions()) {
                    if (subRegion.contains(block)) {
                        return Optional.of(door);
                    }
                }
            }
        }

        return Optional.empty();
    }

    public @NotNull Optional<Room> roomAt(@NotNull Vec3I block) {
        for (Room room : unmodifiableRooms) {
            for (Region3I region : room.roomBounds()) {
                if (region.contains(block)) {
                    return Optional.of(room);
                }
            }
        }

        return Optional.empty();
    }

    public @NotNull Optional<Shop> shopAt(@NotNull Vec3I block) {
        for (Shop shop : unmodifiableShops) {
            if (block.equals(shop.data.triggerLocation())) {
                return Optional.of(shop);
            }
        }

        return Optional.empty();
    }

    public boolean hasCompleted() {
        return hasCompleted;
    }

    @Override
    public void tick(long time) {
        roundTick(time);
    }

    private void roundTick(long time) {
        if (currentRound != null) {
            currentRound.tick(time);

            if (!currentRound.isActive()) {
                if (++roundIndex < unmodifiableRounds.size()) {
                    currentRound = unmodifiableRounds.get(roundIndex);
                    currentRound.startRound();
                }
                else {
                    //TODO end of game code (mostly handled elsewhere, but maybe some stuff should run here?)
                    hasCompleted = true;
                    currentRound = null;
                }
            }
        }
    }
}
