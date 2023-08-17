package org.phantazm.mob2.skill;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.phantazm.mob2.trigger.Trigger;

import java.util.Objects;

public interface Skill {
    default void init() {
    }

    default void use() {
    }

    default void tick() {
    }

    default boolean needsTicking() {
        return false;
    }

    default void end() {
    }

    sealed interface Entry {
        @NotNull Skill skill();

        @Nullable Trigger trigger();
    }

    static @NotNull Entry entry(@NotNull Skill skill) {
        Objects.requireNonNull(skill);
        return new EntryImpl(skill, null);
    }

    static @NotNull Entry entry(@NotNull Skill skill, @Nullable Trigger trigger) {
        Objects.requireNonNull(skill);
        return new EntryImpl(skill, trigger);
    }

    final class EntryImpl implements Entry {
        private final Skill skill;
        private final Trigger trigger;

        private EntryImpl(Skill skill, Trigger trigger) {
            this.skill = skill;
            this.trigger = trigger;
        }

        @Override
        public @NotNull Skill skill() {
            return skill;
        }

        @Override
        public @Nullable Trigger trigger() {
            return trigger;
        }
    }
}
