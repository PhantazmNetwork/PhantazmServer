package org.phantazm.zombies.player.upgrade;

import com.github.steanky.element.core.annotation.Cache;
import com.github.steanky.element.core.annotation.FactoryMethod;
import com.github.steanky.element.core.annotation.Model;
import org.jetbrains.annotations.NotNull;
import org.phantazm.commons.InjectionStore;
import org.phantazm.zombies.scene2.ZombiesScene;

@Model("zombies.upgrade.activator.none")
@Cache
public class NoUpgradeActivator implements UpgradeActivatorComponent {
    public static NoUpgradeActivator INSTANCE = new NoUpgradeActivator();

    @FactoryMethod
    public NoUpgradeActivator() {

    }

    private static final UpgradeActivator NIL = () -> {

    };

    @Override
    public @NotNull UpgradeActivator apply(@NotNull InjectionStore injectionStore, @NotNull ZombiesScene zombiesScene) {
        return NIL;
    }
}
