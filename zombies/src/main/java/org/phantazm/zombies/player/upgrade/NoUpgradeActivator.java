package org.phantazm.zombies.player.upgrade;

import com.github.steanky.element.core.annotation.Cache;
import com.github.steanky.element.core.annotation.FactoryMethod;
import com.github.steanky.element.core.annotation.Model;
import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.phantazm.commons.InjectionStore;
import org.phantazm.zombies.player.ZombiesPlayer;
import org.phantazm.zombies.scene2.ZombiesScene;

import java.util.Optional;
import java.util.Set;

@Model("zombies.upgrade.activator.none")
@Cache
public class NoUpgradeActivator implements UpgradeActivatorComponent {
    public static NoUpgradeActivator INSTANCE = new NoUpgradeActivator();

    @FactoryMethod
    public NoUpgradeActivator() {

    }

    private static final UpgradeActivator NIL = new UpgradeActivator() {
        @Override
        public void hook() {

        }

        @Override
        public void refresh(@NotNull ZombiesPlayer zombiesPlayer) {

        }
    };

    @Override
    public @NotNull UpgradeActivator apply(@NotNull InjectionStore injectionStore, @NotNull ZombiesScene zombiesScene) {
        return NIL;
    }

    @Override
    public @NotNull Optional<Key> nextUpgrade(@NotNull Key group, @Nullable Key key) {
        return Optional.empty();
    }

    @Override
    public @NotNull Optional<Key> highestUpgrade(@NotNull Key group, @NotNull ZombiesPlayer zombiesPlayer) {
        return Optional.empty();
    }

    @Override
    public boolean hasRequirements(@NotNull Key upgrade, @NotNull Set<Key> activeUpgrades, boolean isSynergy) {
        return true;
    }
}
