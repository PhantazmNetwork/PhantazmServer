package org.phantazm.core.scene2;

import net.minestom.server.instance.Instance;
import org.jetbrains.annotations.NotNull;
import org.phantazm.core.game.scene.Utils;
import org.phantazm.core.player.PlayerView;

public interface TablistSettingJoin<T extends InstanceScene> extends Join<T> {
    @Override
    default void join(@NotNull T scene) {
        Instance newInstance = scene.instance();
        for (PlayerView playerView : players()) {
            playerView.getPlayer().ifPresent(player -> {
                Utils.handleInstanceTransfer(player.getInstance(), newInstance, player, newInstancePlayer -> true);
            });
        }
    }
}
