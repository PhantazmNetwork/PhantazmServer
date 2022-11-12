package com.github.phantazmnetwork.zombies.scoreboard.sidebar.lineupdater;

import com.github.phantazmnetwork.commons.ConfigProcessors;
import com.github.steanky.element.core.annotation.DataObject;
import com.github.steanky.element.core.annotation.FactoryMethod;
import com.github.steanky.element.core.annotation.Model;
import com.github.steanky.element.core.annotation.ProcessorMethod;
import com.github.steanky.ethylene.core.ConfigElement;
import com.github.steanky.ethylene.core.collection.ConfigNode;
import com.github.steanky.ethylene.core.processor.ConfigProcessException;
import com.github.steanky.ethylene.core.processor.ConfigProcessor;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.Optional;

@Model("zombies.sidebar.lineupdater.constant")
public class ConstantSidebarLineUpdater implements SidebarLineUpdater {

    @DataObject
    public record Data(@NotNull Component component) {

        public Data {
            Objects.requireNonNull(component, "component");
        }

    }

    private final Data data;

    private boolean componentSet = false;

    @FactoryMethod
    public ConstantSidebarLineUpdater(@NotNull Data data) {
        this.data = Objects.requireNonNull(data, "data");
    }

    @ProcessorMethod
    public static @NotNull ConfigProcessor<Data> processor() {
        ConfigProcessor<Component> componentProcessor = ConfigProcessors.component();
        return new ConfigProcessor<>() {
            @Override
            public Data dataFromElement(@NotNull ConfigElement element) throws ConfigProcessException {
                Component component = componentProcessor.dataFromElement(element.getElementOrThrow("component"));
                return new Data(component);
            }

            @Override
            public @NotNull ConfigElement elementFromData(Data data) throws ConfigProcessException {
                return ConfigNode.of("component", componentProcessor.elementFromData(data.component()));
            }
        };
    }

    @Override
    public void invalidateCache() {
        componentSet = false;
    }

    @Override
    public @NotNull Optional<Component> tick(long time) {
        if (!componentSet) {
            componentSet = true;
            return Optional.of(data.component());
        }

        return Optional.empty();
    }
}
