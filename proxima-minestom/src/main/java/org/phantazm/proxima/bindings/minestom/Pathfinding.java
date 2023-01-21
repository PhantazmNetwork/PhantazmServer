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
import com.github.steanky.vector.Vec3I;
import com.github.steanky.vector.Vec3I2ObjectMap;
import com.github.steanky.vector.Vec3IBiPredicate;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.EntityType;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class Pathfinding {
    protected final ThreadLocal<Vec3I2ObjectMap<Node>> nodeMapLocal;
    protected final InstanceSpaceHandler spaceHandler;
    protected final EntityType entityType;

    protected Vec3IBiPredicate successPredicate;
    protected NodeSnapper nodeSnapper;
    protected PathLimiter pathLimiter;
    protected Explorer explorer;
    protected Heuristic heuristic;
    protected NodeProcessor nodeProcessor;

    protected PathSettings pathSettings;

    public Pathfinding(@NotNull ThreadLocal<Vec3I2ObjectMap<Node>> nodeMapLocal,
            @NotNull InstanceSpaceHandler spaceHandler, @NotNull EntityType entityType) {
        this.nodeMapLocal = Objects.requireNonNull(nodeMapLocal, "nodeMapLocal");
        this.spaceHandler = Objects.requireNonNull(spaceHandler, "spaceHandler");
        this.entityType = Objects.requireNonNull(entityType, "entityType");
    }

    public @NotNull PathSettings getSettings() {
        return Objects.requireNonNullElseGet(pathSettings, () -> pathSettings = generateSettings());
    }

    protected @NotNull PathSettings generateSettings() {
        Vec3IBiPredicate successPredicate =
                this.successPredicate = Objects.requireNonNull(successPredicate(), "successPredicate");
        this.nodeSnapper = Objects.requireNonNull(nodeSnapper(), "nodeSnapper");
        this.pathLimiter = Objects.requireNonNull(pathLimiter(), "pathLimiter");

        Explorer explorer = this.explorer = Objects.requireNonNull(explorer(), "explorer");
        Heuristic heuristic = this.heuristic = Objects.requireNonNull(heuristic(), "heuristic");

        NodeProcessor nodeProcessor = this.nodeProcessor = Objects.requireNonNull(nodeProcessor(), "nodeProcessor");

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
                return nodeMapLocal.get();
            }

            @Override
            public @NotNull NodeProcessor nodeProcessor() {
                return nodeProcessor;
            }
        };
    }

    protected @NotNull Vec3IBiPredicate successPredicate() {
        return (x1, y1, z1, x2, y2, z2) -> Vec3I.distanceSquared(x1, y1, z1, x2, y2, z2) <= 1;
    }

    protected @NotNull NodeSnapper nodeSnapper() {
        return new BasicNodeSnapper(spaceHandler.space(), entityType.width(), entityType.height(), jumpHeight(),
                fallTolerance(), Vec.EPSILON);
    }

    protected float jumpHeight() {
        return 1.0F;
    }

    protected float fallTolerance() {
        return 4.0F;
    }

    protected @NotNull Explorer explorer() {
        return new WalkExplorer(nodeSnapper, pathLimiter);
    }

    protected @NotNull PathLimiter pathLimiter() {
        return PathLimiter.NO_LIMIT;
    }

    protected @NotNull Heuristic heuristic() {
        return Heuristic.DISTANCE_SQUARED;
    }

    protected @NotNull NodeProcessor nodeProcessor() {
        return NodeProcessor.createDiagonals(nodeSnapper);
    }

    public void resetPathSettings() {
        this.successPredicate = null;
        this.nodeSnapper = null;
        this.pathLimiter = null;
        this.explorer = null;
        this.heuristic = null;
        this.nodeProcessor = null;

        this.pathSettings = null;
    }
}
