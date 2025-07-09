package org.phantazm.zombies.player.upgrade.effect;

import com.github.steanky.element.core.annotation.*;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.minestom.server.adventure.audience.PacketGroupingAudience;
import net.minestom.server.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.phantazm.commons.InjectionStore;
import org.phantazm.zombies.player.ZombiesPlayer;
import org.phantazm.zombies.player.upgrade.PlayerUpgrade;
import org.phantazm.zombies.player.upgrade.selector.Selector;
import org.phantazm.zombies.player.upgrade.selector.SelectorComponent;
import org.phantazm.zombies.player.upgrade.trigger.TriggerData;

import java.util.Collection;

@Model("zombies.upgrade.effect.send_message")
@Cache
public class SendMessageEffect implements UpgradeEffectComponent {
    private final SelectorComponent selector;
    private final Component component;

    @FactoryMethod
    public SendMessageEffect(@NotNull Data data, @NotNull @Child("selector") SelectorComponent selector) {
        this.selector = selector;
        this.component = MiniMessage.miniMessage().deserialize(data.message);
    }

    @Override
    public @NotNull UpgradeEffect apply(@NotNull InjectionStore injectionStore, @NotNull ZombiesPlayer zombiesPlayer) {
        return new Internal(selector.apply(injectionStore, zombiesPlayer), component);
    }

    private record Internal(Selector selector,
        Component component) implements UpgradeEffect {

        @Override
        public void apply(@NotNull PlayerUpgrade upgrade, @NotNull ZombiesPlayer zombiesPlayer,
            @NotNull TriggerData triggerData) {
            Collection<Player> players = selector.select(upgrade, zombiesPlayer, triggerData).targets(Player.class);
            if (players.isEmpty()) return;

            PacketGroupingAudience.of(players).sendMessage(component);
        }
    }

    @DataObject
    public record Data(@NotNull String message) {
    }
}
