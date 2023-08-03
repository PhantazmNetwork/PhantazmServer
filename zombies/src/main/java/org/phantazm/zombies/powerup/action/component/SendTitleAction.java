package org.phantazm.zombies.powerup.action.component;

import com.github.steanky.element.core.annotation.Cache;
import com.github.steanky.element.core.annotation.DataObject;
import com.github.steanky.element.core.annotation.FactoryMethod;
import com.github.steanky.element.core.annotation.Model;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.TitlePart;
import net.minestom.server.instance.Instance;
import org.jetbrains.annotations.NotNull;
import org.phantazm.zombies.player.ZombiesPlayer;
import org.phantazm.zombies.powerup.Powerup;
import org.phantazm.zombies.powerup.action.InstantAction;
import org.phantazm.zombies.powerup.action.PowerupAction;
import org.phantazm.zombies.scene.ZombiesScene;

@Model("zombies.powerup.action.send_title")
@Cache(false)
public class SendTitleAction implements PowerupActionComponent {
    private final Data data;

    @FactoryMethod
    public SendTitleAction(@NotNull Data data) {
        this.data = data;
    }

    @Override
    public @NotNull PowerupAction apply(@NotNull ZombiesScene scene) {
        return new Action(data, scene.instance());
    }

    @DataObject
    public record Data(@NotNull Component message, @NotNull TitlePart<Component> titlePart, boolean broadcast) {
    }

    private static class Action extends InstantAction {
        private final Data data;
        private final Instance instance;

        private Action(Data data, Instance instance) {
            this.data = data;
            this.instance = instance;
        }

        @Override
        public boolean activate(@NotNull Powerup powerup, @NotNull ZombiesPlayer player, long time) {
            if (data.broadcast) {
                instance.sendTitlePart(data.titlePart, data.message);
            }
            else {
                player.getPlayer().ifPresent(p -> p.sendTitlePart(data.titlePart, data.message));
            }
            return true;
        }
    }
}
