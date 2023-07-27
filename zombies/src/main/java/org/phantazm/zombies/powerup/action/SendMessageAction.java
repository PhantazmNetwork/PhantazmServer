package org.phantazm.zombies.powerup.action;

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

import java.util.Objects;
import java.util.function.Supplier;

@Model("zombies.powerup.action.send_message")
@Cache(false)
public class SendMessageAction implements Supplier<PowerupAction> {
    private final Data data;
    private final Instance instance;

    @FactoryMethod
    public SendMessageAction(@NotNull Data data, @NotNull Instance instance) {
        this.data = Objects.requireNonNull(data, "data");
        this.instance = Objects.requireNonNull(instance, "instance");
    }

    @Override
    public PowerupAction get() {
        return new Action(data, instance);
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
        public void activate(@NotNull Powerup powerup, @NotNull ZombiesPlayer player, long time) {
            Component component = getComponent(player);
            if (data.broadcast) {
                instance.sendMessage(component);
            }
            else {
                player.getPlayer().ifPresent(p -> p.sendMessage(component));
            }
        }

        private Component getComponent(ZombiesPlayer player) {
            Component playerName = player.module().getPlayerView().getDisplayNameIfPresent();
            TagResolver playerPlaceholder = Placeholder.component("player", playerName);

            return MiniMessage.miniMessage().deserialize(data.format, playerPlaceholder);
        }
    }
}
