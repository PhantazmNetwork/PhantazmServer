package org.phantazm.zombies.player.upgrade.effect;

import com.github.steanky.element.core.annotation.*;
import org.jetbrains.annotations.NotNull;
import org.phantazm.commons.InjectionStore;
import org.phantazm.core.Cooldown;
import org.phantazm.zombies.player.ZombiesPlayer;
import org.phantazm.zombies.player.upgrade.PlayerUpgrade;

@Model("zombies.upgrade.effect.cooldown")
@Cache
public class CooldownEffectComponent implements UpgradeEffectComponent {
    private final Data data;
    private final UpgradeEffectComponent delegate;

    @FactoryMethod
    public CooldownEffectComponent(@NotNull Data data, @Child("delegate") UpgradeEffectComponent delegate) {
        this.data = data;
        this.delegate = delegate;
    }

    @Override
    public @NotNull UpgradeEffect apply(@NotNull InjectionStore injectionStore, @NotNull ZombiesPlayer zombiesPlayer) {
        return new Internal(data, delegate.apply(injectionStore, zombiesPlayer));
    }

    private static final class Internal implements UpgradeEffect {
        private final Data data;
        private final UpgradeEffect effect;
        private final Cooldown cooldown;

        private Internal(Data data, UpgradeEffect effect) {
            this.data = data;
            this.effect = effect;
            this.cooldown = Cooldown.cooldown();
        }

        @Override
        public void apply(@NotNull PlayerUpgrade upgrade, @NotNull ZombiesPlayer zombiesPlayer) {
            if (cooldown.takeCooldown(data.cooldown)) {
                effect.apply(upgrade, zombiesPlayer);
            }
        }

        @Override
        public boolean needsTicking() {
            return true;
        }

        @Override
        public void tick() {
            cooldown.step();
        }
    }

    @DataObject
    public record Data(int cooldown) {
    }
}
