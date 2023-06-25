package org.phantazm.proxima.bindings.minestom;

import com.github.steanky.proxima.Heuristic;
import com.github.steanky.proxima.Navigator;
import com.github.steanky.proxima.PathLimiter;
import com.github.steanky.proxima.explorer.Explorer;
import com.github.steanky.proxima.explorer.WalkExplorer;
import com.github.steanky.proxima.node.Node;
import com.github.steanky.proxima.node.NodeProcessor;
import com.github.steanky.proxima.path.BasicNavigator;
import com.github.steanky.proxima.path.PathResult;
import com.github.steanky.proxima.path.PathSettings;
import com.github.steanky.proxima.path.Pathfinder;
import com.github.steanky.proxima.resolver.PositionResolver;
import com.github.steanky.proxima.snapper.BasicNodeSnapper;
import com.github.steanky.proxima.snapper.NodeSnapper;
import com.github.steanky.vector.Vec3D;
import com.github.steanky.vector.Vec3I2ObjectMap;
import com.github.steanky.vector.Vec3IBiPredicate;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.*;
import org.jetbrains.annotations.NotNull;
import org.phantazm.proxima.bindings.minestom.controller.Controller;
import org.phantazm.proxima.bindings.minestom.controller.GroundController;

import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.BiPredicate;

public class Pathfinding {
    public interface Factory {
        @NotNull Pathfinding make(@NotNull Pathfinder pathfinder,
                @NotNull ThreadLocal<Vec3I2ObjectMap<Node>> nodeMapLocal, @NotNull InstanceSpaceHandler spaceHandler,
                @NotNull EntityType entityType);
    }

    protected final Pathfinder pathfinder;
    protected final ThreadLocal<Vec3I2ObjectMap<Node>> nodeMapLocal;
    protected final InstanceSpaceHandler spaceHandler;
    protected final EntityType entityType;

    protected Vec3IBiPredicate successPredicate;
    protected NodeSnapper nodeSnapper;
    protected PathLimiter pathLimiter;
    protected Explorer explorer;
    protected Heuristic heuristic;
    protected NodeProcessor nodeProcessor;

    protected Navigator navigator;
    protected PathSettings pathSettings;
    protected Controller controller;

    public Pathfinding(@NotNull Pathfinder pathfinder, @NotNull ThreadLocal<Vec3I2ObjectMap<Node>> nodeMapLocal,
            @NotNull InstanceSpaceHandler spaceHandler, @NotNull EntityType entityType) {
        this.pathfinder = Objects.requireNonNull(pathfinder, "pathfinder");
        this.nodeMapLocal = Objects.requireNonNull(nodeMapLocal, "nodeMapLocal");
        this.spaceHandler = Objects.requireNonNull(spaceHandler, "spaceHandler");
        this.entityType = Objects.requireNonNull(entityType, "entityType");
    }

    public @NotNull Navigator getNavigator() {
        return Objects.requireNonNullElseGet(navigator,
                () -> navigator = new BasicNavigator(pathfinder, getSettings()));
    }

    public @NotNull PathSettings getSettings() {
        return Objects.requireNonNullElseGet(pathSettings, () -> pathSettings = generateSettings());
    }

    public @NotNull Controller getController(@NotNull LivingEntity livingEntity) {
        return Objects.requireNonNullElseGet(controller,
                () -> controller = new GroundController(livingEntity, stepHeight()));
    }

    public @NotNull PositionResolver positionResolverForTarget(@NotNull Entity entity) {
        EntityType type = entity.getEntityType();
        return PositionResolver.asIfByInitial(
                new BasicNodeSnapper(spaceHandler.space(), type.width(), type.height(), fallTolerance(), jumpHeight(),
                        Vec.EPSILON), 16, type.width(), Vec.EPSILON);
    }

    public @NotNull BiPredicate<Vec3D, Vec3D> targetChangePredicate(@NotNull Entity entity) {
        return (oldPosition, newPosition) -> oldPosition.distanceSquaredTo(newPosition) > 2;
    }

    public boolean isValidTarget(@NotNull Entity targetEntity) {
        boolean entityValid = !targetEntity.isRemoved() && targetEntity.getInstance() == spaceHandler.instance();
        if (entityValid && targetEntity instanceof Player player) {
            GameMode mode = player.getGameMode();
            return !(mode == GameMode.CREATIVE || mode == GameMode.SPECTATOR);
        }

        return entityValid;
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
        return (x1, y1, z1, x2, y2, z2) -> x1 == x2 && y1 == y2 && z1 == z2;
    }

    protected @NotNull NodeSnapper nodeSnapper() {
        return new BasicNodeSnapper(spaceHandler.space(), entityType.width(), entityType.height(), fallTolerance(),
                jumpHeight(), Vec.EPSILON);
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

    public boolean canPathfind(@NotNull ProximaEntity proximaEntity) {
        return proximaEntity.isOnGround();
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
        this.navigator = null;
    }

    protected float jumpHeight() {
        return 1.0F;
    }

    protected float fallTolerance() {
        return 4.0F;
    }

    protected float stepHeight() {
        return 0.5F;
    }

    public long immobileThreshold() {
        return 100L;
    }

    public long recalculationDelay(@NotNull PathResult pathResult) {
        //randomize path recalculation delay
        double factor = ThreadLocalRandom.current().nextDouble() + 0.5;

        long count = pathResult.isSuccessful() ? pathResult.exploredCount() : pathResult.exploredCount() * 2L;
        return Math.min((long)(count * factor), 3000);
    }
}
