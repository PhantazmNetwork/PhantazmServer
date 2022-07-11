package com.github.phantazmnetwork.zombies.game.map;

import com.github.phantazmnetwork.api.ClientBlockHandler;
import com.github.phantazmnetwork.commons.Tickable;
import com.github.phantazmnetwork.commons.vector.Region3I;
import com.github.phantazmnetwork.commons.vector.Vec3D;
import com.github.phantazmnetwork.commons.vector.Vec3I;
import com.github.phantazmnetwork.mob.spawner.MobSpawner;
import com.github.phantazmnetwork.zombies.game.SpawnDistributor;
import com.github.phantazmnetwork.zombies.game.map.action.Action;
import com.github.phantazmnetwork.zombies.game.map.action.AnnounceRoundAction;
import com.github.phantazmnetwork.commons.component.ConfigRegistries;
import com.github.phantazmnetwork.commons.component.KeyedConfigRegistry;
import com.github.phantazmnetwork.commons.component.KeyedFactory;
import com.github.phantazmnetwork.commons.component.KeyedFactoryRegistry;
import com.github.phantazmnetwork.zombies.map.*;
import com.github.steanky.ethylene.core.collection.ConfigList;
import com.github.steanky.ethylene.core.processor.ConfigProcessException;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.key.Keyed;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.UnmodifiableView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class ZombiesMap extends PositionalMapObject<MapInfo> implements Tickable {
    private static final Logger LOGGER = LoggerFactory.getLogger(ZombiesMap.class);

    private final List<Room> unmodifiableRooms;
    private final List<Door> unmodifiableDoors;
    private final List<Window> unmodifiableWindows;
    private final List<Spawnpoint> unmodifiableSpawnpoints;
    private final List<Round> unmodifiableRounds;

    private int roundIndex = 0;
    private Round currentRound;

    /**
     * Constructs a new instance of this class.
     *
     * @param info     the backing data object
     * @param instance the instance which this MapObject is in
     */
    public ZombiesMap(@NotNull MapInfo info,
                      @NotNull Instance instance,
                      @NotNull MobSpawner mobSpawner,
                      @NotNull ClientBlockHandler blockHandler,
                      @NotNull SpawnDistributor spawnDistributor) {
        super(info, info.info().origin(), instance);

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

        for(RoomInfo roomInfo : roomData) {

        }

        for(DoorInfo doorInfo : doorData) {
            //TODO: include door fill block in mapInfo
            doors.add(new Door(doorInfo, origin, instance, Block.AIR, List.of()));
        }

        for(WindowInfo windowInfo : windowData) {
            windows.add(new Window(instance, windowInfo, origin, blockHandler));
        }

        for(SpawnpointInfo spawnpointInfo : info.spawnpoints()) {
            spawnpoints.add(new Spawnpoint(spawnpointInfo, origin, instance, spawnruleMap::get, mobSpawner));
        }

        for(SpawnruleInfo spawnrule : spawnruleData) {
            spawnruleMap.put(spawnrule.id(), spawnrule);
        }

        for(RoundInfo roundInfo : roundData) {

        }
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

    public void startRound(int roundIndex) {
        Objects.checkIndex(roundIndex, unmodifiableRounds.size());

        this.roundIndex = roundIndex;
        Round newCurrent = unmodifiableRounds.get(roundIndex);
        if(newCurrent == currentRound) {
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
        for(Window window : unmodifiableWindows) {
            double currentDistance = window.getCenter().squaredDistance(origin);
            if(currentDistance < nearestDistance) {
                nearestDistance = currentDistance;
                nearestWindow = window;
            }
        }

        if(nearestDistance < distanceSquared) {
            return Optional.of(nearestWindow);
        }

        return Optional.empty();
    }

    public @NotNull Optional<Door> doorAt(@NotNull Vec3I block) {
        for(Door door : unmodifiableDoors) {
            Region3I enclosing = door.getEnclosing();
            if(enclosing.contains(block)) {
                for(Region3I subRegion : door.regions()) {
                    if(subRegion.contains(block)) {
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
        if(currentRound != null) {
            currentRound.tick(time);

            if(!currentRound.isActive()) {
                if(++roundIndex < unmodifiableRounds.size()) {
                    currentRound = unmodifiableRounds.get(roundIndex);
                    currentRound.startRound();
                }
                else {
                    //TODO end of game code
                }
            }
        }
    }
}
