package org.phantazm.zombies.player.upgrade.effect;

import com.github.steanky.element.core.annotation.*;
import com.github.steanky.ethylene.mapper.annotation.Default;
import net.kyori.adventure.sound.Sound;
import net.minestom.server.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.phantazm.commons.InjectionStore;
import org.phantazm.core.Target;
import org.phantazm.zombies.player.ZombiesPlayer;
import org.phantazm.zombies.player.upgrade.PlayerUpgrade;
import org.phantazm.zombies.player.upgrade.selector.Selector;
import org.phantazm.zombies.player.upgrade.selector.SelectorComponent;
import org.phantazm.zombies.player.upgrade.trigger.TriggerData;

import java.util.Collection;

@Model("zombies.upgrade.effect.sound")
@Cache
public class SoundEffect implements UpgradeEffectComponent {
    private final Data data;
    private final SelectorComponent selectorComponent;

    @FactoryMethod
    public SoundEffect(@NotNull Data data, @NotNull @Child("selector") SelectorComponent selectorComponent) {
        this.data = data;
        this.selectorComponent = selectorComponent;
    }

    @Override
    public @NotNull UpgradeEffect apply(@NotNull InjectionStore injectionStore, @NotNull ZombiesPlayer zombiesPlayer) {
        return new Internal(data, selectorComponent.apply(injectionStore, zombiesPlayer));
    }

    private static final class Internal implements UpgradeEffect {
        private final Data data;
        private final Selector selector;

        private Internal(Data data, Selector selector) {
            this.data = data;
            this.selector = selector;
        }

        @Override
        public void apply(@NotNull PlayerUpgrade upgrade, @NotNull ZombiesPlayer zombiesPlayer, @NotNull TriggerData triggerData) {
            Collection<Target.TargetEntry> entries = selector.select(upgrade, zombiesPlayer, triggerData).entries();
            for (Target.TargetEntry entry : entries) {
                if (data.broadcast) {
                    zombiesPlayer.getScene().instance().playSound(data.sound, entry.point());
                } else if (entry.entity() instanceof Player player) {
                    player.playSound(data.sound, player.getPosition());
                }
            }
        }
    }

    @DataObject
    @Default("""
        {
          selector='{type=zombies.upgrade.selector.self}',
          broadcast=false
        }
        """)
    public record Data(@NotNull Sound sound,
        boolean broadcast) {

    }
}
