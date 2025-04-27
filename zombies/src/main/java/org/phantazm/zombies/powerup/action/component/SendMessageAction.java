package org.phantazm.zombies.powerup.action.component;

import com.github.steanky.element.core.annotation.*;
import com.github.steanky.ethylene.mapper.annotation.Default;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.minestom.server.instance.Instance;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.phantazm.core.ElementUtils;
import org.phantazm.core.time.TickFormatter;
import org.phantazm.zombies.player.ZombiesPlayer;
import org.phantazm.zombies.powerup.Powerup;
import org.phantazm.zombies.powerup.action.InstantAction;
import org.phantazm.zombies.powerup.action.PowerupAction;
import org.phantazm.zombies.powerup.predicate.DeactivationPredicate;
import org.phantazm.zombies.scene2.ZombiesScene;

import java.util.OptionalInt;

@Model("zombies.powerup.action.send_message")
@Cache(false)
public class SendMessageAction implements PowerupActionComponent {
    private final Data data;
    private final TickFormatter tickFormatter;

    @FactoryMethod
    public SendMessageAction(@NotNull Data data, @NotNull @Child("tickFormatter") TickFormatter tickFormatter) {
        this.data = data;
        this.tickFormatter = tickFormatter;
    }

    @Override
    public @NotNull PowerupAction apply(@NotNull ZombiesScene scene) {
        return new Action(data, scene.instance(), tickFormatter);
    }

    @Default("""
        {
          tickFormatter={type='core.tick_formatter.precision_second',decimalPlaces=1},
          timerSource=null
        }
        """)
    @DataObject
    public record Data(@NotNull String format,
        @Nullable String timerSource,
        boolean broadcast) {
    }

    private static class Action extends InstantAction {
        private final Data data;
        private final TickFormatter tickFormatter;
        private final Instance instance;

        private Action(Data data, Instance instance, TickFormatter tickFormatter) {
            this.data = data;
            this.tickFormatter = tickFormatter;
            this.instance = instance;
        }

        @Override
        public void activate(@NotNull Powerup powerup, @NotNull ZombiesPlayer player, long time) {
            Component component = getComponent(player, powerup);
            if (data.broadcast) {
                instance.sendMessage(component);
            } else {
                player.getPlayer().ifPresent(p -> p.sendMessage(component));
            }
        }

        private Component getComponent(ZombiesPlayer player, Powerup powerup) {
            Component playerName = player.module().getPlayerView().getDisplayNameIfCached().orElse(Component.empty());
            TagResolver playerPlaceholder = Placeholder.component("player", playerName);
            TagResolver timePlaceholder = null;

            if (data.timerSource != null) {
                for (PowerupAction action : powerup.activeActions()) {
                    if (ElementUtils.findModel(action.getClass()).filter(name -> name.equals(data.timerSource)).isEmpty())
                        continue;

                    DeactivationPredicate deactivationPredicate = action.deactivationPredicate();
                    OptionalInt durationOptional = deactivationPredicate.duration(powerup, player);

                    if (durationOptional.isPresent()) {
                        timePlaceholder = Placeholder.unparsed("time", tickFormatter.format(durationOptional.getAsInt()));
                    }
                }
            }

            if (timePlaceholder == null) timePlaceholder = Placeholder.unparsed("time", "");
            return MiniMessage.miniMessage().deserialize(data.format, playerPlaceholder, timePlaceholder);
        }
    }
}
