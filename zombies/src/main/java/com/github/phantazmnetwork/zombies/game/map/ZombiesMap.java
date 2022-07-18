package com.github.phantazmnetwork.zombies.game.map;

import com.github.phantazmnetwork.commons.Tickable;
import com.github.phantazmnetwork.commons.component.ComponentBuilder;
import com.github.phantazmnetwork.commons.component.ComponentException;
import com.github.phantazmnetwork.commons.component.DependencyProvider;
import com.github.phantazmnetwork.commons.component.annotation.ComponentDependency;
import com.github.phantazmnetwork.commons.vector.Region3I;
import com.github.phantazmnetwork.commons.vector.Vec3D;
import com.github.phantazmnetwork.commons.vector.Vec3I;
import com.github.phantazmnetwork.core.ClientBlockHandler;
import com.github.phantazmnetwork.mob.spawner.MobSpawner;
import com.github.phantazmnetwork.zombies.game.SpawnDistributor;
import com.github.phantazmnetwork.zombies.map.*;
import net.kyori.adventure.key.Key;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.UnmodifiableView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.function.Supplier;

public class ZombiesMap extends PositionalMapObject<MapInfo> implements Tickable {
    @ComponentDependency("phantazm:zombies.dependency.map.context")
    public record ObjectContext(@NotNull Instance instance,
                                @NotNull MobSpawner spawner,
                                @NotNull ClientBlockHandler blockHandler,
                                @NotNull Supplier<? extends Round> currentRoundSupplier) {
    }

    private static final Logger LOGGER = LoggerFactory.getLogger(ZombiesMap.class);

    private final List<Room> unmodifiableRooms;
    private final List<Door> unmodifiableDoors;
    private final List<Window> unmodifiableWindows;
    private final List<Spawnpoint> unmodifiableSpawnpoints;
    private final List<Round> unmodifiableRounds;

    private int roundIndex = -1;
    private Round currentRound = null;

    /**
     * Constructs a new instance of this class.
     *
     * @param info     the backing data object
     * @param instance the instance which this MapObject is in
     */
    public ZombiesMap(@NotNull MapInfo info, @NotNull ComponentBuilder builder, @NotNull Instance instance,
                      @NotNull MobSpawner mobSpawner, @NotNull ClientBlockHandler blockHandler,
                      @NotNull SpawnDistributor spawnDistributor) {
        super(info, info.settings().origin(), instance);

        ObjectContext context = new ObjectContext(instance, mobSpawner, blockHandler, this::currentRound);
        DependencyProvider provider = DependencyProvider.ofDependencies(context);

        List<RoomInfo> roomData = info.rooms();
        List<DoorInfo> doorData = info.doors();
        List<WindowInfo> windowData = info.windows();
        List<SpawnpointInfo> spawnpointData = info.spawnpoints();
        List<SpawnruleInfo> spawnruleData = info.spawnrules();
        List<RoundInfo> roundData = info.rounds();

        List<Room> rooms = new ArrayList<>(roomData.size());
        List<Door> doors = new ArrayList<>(doorData.size());
        List<Window> windows = new ArrayList<>(windowData.size());
        List<Spawnpoint> spawnpoints = new ArrayList<>(spawnpointData.size());
        List<Round> rounds = new ArrayList<>(roundData.size());
        Map<Key, SpawnruleInfo> spawnruleMap = new HashMap<>(spawnruleData.size());

        this.unmodifiableRooms = Collections.unmodifiableList(rooms);
        this.unmodifiableDoors = Collections.unmodifiableList(doors);
        this.unmodifiableWindows = Collections.unmodifiableList(windows);
        this.unmodifiableSpawnpoints = Collections.unmodifiableList(spawnpoints);
        this.unmodifiableRounds = Collections.unmodifiableList(rounds);

        try {
            for (RoomInfo roomInfo : roomData) {
                rooms.add(new Room(roomInfo, getOrigin(), instance,
                                   builder.makeComponentsFromData(roomInfo.openActions(), provider, ArrayList::new,
                                                                  e -> LOGGER.warn(
                                                                          "Error initializing room actions for {}: {}",
                                                                          roomInfo, e
                                                                  )
                                   )
                ));
            }

            for (DoorInfo doorInfo : doorData) {
                doors.add(new Door(doorInfo, origin, instance, Block.AIR,
                                   builder.makeComponentsFromData(doorInfo.openActions(), provider, ArrayList::new,
                                                                  e -> LOGGER.warn(
                                                                          "Error initializing door actions for {}: {}",
                                                                          doorInfo, e
                                                                  )
                                   )
                ));
            }

            for (WindowInfo windowInfo : windowData) {
                windows.add(new Window(instance, windowInfo, origin, blockHandler,
                                       builder.makeComponentsFromData(windowInfo.repairActions(), provider,
                                                                      ArrayList::new, e -> LOGGER.warn(
                                                       "Error initializing repair actions for {}: {}", windowInfo, e)
                                       ), builder.makeComponentsFromData(windowInfo.breakActions(), provider,
                                                                         ArrayList::new, e -> LOGGER.warn(
                                "Error initializing break actions for {}: {}", windowInfo, e)
                )
                ));
            }

            for (SpawnpointInfo spawnpointInfo : info.spawnpoints()) {
                spawnpoints.add(new Spawnpoint(spawnpointInfo, origin, instance, spawnruleMap::get, mobSpawner));
            }

            for (SpawnruleInfo spawnrule : spawnruleData) {
                spawnruleMap.put(spawnrule.id(), spawnrule);
            }

            for (RoundInfo roundInfo : roundData) {
                rounds.add(new Round(roundInfo, instance,
                                     builder.makeComponentsFromData(roundInfo.startActions(), provider, ArrayList::new,
                                                                    e -> LOGGER.warn(
                                                                            "Error initializing round start actions for {}: {}",
                                                                            roundInfo, e
                                                                    )
                                     ), builder.makeComponentsFromData(roundInfo.endActions(), provider, ArrayList::new,
                                                                       e -> LOGGER.warn(
                                                                               "Error initializing round end actions for {}: {}",
                                                                               roundInfo, e
                                                                       )
                ), spawnDistributor
                ));
            }
        }
        catch (ComponentException e) {
            LOGGER.warn("Uncaught ComponentException when constructing map {}: {}", info.settings().id(), e);
        }
    }

    public @UnmodifiableView @NotNull List<Room> getRooms() {
        return unmodifiableRooms;
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
                    currentRound = null;
                }
            }
        }
    }
}
