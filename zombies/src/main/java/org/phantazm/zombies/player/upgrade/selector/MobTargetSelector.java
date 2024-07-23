package org.phantazm.zombies.player.upgrade.selector;

import com.github.steanky.element.core.annotation.Cache;
import com.github.steanky.element.core.annotation.FactoryMethod;
import com.github.steanky.element.core.annotation.Model;
import net.minestom.server.event.trait.EntityEvent;
import org.jetbrains.annotations.NotNull;
import org.phantazm.commons.InjectionStore;
import org.phantazm.core.Target;
import org.phantazm.zombies.event.trait.MobTargetEvent;
import org.phantazm.zombies.player.ZombiesPlayer;
import org.phantazm.zombies.player.upgrade.PlayerUpgrade;
import org.phantazm.zombies.player.upgrade.trigger.TriggerData;

@Model("zombies.upgrade.selector.mob_target")
@Cache
public class MobTargetSelector implements SelectorComponent {
    private static final Selector INSTANCE = new Internal();

    @FactoryMethod
    public MobTargetSelector() {
    }

    @Override
    public @NotNull Selector apply(@NotNull InjectionStore injectionStore, @NotNull ZombiesPlayer zombiesPlayer) {
        return INSTANCE;
    }

    private static final class Internal implements Selector {
        private Internal() {
        }

        @Override
        public @NotNull Target select(@NotNull PlayerUpgrade upgrade, @NotNull ZombiesPlayer zombiesPlayer,
            @NotNull TriggerData triggerData) {
            if (triggerData.raw() instanceof MobTargetEvent mobTargetEvent) {
                return Target.entities(mobTargetEvent.target());
            }

            if (triggerData.raw() instanceof EntityEvent entityEvent) {
                return Target.entities(entityEvent.getEntity());
            }

            return Target.NONE;
        }
    }
}
