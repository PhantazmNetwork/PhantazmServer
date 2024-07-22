package org.phantazm.zombies.player.upgrade.effect;

import org.jetbrains.annotations.NotNull;
import org.phantazm.zombies.player.ZombiesPlayer;
import org.phantazm.zombies.player.upgrade.PlayerUpgrade;
import org.phantazm.zombies.player.upgrade.trigger.TriggerData;

import java.util.HashMap;
import java.util.Map;

public class MultivariateEffect implements UpgradeEffect {
    public interface HandlerFunction<T> {
        void accept(@NotNull PlayerUpgrade upgrade, @NotNull ZombiesPlayer zombiesPlayer, @NotNull T t);
    }

    public interface Handler<T> extends HandlerFunction<T> {
        @NotNull Class<T> type();

        static <T> @NotNull Handler<T> of(Class<T> cls, @NotNull HandlerFunction<? super T> function) {
            return new Handler<>() {
                @Override
                public @NotNull Class<T> type() {
                    return cls;
                }

                @Override
                public void accept(@NotNull PlayerUpgrade upgrade, @NotNull ZombiesPlayer zombiesPlayer, @NotNull T t) {
                    function.accept(upgrade, zombiesPlayer, t);
                }
            };
        }
    }

    private static <T> void accept(@NotNull PlayerUpgrade upgrade, @NotNull ZombiesPlayer zombiesPlayer,
        @NotNull Handler<T> handler, Object object) {
        handler.accept(upgrade, zombiesPlayer, handler.type().cast(object));
    }

    public record HandlerEntry<T>(@NotNull Class<T> type,
        @NotNull Handler<T> handler) {
    }

    static @NotNull <T> HandlerEntry<T> entry(@NotNull Class<T> type, @NotNull Handler<T> handler) {
        return new HandlerEntry<>(type, handler);
    }

    static @NotNull Map<Class<?>, Handler<?>> ofEntries(@NotNull HandlerEntry<?>... entries) {
        Map<Class<?>, Handler<?>> handlerMap = new HashMap<>(entries.length);
        for (HandlerEntry<?> entry : entries) {
            handlerMap.put(entry.type, entry.handler);
        }

        return Map.copyOf(handlerMap);
    }

    private final Map<Class<?>, Handler<?>> handlerMap;

    public MultivariateEffect(@NotNull Map<Class<?>, Handler<?>> handlerMap) {
        this.handlerMap = Map.copyOf(handlerMap);
    }

    @Override
    public void apply(@NotNull PlayerUpgrade upgrade, @NotNull ZombiesPlayer zombiesPlayer,
        @NotNull TriggerData triggerData) {
        Object raw = triggerData.raw();
        Handler<?> handler = handlerMap.get(raw.getClass());
        if (handler != null) {
            accept(upgrade, zombiesPlayer, handler, raw);
        }
    }
}
