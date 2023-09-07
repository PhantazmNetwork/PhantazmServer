package org.phantazm.zombies.equipment.gun2.reload;

import net.minestom.server.event.EventDispatcher;
import org.jetbrains.annotations.NotNull;
import org.phantazm.zombies.equipment.gun2.GunState;
import org.phantazm.zombies.equipment.gun2.event.GunReloadEvent;

import java.util.Objects;

public class BasicGunReload implements GunReload {

    private final ReloadTester reloadTester;

    private final GunState state;

    public BasicGunReload(@NotNull ReloadTester reloadTester, @NotNull GunState state) {
        this.reloadTester = Objects.requireNonNull(reloadTester);
        this.state = Objects.requireNonNull(state);
    }

    @Override
    public void reload() {
        if (!reloadTester.shouldReload()) {
            return;
        }

        state.setTicksSinceLastReload(0);
        EventDispatcher.call(new GunReloadEvent());
    }
}
