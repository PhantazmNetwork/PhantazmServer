package com.github.phantazmnetwork.zombies.game.map;

import com.github.phantazmnetwork.api.ClientBlockHandler;
import com.github.phantazmnetwork.commons.factory.DependencyProvider;
import com.github.phantazmnetwork.commons.vector.Vec3D;
import com.github.phantazmnetwork.commons.vector.Vec3I;
import com.github.phantazmnetwork.mob.spawner.MobSpawner;
import com.github.phantazmnetwork.zombies.map.MapInfo;
import com.github.phantazmnetwork.zombies.map.SpawnpointInfo;
import com.github.phantazmnetwork.zombies.map.SpawnruleInfo;
import com.github.phantazmnetwork.zombies.map.WindowInfo;
import net.kyori.adventure.key.Key;
import net.minestom.server.coordinate.Point;
import net.minestom.server.instance.Instance;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.UnmodifiableView;

import java.util.*;

public class ZombiesMap extends MapObject<MapInfo> {
    private final List<Spawnpoint> unmodifiableSpawnpoints;
    private final List<Window> unmodifiableWindows;

    /**
     * Constructs a new instance of this class.
     *
     * @param info     the backing data object
     * @param origin   the origin vector this object's coordinates are considered relative to
     * @param instance the instance which this MapObject is in
     */
    public ZombiesMap(@NotNull MapInfo info, @NotNull Vec3I origin, @NotNull Instance instance,
                      @NotNull MobSpawner mobSpawner, @NotNull ClientBlockHandler blockHandler) {
        super(info, origin, instance);

        List<SpawnruleInfo> spawnruleData = info.spawnrules();
        List<SpawnpointInfo> spawnpointData = info.spawnpoints();
        List<WindowInfo> windowData = info.windows();

        Map<Key, SpawnruleInfo> spawnruleMap = new HashMap<>(spawnruleData.size());

        List<Spawnpoint> spawnpoints = new ArrayList<>(spawnpointData.size());
        this.unmodifiableSpawnpoints = Collections.unmodifiableList(spawnpoints);

        List<Window> windows = new ArrayList<>(windowData.size());
        this.unmodifiableWindows = Collections.unmodifiableList(windows);

        for(SpawnpointInfo spawnpointInfo : info.spawnpoints()) {
            spawnpoints.add(new Spawnpoint(spawnpointInfo, origin, instance, spawnruleMap::get, mobSpawner));
        }

        for(SpawnruleInfo spawnrule : spawnruleData) {
            spawnruleMap.put(spawnrule.id(), spawnrule);
        }

        for(WindowInfo windowInfo : windowData) {
            windows.add(new Window(instance, windowInfo, origin, blockHandler));
        }
    }

    public @UnmodifiableView @NotNull List<Spawnpoint> getSpawnpoints() {
        return unmodifiableSpawnpoints;
    }

    public @UnmodifiableView @NotNull List<Window> getWindows() {
        return unmodifiableWindows;
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
}
