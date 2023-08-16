package org.phantazm.mob2.skill;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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

        @Nullable String trigger();
    }

    static @NotNull Entry entry(@NotNull Skill skill) {
        Objects.requireNonNull(skill);
        return new EntryImpl(skill, null);
    }

    static @NotNull Entry entry(@NotNull Skill skill, @Nullable String string) {
        Objects.requireNonNull(skill);
        return new EntryImpl(skill, string);
    }

    final class EntryImpl implements Entry {
        private final Skill skill;
        private final String string;

        private EntryImpl(Skill skill, String string) {
            this.skill = skill;
            this.string = string;
        }

        @Override
        public @NotNull Skill skill() {
            return skill;
        }

        @Override
        public @Nullable String trigger() {
            return string;
        }
    }
}
