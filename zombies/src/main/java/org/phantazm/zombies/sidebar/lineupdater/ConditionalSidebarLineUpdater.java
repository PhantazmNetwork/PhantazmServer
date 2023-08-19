package org.phantazm.zombies.sidebar.lineupdater;

import com.github.steanky.element.core.annotation.*;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BooleanSupplier;

@Model("zombies.sidebar.line_updater.conditional")
@Cache(false)
public class ConditionalSidebarLineUpdater implements SidebarLineUpdater {


    private final List<ChildUpdater> childUpdaters;

    @FactoryMethod
    public ConditionalSidebarLineUpdater(@NotNull @Child("child_updaters") List<ChildUpdater> childUpdaters) {
        this.childUpdaters = List.copyOf(childUpdaters);
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

    @Model("zombies.sidebar.line_updater.conditional.child")
    @Cache(false)
    public static class ChildUpdater {

        private final BooleanSupplier condition;
        private final SidebarLineUpdater updater;

        @FactoryMethod
        public ChildUpdater(@NotNull @Child("condition") BooleanSupplier condition,
            @NotNull @Child("updater") SidebarLineUpdater updater) {
            this.condition = Objects.requireNonNull(condition);
            this.updater = Objects.requireNonNull(updater);
        }

        public @NotNull BooleanSupplier getCondition() {
            return condition;
        }

        public @NotNull SidebarLineUpdater getUpdater() {
            return updater;
        }

        @DataObject
        public record Data(
            @NotNull @ChildPath("condition") String conditionPath,
            @NotNull @ChildPath("updater") String updaterPath) {

            public Data {
                Objects.requireNonNull(conditionPath);
                Objects.requireNonNull(updaterPath);
            }

        }

    }

    @DataObject
    public record Data(@NotNull @ChildPath("child_updaters") List<String> childUpdaterPaths) {

        public Data {
            Objects.requireNonNull(childUpdaterPaths);
        }

    }
}
