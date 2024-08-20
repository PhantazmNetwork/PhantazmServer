package org.phantazm.zombies.player.upgrade.trigger.filter;

import com.github.steanky.element.core.annotation.Cache;
import com.github.steanky.element.core.annotation.Child;
import com.github.steanky.element.core.annotation.FactoryMethod;
import com.github.steanky.element.core.annotation.Model;
import org.jetbrains.annotations.NotNull;
import org.phantazm.commons.InjectionStore;
import org.phantazm.zombies.player.ZombiesPlayer;
import org.phantazm.zombies.player.upgrade.PlayerUpgrade;
import org.phantazm.zombies.player.upgrade.selector.Selector;
import org.phantazm.zombies.player.upgrade.selector.SelectorComponent;
import org.phantazm.zombies.player.upgrade.trigger.TriggerData;

@Model("zombies.upgrade.filter.selector")
@Cache
public class SelectorTriggerFilter implements TriggerFilterComponent {
    private final SelectorComponent selectorComponent;

    @FactoryMethod
    public SelectorTriggerFilter(@NotNull @Child("selector") SelectorComponent selectorComponent) {
        this.selectorComponent = selectorComponent;
    }

    @Override
    public @NotNull TriggerFilter apply(@NotNull InjectionStore injectionStore, @NotNull ZombiesPlayer zombiesPlayer) {
        return new Internal(selectorComponent.apply(injectionStore, zombiesPlayer));
    }

    private record Internal(Selector selector) implements TriggerFilter {
        @Override
        public boolean test(@NotNull PlayerUpgrade upgrade, @NotNull ZombiesPlayer zombiesPlayer, @NotNull TriggerData triggerData) {
            return !selector.select(upgrade, zombiesPlayer, triggerData).targets().isEmpty();
        }
    }
}
