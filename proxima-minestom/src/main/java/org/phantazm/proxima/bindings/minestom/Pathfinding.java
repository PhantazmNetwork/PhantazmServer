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
import net.minestom.server.collision.BoundingBox;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.GameMode;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.phantazm.proxima.bindings.minestom.controller.Controller;
import org.phantazm.proxima.bindings.minestom.controller.GroundController;

import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.BiPredicate;

public class Pathfinding {
    public interface Penalty {
        Penalty NONE = (x, y, z, h) -> h;

        float calculate(int x, int y, int z, float h);
    }

    public static final double PLAYER_PATH_EPSILON = 0.0005;
    public static final double MOB_PATH_EPSILON = 1E-6;
    public static final double PLAYER_PATH_EPSILON_DOWNWARDS = MOB_PATH_EPSILON;

    public interface Factory {
        @NotNull
        Pathfinding make(@NotNull Pathfinder pathfinder,
            @NotNull ThreadLocal<Vec3I2ObjectMap<Node>> nodeMapLocal, @NotNull InstanceSpaceHandler spaceHandler);
    }

    protected final Pathfinder pathfinder;
    protected final ThreadLocal<Vec3I2ObjectMap<Node>> nodeMapLocal;
    protected final InstanceSpaceHandler spaceHandler;

    protected Entity self;
    protected Entity target;
    protected Penalty penalty;

    protected BoundingBox lastBoundingBox;
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
        @NotNull InstanceSpaceHandler spaceHandler) {
        this.pathfinder = Objects.requireNonNull(pathfinder);
        this.nodeMapLocal = Objects.requireNonNull(nodeMapLocal);
        this.spaceHandler = Objects.requireNonNull(spaceHandler);
        this.penalty = Penalty.NONE;
    }

    private void clearState() {
        this.lastBoundingBox = null;
        this.successPredicate = null;
        this.nodeSnapper = null;
        this.pathLimiter = null;
        this.explorer = null;
        this.heuristic = null;
        this.nodeProcessor = null;
        this.navigator = null;
        this.pathSettings = null;
        this.controller = null;
    }

    public void setPenalty(@NotNull Penalty penalty) {
        Objects.requireNonNull(penalty);
        if (penalty == this.penalty) {
            return;
        }

        clearState();
        this.penalty = penalty;
    }

    public void setSelf(@NotNull Entity self) {
        this.self = self;
    }

    public void setTarget(@Nullable Entity target) {
        this.target = target;
    }

    public void cancel() {
        Navigator navigator = this.navigator;
        if (navigator != null) {
            navigator.cancel();
        }
    }

    public @NotNull Navigator getNavigator(@NotNull BoundingBox boundingBox) {
        if (lastBoundingBox != null && !boundingBox.equals(lastBoundingBox)) {
            cancel();
            return navigator =
                       new BasicNavigator(pathfinder,
                           pathSettings = generateSettings(this.lastBoundingBox = boundingBox));
        }

        return Objects.requireNonNullElseGet(navigator, () -> navigator =
                                                                  new BasicNavigator(pathfinder,
                                                                      pathSettings = generateSettings(
                                                                          this.lastBoundingBox = boundingBox)));
    }

    public @NotNull PathSettings getSettings(@NotNull BoundingBox boundingBox) {
        if (lastBoundingBox != null && !boundingBox.equals(lastBoundingBox)) {
            cancel();
            return pathSettings = generateSettings(this.lastBoundingBox = boundingBox);
        }

        return Objects.requireNonNullElseGet(pathSettings,
            () -> pathSettings = generateSettings(this.lastBoundingBox = boundingBox));
    }

    public @NotNull Controller getController(@NotNull LivingEntity livingEntity) {
        return Objects.requireNonNullElseGet(controller,
            () -> controller = new GroundController(livingEntity, stepHeight(), jumpHeight()));
    }

    public @NotNull PositionResolver positionResolverForTarget(@NotNull Entity entity) {
        BoundingBox boundingBox = entity.getBoundingBox();
        return PositionResolver.asIfByInitial(
            new BasicNodeSnapper(spaceHandler.space(), boundingBox.width(), boundingBox.height(), fallTolerance(),
                jumpHeight(), PLAYER_PATH_EPSILON), 16, boundingBox.width(), PLAYER_PATH_EPSILON_DOWNWARDS);
    }

    public @NotNull BiPredicate<Vec3D, Vec3D> targetChangePredicate(@NotNull Entity entity) {
        return (oldPosition, newPosition) -> oldPosition.distanceSquaredTo(newPosition) > 2;
    }

    public boolean isValidTarget(@NotNull Entity targetEntity) {
        boolean entityValid = !targetEntity.isRemoved() && targetEntity.getInstance() == spaceHandler.instance();
        if (entityValid && targetEntity instanceof Player player) {
            GameMode mode = player.getGameMode();
            return mode != GameMode.SPECTATOR;
        }

        return entityValid;
    }

    protected @NotNull PathSettings generateSettings(@NotNull BoundingBox boundingBox) {
        Vec3IBiPredicate successPredicate =
            this.successPredicate = Objects.requireNonNull(successPredicate(), "successPredicate");
        this.nodeSnapper = Objects.requireNonNull(nodeSnapper(boundingBox), "nodeSnapper");
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

    protected @NotNull NodeSnapper nodeSnapper(@NotNull BoundingBox boundingBox) {
        return new BasicNodeSnapper(spaceHandler.space(), boundingBox.width(), boundingBox.height(), fallTolerance(),
            jumpHeight(), MOB_PATH_EPSILON);
    }

    protected @NotNull Explorer explorer() {
        return new WalkExplorer(nodeSnapper, pathLimiter);
    }

    protected @NotNull PathLimiter pathLimiter() {
        return PathLimiter.NO_LIMIT;
    }

    protected @NotNull Heuristic heuristic() {
        return (fromX, fromY, fromZ, toX, toY, toZ) -> {
            float h = Heuristic.DISTANCE_SQUARED.heuristic(fromX, fromY, fromZ, toX, toY, toZ);
            return penalty.calculate(fromX, fromY, fromZ, h);
        };
    }

    public boolean canPathfind(@NotNull ProximaEntity proximaEntity) {
        return proximaEntity.isOnGround();
    }

    public boolean useSynthetic() {
        return true;
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

        long count = pathResult.isSuccessful() ? pathResult.exploredCount():pathResult.exploredCount() * 2L;
        return Math.min((long) (count * factor), 3000);
    }
}
