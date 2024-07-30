package org.phantazm.zombies.player.upgrade.effect;

import com.github.steanky.element.core.annotation.Cache;
import com.github.steanky.element.core.annotation.Child;
import com.github.steanky.element.core.annotation.FactoryMethod;
import com.github.steanky.element.core.annotation.Model;
import org.jetbrains.annotations.NotNull;
import org.phantazm.commons.InjectionStore;
import org.phantazm.zombies.player.ZombiesPlayer;
import org.phantazm.zombies.player.upgrade.PlayerUpgrade;
import org.phantazm.zombies.player.upgrade.trigger.TriggerData;
import org.phantazm.zombies.player.upgrade.trigger.filter.TriggerFilter;
import org.phantazm.zombies.player.upgrade.trigger.filter.EventFilterComponent;

@Model("zombies.upgrade.effect.conditional")
@Cache
public class ConditionalEffect implements UpgradeEffectComponent {
    private final UpgradeEffectComponent first;
    private final UpgradeEffectComponent second;
    private final EventFilterComponent filter;

    @FactoryMethod
    public ConditionalEffect(@NotNull @Child("first") UpgradeEffectComponent first,
        @NotNull @Child("second") UpgradeEffectComponent second,
        @NotNull @Child("filter") EventFilterComponent filter) {
        this.first = first;
        this.second = second;
        this.filter = filter;
    }

    @Override
    public @NotNull UpgradeEffect apply(@NotNull InjectionStore injectionStore, @NotNull ZombiesPlayer zombiesPlayer) {
        return new Internal(first.apply(injectionStore, zombiesPlayer), second.apply(injectionStore, zombiesPlayer),
            filter.apply(injectionStore, zombiesPlayer));
    }

    private static final class Internal implements UpgradeEffect {
        private final UpgradeEffect first;
        private final UpgradeEffect second;
        private final TriggerFilter filter;

        private final boolean firstNeedsTicking;
        private final boolean secondNeedsTicking;

        private Internal(UpgradeEffect first, UpgradeEffect second, TriggerFilter filter) {
            this.first = first;
            this.second = second;
            this.filter = filter;

            this.firstNeedsTicking = first.needsTicking();
            this.secondNeedsTicking = second.needsTicking();
        }

        @Override
        public void apply(@NotNull PlayerUpgrade upgrade, @NotNull ZombiesPlayer zombiesPlayer,
            @NotNull TriggerData triggerData) {
            if (filter.test(triggerData)) {
                first.apply(upgrade, zombiesPlayer, triggerData);
            } else {
                second.apply(upgrade, zombiesPlayer, triggerData);
            }
        }

        @Override
        public void clear(@NotNull PlayerUpgrade upgrade, @NotNull ZombiesPlayer zombiesPlayer) {
            first.clear(upgrade, zombiesPlayer);
            second.clear(upgrade, zombiesPlayer);
        }

        @Override
        public void tick() {
            if (firstNeedsTicking) {
                first.tick();
            }

            if (secondNeedsTicking) {
                second.tick();
            }
        }

        @Override
        public boolean needsTicking() {
            return firstNeedsTicking || secondNeedsTicking;
        }
    }
}
