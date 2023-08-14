package org.phantazm.mob2;

import com.github.steanky.toolkit.collection.Containers;
import net.minestom.server.coordinate.Point;
import net.minestom.server.entity.Entity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Represents a target of a particular skill.
 */
public sealed interface Target
        permits Target.NoTarget, Target.SinglePointTarget, Target.MultiPointTarget, Target.SingleEntityTarget,
        Target.MultiEntityTarget {
    Target NONE = new NoTarget();

    @NotNull Optional<? extends Point> location();

    @NotNull @Unmodifiable Collection<? extends @NotNull Point> locations();

    @NotNull @Unmodifiable Collection<? extends @NotNull Entity> targets();

    @NotNull Optional<? extends Entity> target();

    static @NotNull Target ofNullable(@Nullable Entity entity) {
        if (entity == null) {
            return NONE;
        }

        return new SingleEntityTarget(entity);
    }

    static @NotNull Target entities() {
        return NONE;
    }

    static @NotNull Target entities(@NotNull Entity entity) {
        Objects.requireNonNull(entity, "entity");
        return new SingleEntityTarget(entity);
    }

    static @NotNull Target entities(@NotNull Entity @NotNull ... entities) {
        Objects.requireNonNull(entities, "entities");
        if (entities.length == 0) {
            return NONE;
        }

        if (entities.length == 1) {
            return new SingleEntityTarget(Objects.requireNonNull(entities[0], "entity"));
        }

        return new MultiEntityTarget(List.of(entities));
    }

    static @NotNull Target entities(@NotNull Collection<? extends @NotNull Entity> entities) {
        List<? extends Entity> copy = List.copyOf(entities);
        if (copy.isEmpty()) {
            return NONE;
        }

        if (copy.size() == 1) {
            return new SingleEntityTarget(copy.get(0));
        }

        return new MultiEntityTarget(copy);
    }

    static @NotNull Target ofNullable(@Nullable Point point) {
        if (point == null) {
            return NONE;
        }

        return new SinglePointTarget(point);
    }

    static @NotNull Target points() {
        return NONE;
    }

    static @NotNull Target points(@NotNull Point point) {
        Objects.requireNonNull(point, "point");
        return new SinglePointTarget(point);
    }

    static @NotNull Target points(@NotNull Point @NotNull ... points) {
        Objects.requireNonNull(points, "points");
        if (points.length == 0) {
            return NONE;
        }

        if (points.length == 1) {
            return new SinglePointTarget(Objects.requireNonNull(points[0], "point"));
        }

        return new MultiPointTarget(List.of(points));
    }

    static @NotNull Target points(@NotNull Collection<? extends @NotNull Point> points) {
        List<? extends Point> copy = List.copyOf(points);
        if (copy.isEmpty()) {
            return NONE;
        }

        if (copy.size() == 1) {
            return new SinglePointTarget(copy.get(0));
        }

        return new MultiPointTarget(copy);
    }

    final class NoTarget implements Target {
        private NoTarget() {
        }

        @Override
        public @NotNull Optional<? extends Point> location() {
            return Optional.empty();
        }

        @Override
        public @NotNull @Unmodifiable Collection<? extends @NotNull Point> locations() {
            return List.of();
        }

        @Override
        public @NotNull @Unmodifiable Collection<? extends @NotNull Entity> targets() {
            return List.of();
        }

        @Override
        public @NotNull Optional<? extends Entity> target() {
            return Optional.empty();
        }
    }

    final class SinglePointTarget implements Target {
        private final Point point;

        private SinglePointTarget(@NotNull Point point) {
            this.point = point;
        }

        @Override
        public @NotNull Optional<? extends Point> location() {
            return Optional.of(point);
        }

        @Override
        public @NotNull @Unmodifiable Collection<? extends @NotNull Point> locations() {
            return List.of(point);
        }

        @Override
        public @NotNull @Unmodifiable Collection<? extends @NotNull Entity> targets() {
            return List.of();
        }

        @Override
        public @NotNull Optional<? extends Entity> target() {
            return Optional.empty();
        }
    }

    final class MultiPointTarget implements Target {
        private final List<? extends Point> points;

        private MultiPointTarget(@NotNull List<? extends Point> points) {
            this.points = points;
        }

        @Override
        public @NotNull Optional<? extends Point> location() {
            if (points.isEmpty()) {
                return Optional.empty();
            }

            return Optional.of(points.get(0));
        }

        @Override
        public @NotNull @Unmodifiable Collection<? extends @NotNull Point> locations() {
            return points;
        }

        @Override
        public @NotNull @Unmodifiable Collection<? extends @NotNull Entity> targets() {
            return List.of();
        }

        @Override
        public @NotNull Optional<? extends Entity> target() {
            return Optional.empty();
        }
    }

    final class SingleEntityTarget implements Target {
        private final Entity entity;

        private SingleEntityTarget(@NotNull Entity entity) {
            this.entity = entity;
        }

        @Override
        public @NotNull Optional<? extends Point> location() {
            return Optional.of(entity.getPosition());
        }

        @Override
        public @NotNull @Unmodifiable Collection<? extends @NotNull Point> locations() {
            return List.of(entity.getPosition());
        }

        @Override
        public @NotNull @Unmodifiable Collection<? extends Entity> targets() {
            return List.of(entity);
        }

        @Override
        public @NotNull Optional<? extends Entity> target() {
            return Optional.of(entity);
        }
    }

    final class MultiEntityTarget implements Target {
        private final List<? extends Entity> entities;

        private MultiEntityTarget(@NotNull List<? extends Entity> entities) {
            this.entities = entities;
        }

        @Override
        public @NotNull Optional<? extends Point> location() {
            if (entities.isEmpty()) {
                return Optional.empty();
            }

            return Optional.of(entities.get(0).getPosition());
        }

        @Override
        public @NotNull @Unmodifiable Collection<? extends @NotNull Point> locations() {
            if (entities.isEmpty()) {
                return List.of();
            }

            Point[] points = new Point[entities.size()];
            for (int i = 0; i < points.length; i++) {
                points[i] = entities.get(i).getPosition();
            }

            return Containers.arrayView(points);
        }

        @Override
        public @NotNull @Unmodifiable Collection<? extends Entity> targets() {
            return entities;
        }

        @Override
        public @NotNull Optional<? extends Entity> target() {
            if (entities.isEmpty()) {
                return Optional.empty();
            }

            return Optional.of(entities.get(0));
        }
    }
}
