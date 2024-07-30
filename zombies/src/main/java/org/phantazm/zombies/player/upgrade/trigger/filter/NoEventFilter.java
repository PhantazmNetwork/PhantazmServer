package org.phantazm.zombies.player.upgrade.trigger.filter;

import com.github.steanky.element.core.annotation.Cache;
import com.github.steanky.element.core.annotation.FactoryMethod;
import com.github.steanky.element.core.annotation.Model;
import org.jetbrains.annotations.NotNull;
import org.phantazm.commons.InjectionStore;
import org.phantazm.zombies.player.ZombiesPlayer;

@Model("zombies.upgrade.filter.none")
@Cache
public class NoEventFilter implements EventFilterComponent {
    @FactoryMethod
    public NoEventFilter() {

    }

    @Override
    public @NotNull TriggerFilter apply(@NotNull InjectionStore injectionStore, @NotNull ZombiesPlayer zombiesPlayer) {
        return TriggerFilter.ALL;
    }
}
