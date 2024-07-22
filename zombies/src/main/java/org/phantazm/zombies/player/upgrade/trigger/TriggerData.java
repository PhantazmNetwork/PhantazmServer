package org.phantazm.zombies.player.upgrade.trigger;

import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

public sealed interface TriggerData permits TriggerData.Impl {
    final class Impl implements TriggerData {
        private final Object object;

        private Impl(Object object) {
            this.object = object;
        }

        @Override
        public <T> void consume(@NotNull Class<T> cls, @NotNull Consumer<? super T> consumer) {
            if (cls.isAssignableFrom(object.getClass())) {
                consumer.accept(cls.cast(object));
            }
        }

        @Override
        public @NotNull Object raw() {
            return object;
        }
    }

    <T> void consume(@NotNull Class<T> cls, @NotNull Consumer<? super T> consumer);

    @NotNull Object raw();

    static @NotNull TriggerData of(@NotNull Object object) {
        return new Impl(object);
    }
}
