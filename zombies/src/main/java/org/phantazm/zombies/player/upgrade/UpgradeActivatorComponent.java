package org.phantazm.zombies.player.upgrade;

import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.NotNull;
import org.phantazm.commons.DualComponent;
import org.phantazm.zombies.scene2.ZombiesScene;

import java.util.Optional;

public interface UpgradeActivatorComponent extends DualComponent<ZombiesScene, UpgradeActivator> {
    @NotNull Optional<Key> nextUpgrade(@NotNull Key group, @NotNull Key key);
}
