package org.phantazm.zombies;

import net.minestom.server.entity.Entity;
import net.minestom.server.entity.Player;
import net.minestom.server.tag.Tag;
import net.minestom.server.tag.TagHandler;
import org.jetbrains.annotations.NotNull;
import org.phantazm.zombies.player.ZombiesPlayer;
import org.phantazm.zombies.scene2.ZombiesScene;

import java.lang.ref.Reference;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

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

    public static @NotNull Optional<TagHandler> sceneLocalTags(@NotNull ZombiesScene scene, @NotNull UUID uuid) {
        if (scene.hasPlayer(uuid)) {
            return Optional.of(scene.playerTags(uuid));
        }

        return Optional.empty();
    }

    public static void cleanTagTrackingMap(Map<UUID, Reference<Entity>> map, Tag<?> tag, ZombiesPlayer zombiesPlayer) {
        map.entrySet().removeIf(entry -> {
            UUID uuid = entry.getKey();
            Entity entity = entry.getValue().get();

            TagHandler handler;
            if (entity == null) {
                // try to get the handler from the scene local storage
                Optional<TagHandler> handlerOptional = ZombiesTagUtils.sceneLocalTags(zombiesPlayer.getScene(), uuid);

                if (handlerOptional.isPresent()) {
                    handler = handlerOptional.get();
                } else {
                    return true;
                }
            } else {
                // non-players that die should get removed from the tracking map
                if (!(entity instanceof Player)) {
                    if (entity.isRemoved()) {
                        return true;
                    }
                }

                // we can get the handler from the entity directly
                handler = ZombiesTagUtils.sceneLocalTags(zombiesPlayer.getScene(), entity);
            }

            handler.removeTag(tag);
            return true;
        });
    }
}
