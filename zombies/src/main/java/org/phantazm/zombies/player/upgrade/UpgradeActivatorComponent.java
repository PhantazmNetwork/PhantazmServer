package org.phantazm.zombies.player.upgrade;

import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.phantazm.commons.DualComponent;
import org.phantazm.zombies.player.ZombiesPlayer;
import org.phantazm.zombies.scene2.ZombiesScene;

import java.util.Optional;
import java.util.Set;

public interface UpgradeActivatorComponent extends DualComponent<ZombiesScene, UpgradeActivator> {
    @NotNull Optional<Key> nextUpgrade(@NotNull Key group, @Nullable Key key);

    @NotNull Optional<Key> highestUpgrade(@NotNull Key group, @NotNull ZombiesPlayer zombiesPlayer);

    boolean hasRequirements(@NotNull Key upgrade, @NotNull Set<Key> activeUpgrades, boolean isSynergy);
}
