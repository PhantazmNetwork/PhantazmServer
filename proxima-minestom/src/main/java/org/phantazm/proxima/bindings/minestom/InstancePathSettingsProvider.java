package org.phantazm.proxima.bindings.minestom;

import com.github.steanky.proxima.Heuristic;
import com.github.steanky.proxima.PathLimiter;
import com.github.steanky.proxima.explorer.Explorer;
import com.github.steanky.proxima.explorer.WalkExplorer;
import com.github.steanky.proxima.node.Node;
import com.github.steanky.proxima.node.NodeProcessor;
import com.github.steanky.proxima.path.PathSettings;
import com.github.steanky.proxima.snapper.BasicNodeSnapper;
import com.github.steanky.proxima.snapper.NodeSnapper;
import com.github.steanky.vector.Bounds3I;
import com.github.steanky.vector.HashVec3I2ObjectMap;
import com.github.steanky.vector.Vec3I2ObjectMap;
import com.github.steanky.vector.Vec3IBiPredicate;
import net.minestom.server.collision.BoundingBox;
import net.minestom.server.entity.Entity;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

public class InstancePathSettingsProvider implements PathSettingsProvider {
    private static final double EPSILON = 1E-6;

    private final InstanceSpaceHandler spaceHandler;
    private final Map<String, PathSettings> settingsMap;
    private final PathLimiter pathLimiter;
    private final Heuristic heuristic;
    private final ThreadLocal<Vec3I2ObjectMap<Node>> localGraphs;

    public InstancePathSettingsProvider(@NotNull InstanceSpaceHandler spaceHandler, @NotNull Bounds3I searchBounds,
            @NotNull Heuristic heuristic) {
        this.spaceHandler = Objects.requireNonNull(spaceHandler, "spaceHandler");
        this.settingsMap = new ConcurrentHashMap<>();
        this.pathLimiter = PathLimiter.inBounds(searchBounds);
        this.heuristic = Objects.requireNonNull(heuristic, "heuristic");
        this.localGraphs = ThreadLocal.withInitial(() -> new HashVec3I2ObjectMap<>(searchBounds));
    }

    @Override
    public @NotNull PathSettings groundSettings(@NotNull String name, @NotNull Entity entity, float jumpHeight,
            float fallTolerance, @NotNull Vec3IBiPredicate successPredicate) {
        Objects.requireNonNull(successPredicate, "successPredicate");

        BoundingBox bounds = entity.getBoundingBox();

        return settingsMap.computeIfAbsent(name, ignored -> {
            NodeSnapper nodeSnapper =
                    new BasicNodeSnapper(spaceHandler.space(), bounds.width(), bounds.height(), jumpHeight,
                            fallTolerance, EPSILON);
            Explorer explorer = new WalkExplorer(nodeSnapper, pathLimiter);
            NodeProcessor nodeProcessor = NodeProcessor.createDiagonals(nodeSnapper);

            return new PathSettings() {
                @Override
                public @NotNull Vec3IBiPredicate successPredicate() {
                    return successPredicate;
                }

                @Override
                public @NotNull Explorer explorer() {
                    return explorer;
                }

                @Override
                public @NotNull Heuristic heuristic() {
                    return heuristic;
                }

                @Override
                public @NotNull Vec3I2ObjectMap<Node> graph() {
                    return localGraphs.get();
                }

                @Override
                public @NotNull NodeProcessor nodeProcessor() {
                    return nodeProcessor;
                }
            };
        });
    }
}
