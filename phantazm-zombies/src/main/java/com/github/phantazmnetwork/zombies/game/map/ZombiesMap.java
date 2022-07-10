package com.github.phantazmnetwork.zombies.game.map;

import com.github.phantazmnetwork.api.ClientBlockHandler;
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

public class ZombiesMap extends MapObject<MapInfo> {
    private final List<Spawnpoint> unmodifiableSpawnpoints;
    private final List<Window> unmodifiableWindows;
    private final List<Door> unmodifiableDoors;

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

        Map<Key, SpawnruleInfo> spawnruleMap = new HashMap<>(spawnruleData.size());

        List<Spawnpoint> spawnpoints = new ArrayList<>(spawnpointData.size());
        this.unmodifiableSpawnpoints = Collections.unmodifiableList(spawnpoints);

        List<Window> windows = new ArrayList<>(windowData.size());
        this.unmodifiableWindows = Collections.unmodifiableList(windows);

        List<Door> doors = new ArrayList<>(doorData.size());
        this.unmodifiableDoors = Collections.unmodifiableList(doors);

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
}