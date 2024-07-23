package org.phantazm.zombies.player.upgrade.selector;

import com.github.steanky.element.core.annotation.Cache;
import com.github.steanky.element.core.annotation.DataObject;
import com.github.steanky.element.core.annotation.FactoryMethod;
import com.github.steanky.element.core.annotation.Model;
import net.minestom.server.event.trait.EntityEvent;
import org.jetbrains.annotations.NotNull;
import org.phantazm.commons.InjectionStore;
import org.phantazm.core.Target;
import org.phantazm.zombies.event.trait.EntityTargetEvent;
import org.phantazm.zombies.player.ZombiesPlayer;
import org.phantazm.zombies.player.upgrade.PlayerUpgrade;
import org.phantazm.zombies.player.upgrade.trigger.TriggerData;

@Model("zombies.upgrade.selector.event")
@Cache
public class EventSelector implements SelectorComponent {
    private final Data data;

    @FactoryMethod
    public EventSelector(@NotNull Data data) {
        this.data = data;
    }

    @Override
    public @NotNull Selector apply(@NotNull InjectionStore injectionStore, @NotNull ZombiesPlayer zombiesPlayer) {
        return new Internal(data);
    }

    private static final class Internal implements Selector {
        private final Data data;

        private Internal(Data data) {
            this.data = data;
        }

        @Override
        public @NotNull Target select(@NotNull PlayerUpgrade upgrade, @NotNull ZombiesPlayer zombiesPlayer,
            @NotNull TriggerData triggerData) {
            if (data.useTarget && triggerData.raw() instanceof EntityTargetEvent event) {
                return Target.entities(event.target());
            }

            if (triggerData.raw() instanceof EntityEvent entityEvent) {
                return Target.entities(entityEvent.getEntity());
            }

            return Target.NONE;
        }
    }

    @DataObject
    public record Data(boolean useTarget) {
    }
}
