package org.phantazm.core.entity.fakeplayer;

import net.minestom.server.entity.*;
import net.minestom.server.network.packet.server.play.PlayerInfoRemovePacket;
import net.minestom.server.network.packet.server.play.PlayerInfoUpdatePacket;
import net.minestom.server.network.player.PlayerConnection;
import net.minestom.server.timer.SchedulerManager;
import net.minestom.server.utils.PacketUtils;
import net.minestom.server.utils.time.TimeUnit;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class MinimalFakePlayer extends Entity {

    private final SchedulerManager schedulerManager;

    private final String username;

    private final PlayerSkin skin;

    public MinimalFakePlayer(@NotNull SchedulerManager schedulerManager, @NotNull String username,
            @Nullable PlayerSkin skin) {
        super(EntityType.PLAYER, UUID.randomUUID());

        this.schedulerManager = Objects.requireNonNull(schedulerManager, "schedulerManager");
        this.username = Objects.requireNonNull(username, "username");
        this.skin = skin;
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

    private @NotNull PlayerInfoUpdatePacket getAddPlayerPacket() {
        List<PlayerInfoUpdatePacket.Property> properties;
        if (skin != null) {
            properties = List.of(new PlayerInfoUpdatePacket.Property("textures", skin.textures(), skin.signature()));
        }
        else {
            properties = List.of();
        }

        return new PlayerInfoUpdatePacket(PlayerInfoUpdatePacket.Action.ADD_PLAYER,
                new PlayerInfoUpdatePacket.Entry(getUuid(), username, properties, false, 0, GameMode.SURVIVAL, null,
                        null));
    }

    private @NotNull PlayerInfoRemovePacket getRemovePlayerPacket() {
        return new PlayerInfoRemovePacket(getUuid());
    }
}
