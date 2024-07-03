package org.phantazm.zombies.player.upgrade;

import org.jetbrains.annotations.NotNull;
import org.phantazm.commons.InjectionStore;
import org.phantazm.zombies.scene2.ZombiesScene;

public class NoUpgradeActivator implements UpgradeActivatorComponent {
    public static NoUpgradeActivator INSTANCE = new NoUpgradeActivator();

    private NoUpgradeActivator() {

    }

    private static final UpgradeActivator NIL = () -> {

    };

    @Override
    public @NotNull UpgradeActivator apply(@NotNull InjectionStore injectionStore, @NotNull ZombiesScene zombiesScene) {
        return NIL;
    }
}
