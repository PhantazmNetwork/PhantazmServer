package org.phantazm.core.entity.fakeplayer;

import net.minestom.server.entity.*;
import net.minestom.server.entity.metadata.PlayerMeta;
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

public class MinimalFakePlayer extends LivingEntity {

    private final SchedulerManager schedulerManager;

    private final String username;

    private final PlayerSkin skin;

    protected MinimalFakePlayer(@NotNull SchedulerManager schedulerManager, @NotNull String username,
        @Nullable PlayerSkin skin, boolean register) {
        super(EntityType.PLAYER, UUID.randomUUID(), false);

        this.schedulerManager = Objects.requireNonNull(schedulerManager);
        this.username = Objects.requireNonNull(username);
        this.skin = skin;

        PlayerMeta meta = (PlayerMeta) getEntityMeta();
        meta.setNotifyAboutChanges(false);
        meta.setCapeEnabled(true);
        meta.setJacketEnabled(true);
        meta.setLeftSleeveEnabled(true);
        meta.setRightSleeveEnabled(true);
        meta.setLeftLegEnabled(true);
        meta.setRightLegEnabled(true);
        meta.setHatEnabled(true);
        meta.setNotifyAboutChanges(true);

        if (register) super.register();
    }

    public MinimalFakePlayer(@NotNull SchedulerManager schedulerManager, @NotNull String username,
        @Nullable PlayerSkin skin) {
        this(schedulerManager, username, skin, true);
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
        Objects.requireNonNull(connection);
        schedulerManager.buildTask(() -> connection.sendPacket(getRemovePlayerPacket())).delay(20, TimeUnit.SERVER_TICK)
            .schedule();
    }

    private @NotNull PlayerInfoUpdatePacket getAddPlayerPacket() {
        List<PlayerInfoUpdatePacket.Property> properties;
        if (skin != null) {
            properties = List.of(new PlayerInfoUpdatePacket.Property("textures", skin.textures(), skin.signature()));
        } else {
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
