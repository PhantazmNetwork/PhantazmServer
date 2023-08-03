package org.phantazm.zombies.powerup.action.component;

import com.github.steanky.element.core.annotation.Cache;
import com.github.steanky.element.core.annotation.DataObject;
import com.github.steanky.element.core.annotation.FactoryMethod;
import com.github.steanky.element.core.annotation.Model;
import org.jetbrains.annotations.NotNull;
import org.phantazm.zombies.map.handler.WindowHandler;
import org.phantazm.zombies.player.ZombiesPlayer;
import org.phantazm.zombies.powerup.Powerup;
import org.phantazm.zombies.powerup.action.InstantAction;
import org.phantazm.zombies.powerup.action.PowerupAction;
import org.phantazm.zombies.scene.ZombiesScene;

@Model("zombies.powerup.action.modify_windows")
@Cache(false)
public class ModifyWindowsAction implements PowerupActionComponent {
    private final Data data;

    @FactoryMethod
    public ModifyWindowsAction(@NotNull Data data) {
        this.data = data;
    }

    @Override
    public @NotNull PowerupAction apply(@NotNull ZombiesScene scene) {
        return new Action(data, scene.getMap().windowHandler());
    }

    @DataObject
    public record Data(double radius, boolean shouldBreak) {
    }

    private static class Action extends InstantAction {
        private final Data data;
        private final WindowHandler windowHandler;

        private Action(Data data, WindowHandler windowHandler) {
            this.data = data;
            this.windowHandler = windowHandler;
        }

        @Override
        public boolean activate(@NotNull Powerup powerup, @NotNull ZombiesPlayer player, long time) {
            windowHandler.tracker().forEachInRangeToCenter(powerup.spawnLocation(), data.radius, window -> {
                if (data.shouldBreak) {
                    window.updateIndex(0);
                }
                else {
                    window.updateIndex(window.getVolume());
                }
            });
            return true;
        }
    }
}
