package org.phantazm.zombies.player.upgrade.effect;

import com.github.steanky.element.core.annotation.Cache;
import com.github.steanky.element.core.annotation.FactoryMethod;
import com.github.steanky.element.core.annotation.Model;
import net.minestom.server.event.trait.CancellableEvent;
import org.jetbrains.annotations.NotNull;
import org.phantazm.commons.InjectionStore;
import org.phantazm.zombies.player.ZombiesPlayer;
import org.phantazm.zombies.player.upgrade.PlayerUpgrade;
import org.phantazm.zombies.player.upgrade.trigger.TriggerData;

@Model("zombies.upgrade.effect.cancel")
@Cache
public class CancelEffect implements UpgradeEffectComponent {
    private static final UpgradeEffect INSTANCE = new Internal();

    @FactoryMethod
    public CancelEffect() {

    }

    @Override
    public @NotNull UpgradeEffect apply(@NotNull InjectionStore injectionStore, @NotNull ZombiesPlayer zombiesPlayer) {
        return INSTANCE;
    }

    private static final class Internal extends SingleEventEffect<CancellableEvent> {
        private Internal() {
            super(CancellableEvent.class);
        }

        @Override
        protected void applyEvent(@NotNull PlayerUpgrade upgrade, @NotNull ZombiesPlayer zombiesPlayer,
            @NotNull TriggerData triggerData, @NotNull CancellableEvent event) {
            event.setCancelled(true);
        }
    }
}
