package com.github.phantazmnetwork.zombies.game.map;

import com.github.phantazmnetwork.api.ClientBlockHandler;
import com.github.phantazmnetwork.commons.Tickable;
import com.github.phantazmnetwork.commons.factory.DependencyProvider;
import com.github.phantazmnetwork.commons.vector.Region3I;
import com.github.phantazmnetwork.commons.vector.Vec3D;
import com.github.phantazmnetwork.commons.vector.Vec3I;
import com.github.phantazmnetwork.mob.spawner.MobSpawner;
import com.github.phantazmnetwork.zombies.map.*;
import net.kyori.adventure.key.Key;
import net.minestom.server.coordinate.Point;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.UnmodifiableView;

import java.util.*;

public class ZombiesMap extends PositionalMapObject<MapInfo> implements Tickable {
    private final List<Spawnpoint> unmodifiableSpawnpoints;
    private final List<Window> unmodifiableWindows;
    private final List<Door> unmodifiableDoors;
    private final List<Round> unmodifiableRounds;

    private int roundIndex = 0;
    private Round currentRound;

    /**
     * Constructs a new instance of this class.
     *
     * @param info     the backing data object
     * @param instance the instance which this MapObject is in
     */
    public ZombiesMap(@NotNull MapInfo info, @NotNull Instance instance, @NotNull MobSpawner mobSpawner,
                      @NotNull ClientBlockHandler blockHandler) {
        super(info, info.info().origin(), instance);

        List<SpawnruleInfo> spawnruleData = info.spawnrules();
        List<SpawnpointInfo> spawnpointData = info.spawnpoints();
        List<WindowInfo> windowData = info.windows();
        List<DoorInfo> doorData = info.doors();
        List<RoundInfo> roundData = info.rounds();

        Map<Key, SpawnruleInfo> spawnruleMap = new HashMap<>(spawnruleData.size());

        List<Spawnpoint> spawnpoints = new ArrayList<>(spawnpointData.size());
        this.unmodifiableSpawnpoints = Collections.unmodifiableList(spawnpoints);

        List<Window> windows = new ArrayList<>(windowData.size());
        this.unmodifiableWindows = Collections.unmodifiableList(windows);

        List<Door> doors = new ArrayList<>(doorData.size());
        this.unmodifiableDoors = Collections.unmodifiableList(doors);

        List<Round> rounds = new ArrayList<>(roundData.size());
        this.unmodifiableRounds = Collections.unmodifiableList(rounds);

        for(SpawnpointInfo spawnpointInfo : info.spawnpoints()) {
            spawnpoints.add(new Spawnpoint(spawnpointInfo, origin, instance, spawnruleMap::get, mobSpawner));
        }

        for(SpawnruleInfo spawnrule : spawnruleData) {
            spawnruleMap.put(spawnrule.id(), spawnrule);
        }

        for(WindowInfo windowInfo : windowData) {
            windows.add(new Window(instance, windowInfo, origin, blockHandler));
        }

        for(DoorInfo doorInfo : doorData) {
            //TODO: include door fill block in mapInfo
            doors.add(new Door(doorInfo, origin, instance, Block.AIR));
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
                for(Region3I subRegion : door.data.regions()) {
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
