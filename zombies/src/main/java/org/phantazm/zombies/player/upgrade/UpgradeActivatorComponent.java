package org.phantazm.zombies.player.upgrade;

import net.kyori.adventure.key.Key;
import net.minestom.server.tag.Tag;
import net.minestom.server.tag.TagHandler;
import org.jetbrains.annotations.NotNull;
import org.phantazm.commons.DualComponent;
import org.phantazm.zombies.scene2.ZombiesScene;

public interface UpgradeActivatorComponent extends DualComponent<ZombiesScene, UpgradeActivator> {
    boolean mayPurchase(@NotNull Key upgrade, @NotNull TagHandler handler, boolean isSynergy);

    @NotNull Tag<Boolean> purchaseTag(@NotNull Key upgrade);
}
