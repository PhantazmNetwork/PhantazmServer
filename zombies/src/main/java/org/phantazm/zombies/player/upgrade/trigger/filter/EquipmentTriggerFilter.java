package org.phantazm.zombies.player.upgrade.trigger.filter;

import com.github.steanky.element.core.annotation.*;
import org.jetbrains.annotations.NotNull;
import org.phantazm.commons.InjectionStore;
import org.phantazm.zombies.event.trait.EquipmentEvent;
import org.phantazm.zombies.player.ZombiesPlayer;
import org.phantazm.zombies.player.upgrade.PlayerUpgrade;
import org.phantazm.zombies.player.upgrade.trigger.TriggerData;

@Model("zombies.upgrade.filter.equipment")
@Cache
public class EquipmentTriggerFilter implements TriggerFilterComponent {
    private final Data data;

    @FactoryMethod
    public EquipmentTriggerFilter(@NotNull Data data) {
        this.data = data;
    }

    @Override
    public @NotNull TriggerFilter apply(@NotNull InjectionStore injectionStore, @NotNull ZombiesPlayer zombiesPlayer) {
        return new Internal(data);
    }

    private record Internal(Data data) implements TriggerFilter {
        @Override
        public boolean test(@NotNull PlayerUpgrade upgrade, @NotNull ZombiesPlayer zombiesPlayer, @NotNull TriggerData triggerData) {
            if (triggerData.raw() instanceof EquipmentEvent event) {
                return event.equipment().key().asString().equals(data.equipment);
            }

            return false;
        }
    }

    @DataObject
    public record Data(String equipment) {

    }
}
