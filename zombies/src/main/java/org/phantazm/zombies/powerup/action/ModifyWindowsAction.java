package org.phantazm.zombies.powerup.action;

import com.github.steanky.element.core.annotation.DataObject;
import com.github.steanky.element.core.annotation.FactoryMethod;
import com.github.steanky.element.core.annotation.Model;
import org.jetbrains.annotations.NotNull;
import org.phantazm.zombies.map.Window;
import org.phantazm.zombies.map.handler.WindowHandler;
import org.phantazm.zombies.player.ZombiesPlayer;
import org.phantazm.zombies.powerup.Powerup;

import java.util.Objects;
import java.util.function.Supplier;

@Model("zombies.powerup.action.modify_windows")
public class ModifyWindowsAction implements Supplier<PowerupAction> {
    private final Data data;
    private final Supplier<? extends WindowHandler> windowHandler;

    @FactoryMethod
    public ModifyWindowsAction(@NotNull Data data, @NotNull Supplier<? extends WindowHandler> windowHandler) {
        this.data = Objects.requireNonNull(data, "data");
        this.windowHandler = Objects.requireNonNull(windowHandler, "windowHandler");
    }

    @Override
    public PowerupAction get() {
        return new Action(data, windowHandler);
    }

    @DataObject
    public record Data(double radius, boolean shouldBreak) {
    }

    private static class Action extends InstantAction {
        private final Data data;
        private final Supplier<? extends WindowHandler> windowHandler;

        private Action(Data data, Supplier<? extends WindowHandler> windowHandler) {
            this.data = data;
            this.windowHandler = windowHandler;
        }

        @Override
        public void activate(@NotNull Powerup powerup, @NotNull ZombiesPlayer player, long time) {
            for (Window window : windowHandler.get().windows()) {
                if (powerup.spawnLocation().distanceSquared(window.getCenter()) > data.radius * data.radius) {
                    continue;
                }

                if (data.shouldBreak) {
                    window.updateIndex(0);
                }
                else {
                    window.updateIndex(window.getVolume());
                }
            }
        }
    }
}
