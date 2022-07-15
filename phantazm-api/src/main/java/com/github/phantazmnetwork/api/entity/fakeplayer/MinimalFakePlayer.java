package com.github.phantazmnetwork.api.entity.fakeplayer;

import net.minestom.server.entity.*;
import net.minestom.server.network.packet.server.play.PlayerInfoPacket;
import net.minestom.server.network.player.PlayerConnection;
import net.minestom.server.timer.SchedulerManager;
import net.minestom.server.utils.PacketUtils;
import net.minestom.server.utils.time.TimeUnit;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class MinimalFakePlayer extends Entity {

    private final SchedulerManager schedulerManager;

    private final String username;

    private final PlayerSkin skin;

    public MinimalFakePlayer(@NotNull SchedulerManager schedulerManager, @NotNull String username,
                             @NotNull PlayerSkin skin) {
        super(EntityType.PLAYER, UUID.randomUUID());

        this.schedulerManager = Objects.requireNonNull(schedulerManager, "schedulerManager");
        this.username = Objects.requireNonNull(username, "username");
        this.skin = Objects.requireNonNull(skin, "skin");
    }

    public void init() {
        PacketUtils.broadcastPacket(getAddPlayerPacket());
    }

    public @NotNull String getUsername() {
        return username;
    }

    @SuppressWarnings("UnstableApiUsage")
    @Override
    public void updateNewViewer(@NotNull Player player) {
        player.sendPacket(getAddPlayerPacket());
        removeFromTabList(player.getPlayerConnection());
        super.updateNewViewer(player);
    }

    @Override
    public void remove() {
        super.remove();
        PacketUtils.broadcastPacket(getRemovePlayerPacket());
    }

    @SuppressWarnings("UnstableApiUsage")
    private void removeFromTabList(@NotNull PlayerConnection connection) {
        Objects.requireNonNull(connection, "connection");
        schedulerManager.buildTask(() -> connection.sendPacket(getRemovePlayerPacket())).delay(20, TimeUnit.SERVER_TICK)
                        .schedule();
    }

    private @NotNull PlayerInfoPacket getAddPlayerPacket() {
        List<PlayerInfoPacket.AddPlayer.Property> properties;
        if (skin != null) {
            PlayerInfoPacket.AddPlayer.Property skinProperty =
                    new PlayerInfoPacket.AddPlayer.Property("textures", skin.textures(), skin.signature());
            properties = Collections.singletonList(skinProperty);
        }
        else {
            properties = Collections.emptyList();
        }

        PlayerInfoPacket.Entry entry =
                new PlayerInfoPacket.AddPlayer(getUuid(), username, properties, GameMode.SURVIVAL, 0, null);
        return new PlayerInfoPacket(PlayerInfoPacket.Action.ADD_PLAYER, entry);
    }

    private @NotNull PlayerInfoPacket getRemovePlayerPacket() {
        return new PlayerInfoPacket(PlayerInfoPacket.Action.REMOVE_PLAYER,
                                    new PlayerInfoPacket.RemovePlayer(getUuid())
        );
    }

}
