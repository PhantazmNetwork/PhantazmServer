package org.phantazm.zombies.powerup.action.component;

import com.github.steanky.element.core.annotation.Cache;
import com.github.steanky.element.core.annotation.DataObject;
import com.github.steanky.element.core.annotation.FactoryMethod;
import com.github.steanky.element.core.annotation.Model;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.minestom.server.instance.Instance;
import org.jetbrains.annotations.NotNull;
import org.phantazm.zombies.player.ZombiesPlayer;
import org.phantazm.zombies.powerup.Powerup;
import org.phantazm.zombies.powerup.action.InstantAction;
import org.phantazm.zombies.powerup.action.PowerupAction;
import org.phantazm.zombies.scene.ZombiesScene;

@Model("zombies.powerup.action.send_message")
@Cache(false)
public class SendMessageAction implements PowerupActionComponent {
    private final Data data;

    @FactoryMethod
    public SendMessageAction(@NotNull Data data) {
        this.data = data;
    }

    @Override
    public @NotNull PowerupAction apply(@NotNull ZombiesScene scene) {
        return new Action(data, scene.instance());
    }

    @DataObject
    public record Data(@NotNull String format, boolean broadcast) {
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
            Component component = getComponent(player);
            if (data.broadcast) {
                instance.sendMessage(component);
            }
            else {
                player.getPlayer().ifPresent(p -> p.sendMessage(component));
            }
            return true;
        }

        private Component getComponent(ZombiesPlayer player) {
            Component playerName = player.module().getPlayerView().getDisplayNameIfPresent();
            TagResolver playerPlaceholder = Placeholder.component("player", playerName);

            return MiniMessage.miniMessage().deserialize(data.format, playerPlaceholder);
        }
    }
}
