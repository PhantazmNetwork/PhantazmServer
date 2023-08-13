package org.phantazm.server;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.minestom.server.MinecraftServer;
import net.minestom.server.event.player.PlayerPluginMessageEvent;
import net.minestom.server.event.player.PlayerSpawnEvent;
import org.jetbrains.annotations.NotNull;
import org.phantazm.server.config.player.PlayerConfig;

public final class PlayerFeature {

    private PlayerFeature() {
        throw new UnsupportedOperationException();
    }

    static void initialize(@NotNull PlayerConfig playerConfig) {
        MinecraftServer.getGlobalEventHandler().addListener(PlayerSpawnEvent.class, event -> {
            if (!event.isFirstSpawn()) {
                return;
            }

            TagResolver namePlaceholder = Placeholder.component("name", event.getPlayer().getName());
            Component newName = MiniMessage.miniMessage().deserialize(playerConfig.nameFormat(), namePlaceholder);
            event.getPlayer().setCustomName(newName);
            event.getPlayer().setCustomNameVisible(true);

            event.getPlayer().sendMessage(playerConfig.joinMessage());
        });
        MinecraftServer.getGlobalEventHandler().addListener(PlayerPluginMessageEvent.class, event -> {
            if (event.getIdentifier().equals("minecraft:brand") && event.getMessageString().contains("optifine")) {
                event.getPlayer().sendMessage(playerConfig.optifineMessage());
            }
        });
    }

}
