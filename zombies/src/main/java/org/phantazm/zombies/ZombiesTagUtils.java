package org.phantazm.zombies;

import net.minestom.server.entity.Entity;
import net.minestom.server.entity.Player;
import net.minestom.server.tag.TagHandler;
import org.jetbrains.annotations.NotNull;
import org.phantazm.zombies.player.ZombiesPlayer;
import org.phantazm.zombies.scene2.ZombiesScene;

public final class ZombiesTagUtils {
    public static @NotNull TagHandler sceneLocalTags(@NotNull ZombiesPlayer zombiesPlayer) {
        return zombiesPlayer.getScene().playerTags(zombiesPlayer.getUUID());
    }

    public static @NotNull TagHandler sceneLocalTags(@NotNull ZombiesScene scene, @NotNull Entity entity) {
        if (entity instanceof Player player) {
            return scene.playerTags(player.getUuid());
        }

        return entity.tagHandler();
    }
}
