package org.phantazm.mob2;

import com.github.steanky.toolkit.collection.Containers;
import net.minestom.server.coordinate.Point;
import net.minestom.server.entity.Entity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.util.*;
import java.util.function.Consumer;

/**
 * Represents a target of a particular skill.
 */
public sealed interface Target
    permits Target.NoTarget, Target.SinglePointTarget, Target.MultiPointTarget, Target.SingleEntityTarget,
    Target.MultiEntityTarget, Target.EntryListTarget {
    Target NONE = new NoTarget();

    record TargetEntry(@Nullable Point point,
        @Nullable Entity entity) {
        public TargetEntry {
            if (point == null) {
                Objects.requireNonNull(entity);
            }

            if (entity == null) {
                Objects.requireNonNull(point);
            }

            if (point != null && entity != null) {
                throw new IllegalArgumentException("can't define both a point and an entity");
            }
        }

        @SuppressWarnings("DataFlowIssue")
        public @NotNull Point point() {
            return point != null ? point : entity.getPosition();
        }

        public @NotNull Optional<Entity> entityOptional() {
            return Optional.ofNullable(entity);
        }

        public boolean isEntity() {
            return entity != null;
        }

        public boolean isPoint() {
            return point != null;
        }
    }

    static @NotNull TargetEntry entry(@NotNull Entity entity) {
        return new TargetEntry(null, entity);
    }

    static @NotNull TargetEntry entry(@NotNull Point point) {
        return new TargetEntry(point, null);
    }

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
        Objects.requireNonNull(entity);
        return new SingleEntityTarget(entity);
    }

    static @NotNull Target entities(@NotNull Entity @NotNull ... entities) {
        Objects.requireNonNull(entities);
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
        Objects.requireNonNull(point);
        return new SinglePointTarget(point);
    }

    static @NotNull Target points(@NotNull Point @NotNull ... points) {
        Objects.requireNonNull(points);
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

    static @NotNull Target entries(@NotNull TargetEntry entry) {
        Objects.requireNonNull(entry);
        return new EntryListTarget(List.of(entry));
    }

    static @NotNull Target entries(@NotNull TargetEntry... entries) {
        if (entries.length == 0) {
            return Target.NONE;
        }

        return new EntryListTarget(List.of(entries));
    }

    static @NotNull Target entries(@NotNull Collection<? extends @NotNull TargetEntry> entries) {
        List<TargetEntry> copy = List.copyOf(entries);
        if (copy.isEmpty()) {
            return Target.NONE;
        }

        return new EntryListTarget(copy);
    }

    @NotNull
    Optional<? extends Point> location();

    @NotNull
    @Unmodifiable
    Collection<? extends @NotNull Point> locations();

    @NotNull
    @Unmodifiable
    Collection<? extends @NotNull Entity> targets();

    @NotNull @Unmodifiable Collection<TargetEntry> entries();

    @NotNull
    Optional<? extends Entity> target();

    default <T extends Entity> void forType(@NotNull Class<T> type, @NotNull Consumer<? super T> consumer) {
        Objects.requireNonNull(type);
        Objects.requireNonNull(consumer);

        Collection<? extends Entity> targets = targets();
        for (Entity targetEntity : targets) {
            if (type.isAssignableFrom(targetEntity.getClass())) {
                consumer.accept(type.cast(targetEntity));
            }
        }
    }

    default <T extends Entity> @NotNull Optional<T> forType(@NotNull Class<T> type) {
        Objects.requireNonNull(type);

        Optional<? extends Entity> target = target();
        if (target.isEmpty()) {
            return Optional.empty();
        }

        Entity entity = target.get();
        if (type.isAssignableFrom(entity.getClass())) {
            return Optional.of(type.cast(entity));
        }

        return Optional.empty();
    }

    final class EntryListTarget implements Target {
        private final List<TargetEntry> entries;

        private EntryListTarget(@NotNull List<TargetEntry> entries) {
            this.entries = entries;
        }

        @Override
        public @NotNull Optional<? extends Point> location() {
            return Optional.of(entries.get(0).point());
        }

        @Override
        public @NotNull @Unmodifiable Collection<? extends @NotNull Point> locations() {
            return Containers.mappedView(TargetEntry::point, entries);
        }

        @Override
        public @NotNull @Unmodifiable Collection<? extends @NotNull Entity> targets() {
            List<Entity> targets = new ArrayList<>(entries.size());
            for (TargetEntry targetEntry : entries) {
                Entity entity = targetEntry.entity;
                if (entity == null) {
                    continue;
                }

                targets.add(entity);
            }

            return List.copyOf(targets);
        }

        @Override
        public @NotNull @Unmodifiable Collection<TargetEntry> entries() {
            return entries;
        }

        @Override
        public @NotNull Optional<? extends Entity> target() {
            for (TargetEntry entry : entries) {
                Entity entity = entry.entity;
                if (entity != null) {
                    return Optional.of(entity);
                }
            }

            return Optional.empty();
        }
    }

    final class NoTarget implements Target {
        private NoTarget() {
        }

        @Override
        public @NotNull Optional<? extends Point> location() {
            return Optional.empty();
        }

        @Override
        public @NotNull
        @Unmodifiable Collection<? extends @NotNull Point> locations() {
            return List.of();
        }

        @Override
        public @NotNull
        @Unmodifiable Collection<? extends @NotNull Entity> targets() {
            return List.of();
        }

        @Override
        public @NotNull @Unmodifiable Collection<TargetEntry> entries() {
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
        public @NotNull
        @Unmodifiable Collection<? extends @NotNull Point> locations() {
            return List.of(point);
        }

        @Override
        public @NotNull
        @Unmodifiable Collection<? extends @NotNull Entity> targets() {
            return List.of();
        }

        @Override
        public @NotNull @Unmodifiable Collection<TargetEntry> entries() {
            return List.of(new TargetEntry(point, null));
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
            return Optional.of(points.get(0));
        }

        @Override
        public @NotNull
        @Unmodifiable Collection<? extends @NotNull Point> locations() {
            return points;
        }

        @Override
        public @NotNull
        @Unmodifiable Collection<? extends @NotNull Entity> targets() {
            return List.of();
        }

        @Override
        public @NotNull @Unmodifiable Collection<TargetEntry> entries() {
            return Containers.mappedView(point -> new TargetEntry(point, null), points);
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
        public @NotNull
        @Unmodifiable Collection<? extends @NotNull Point> locations() {
            return List.of(entity.getPosition());
        }

        @Override
        public @NotNull
        @Unmodifiable Collection<? extends Entity> targets() {
            return List.of(entity);
        }

        @Override
        public @NotNull @Unmodifiable Collection<TargetEntry> entries() {
            return List.of(new TargetEntry(null, entity));
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
            return Optional.of(entities.get(0).getPosition());
        }

        @Override
        public @NotNull
        @Unmodifiable Collection<? extends @NotNull Point> locations() {
            Point[] points = new Point[entities.size()];
            for (int i = 0; i < points.length; i++) {
                points[i] = entities.get(i).getPosition();
            }

            return Containers.arrayView(points);
        }

        @Override
        public @NotNull
        @Unmodifiable Collection<? extends Entity> targets() {
            return entities;
        }

        @Override
        public @NotNull @Unmodifiable Collection<TargetEntry> entries() {
            return Containers.mappedView(entity -> new TargetEntry(null, entity), entities);
        }

        @Override
        public @NotNull Optional<? extends Entity> target() {
            return Optional.of(entities.get(0));
        }
    }
}
