package com.github.phantazmnetwork.zombies.scoreboard.sidebar.lineupdater;

import com.github.steanky.element.core.annotation.*;
import com.github.steanky.ethylene.core.ConfigElement;
import com.github.steanky.ethylene.core.collection.ConfigNode;
import com.github.steanky.ethylene.core.processor.ConfigProcessException;
import com.github.steanky.ethylene.core.processor.ConfigProcessor;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BooleanSupplier;

@Model("zombies.sidebar.line_updater.conditional")
public class ConditionalSidebarLineUpdater implements SidebarLineUpdater {


    @Model("zombies.sidebar.line_updater.conditional.child")
    public static class ChildUpdater {

        @DataObject
        public record Data(@NotNull @DataPath("condition") String conditionPath,
                           @NotNull @DataPath("updater") String updaterPath) {

            public Data {
                Objects.requireNonNull(conditionPath, "conditionPath");
                Objects.requireNonNull(updaterPath, "updaterPath");
            }

        }

        private final BooleanSupplier condition;

        private final SidebarLineUpdater updater;

        @FactoryMethod
        public ChildUpdater(@NotNull ChildUpdater.Data data, @NotNull @DataName("condition") BooleanSupplier condition,
                @NotNull @DataName("updater") SidebarLineUpdater updater) {
            this.condition = Objects.requireNonNull(condition, "condition");
            this.updater = Objects.requireNonNull(updater, "updater");
        }

        @ProcessorMethod
        public static @NotNull ConfigProcessor<ChildUpdater.Data> processor() {
            return new ConfigProcessor<>() {
                @Override
                public @NotNull ChildUpdater.Data dataFromElement(@NotNull ConfigElement element)
                        throws ConfigProcessException {
                    String conditionPath = element.getStringOrThrow("condition");
                    String updaterPath = element.getStringOrThrow("updater");
                    return new ChildUpdater.Data(conditionPath, updaterPath);
                }

                @Override
                public @NotNull ConfigElement elementFromData(@NotNull ChildUpdater.Data data) {
                    return ConfigNode.of("condition", data.conditionPath(), "updater", data.updaterPath());
                }
            };
        }

        public @NotNull BooleanSupplier getCondition() {
            return condition;
        }

        public @NotNull SidebarLineUpdater getUpdater() {
            return updater;
        }

    }

    @DataObject
    public record Data(@NotNull @DataPath("child_updaters") List<String> childUpdaterPaths) {

        public Data {
            Objects.requireNonNull(childUpdaterPaths, "childUpdaterPaths");
        }

    }

    private final List<ChildUpdater> childUpdaters;

    @FactoryMethod
    public ConditionalSidebarLineUpdater(@NotNull Data data,
            @NotNull @DataName("child_updaters") List<ChildUpdater> childUpdaters) {
        this.childUpdaters = List.copyOf(childUpdaters);
    }

    @ProcessorMethod
    public static @NotNull ConfigProcessor<Data> processor() {
        return new ConfigProcessor<>() {
            @Override
            public @NotNull Data dataFromElement(@NotNull ConfigElement element) throws ConfigProcessException {
                List<String> childUpdaters = ConfigProcessor.STRING.listProcessor()
                        .dataFromElement(element.getElementOrThrow("child_processors"));
                return new Data(childUpdaters);
            }

            @Override
            public @NotNull ConfigElement elementFromData(@NotNull Data data) throws ConfigProcessException {
                return ConfigProcessor.STRING.listProcessor().elementFromData(data.childUpdaterPaths());
            }
        };
    }

    @Override
    public void invalidateCache() {
        for (ChildUpdater childUpdater : childUpdaters) {
            childUpdater.getUpdater().invalidateCache();
        }
    }

    @Override
    public @NotNull Optional<Component> tick(long time) {
        for (ChildUpdater childUpdater : childUpdaters) {
            if (childUpdater.getCondition().getAsBoolean()) {
                return childUpdater.getUpdater().tick(time);
            }
        }

        return Optional.empty();
    }
}
