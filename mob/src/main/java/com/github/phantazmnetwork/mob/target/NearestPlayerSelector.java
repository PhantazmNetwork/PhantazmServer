package com.github.phantazmnetwork.mob.target;

import com.github.steanky.element.core.annotation.*;
import com.github.steanky.ethylene.core.ConfigElement;
import com.github.steanky.ethylene.core.collection.ConfigNode;
import com.github.steanky.ethylene.core.processor.ConfigProcessException;
import com.github.steanky.ethylene.core.processor.ConfigProcessor;
import net.minestom.server.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Iterator;
import java.util.Objects;

@Model("mob.selector.nearest_player")
public class NearestPlayerSelector extends FirstTargetSelector<Player> {

    @DataObject
    public record Data(@NotNull @DataPath("selector") String selectorPath) {

        public Data {
            Objects.requireNonNull(selectorPath, "selectorPath");
        }

    }

    /**
     * Creates a new {@link MappedSelector}.
     *
     * @param delegate The delegate {@link TargetSelector} to map
     */
    @FactoryMethod
    public NearestPlayerSelector(@NotNull Data data,
            @NotNull @DataName("selector") TargetSelector<Iterable<Player>> delegate) {
        super(delegate);
    }

    @ProcessorMethod
    public static @NotNull ConfigProcessor<Data> processor() {
        return new ConfigProcessor<Data>() {
            @Override
            public @NotNull Data dataFromElement(@NotNull ConfigElement element) throws ConfigProcessException {
                String selectorPath = element.getStringOrThrow("selectorPath");
                return new Data(selectorPath);
            }

            @Override
            public @NotNull ConfigElement elementFromData(@NotNull Data data) {
                return ConfigNode.of("selectorPath", data.selectorPath());
            }
        };
    }

    @Override
    protected Player map(@NotNull Iterable<Player> players) {
        Iterator<Player> iterator = players.iterator();
        if (iterator.hasNext()) {
            return iterator.next();
        }

        return null;
    }
}
