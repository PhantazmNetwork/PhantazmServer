package org.phantazm.core.hologram;

import net.kyori.adventure.text.Component;
import net.minestom.server.coordinate.Point;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.Metadata;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.metadata.other.ArmorStandMeta;
import net.minestom.server.network.packet.server.LazyPacket;
import net.minestom.server.network.packet.server.SendablePacket;
import net.minestom.server.network.packet.server.play.EntityHeadLookPacket;
import net.minestom.server.network.packet.server.play.EntityMetaDataPacket;
import org.jetbrains.annotations.NotNull;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.Predicate;

/**
 * A subclass of {@link InstanceHologram} which additionally may be set up to only render for players who satisfy a
 * certain predicate. A {@link LineFormatter} may be provided to (optionally asynchronously) format lines passed to
 * {@link Hologram#add(Object)} and derivatives.
 */
public class ViewableHologram extends InstanceHologram {
    private final Predicate<? super Player> canRender;

    private static class FormattingEntity extends Entity {
        private final LineFormatter formatter;
        private final String formatString;

        private FormattingEntity(LineFormatter formatter, String formatString) {
            super(EntityType.ARMOR_STAND);
            this.formatter = formatter;
            this.formatString = formatString;
        }

        private void sendPacketWhenComplete(Player target, CompletableFuture<SendablePacket> future) {
            Reference<Player> playerReference = new WeakReference<>(target);
            future.thenAccept(asyncPacket -> {
                if (asyncPacket == null) {
                    return;
                }

                Player actualPlayer = playerReference.get();
                if (actualPlayer == null) {
                    return;
                }

                if (!this.isViewer(actualPlayer)) {
                    return;
                }

                actualPlayer.sendPacket(asyncPacket);
            });
        }

        private boolean sendAsyncOrInitial(Player player, CompletableFuture<SendablePacket> asyncMetadata) {
            boolean sentAsyncPacket;
            SendablePacket packet;
            if (asyncMetadata.isDone() && (packet = asyncMetadata.join()) != null) {
                sentAsyncPacket = true;
            } else {
                //used as an initial value for the metadata while we wait for the async metadata
                packet = initialMetadataPacket(this, formatter.initialValue());
                sentAsyncPacket = false;
            }

            player.sendPacket(packet);
            return sentAsyncPacket;
        }

        private void reformat(Player player) {
            CompletableFuture<SendablePacket> asyncMetadata = asyncMetadataPacket(this, formatter,
                player, formatString);

            if (!sendAsyncOrInitial(player, asyncMetadata)) {
                sendPacketWhenComplete(player, asyncMetadata);
            }
        }

        @Override
        public void updateNewViewer(@NotNull Player player) {
            player.sendPacket(getEntityType().registry().spawnType().getSpawnPacket(this));
            if (hasVelocity()) player.sendPacket(getVelocityPacket());

            CompletableFuture<SendablePacket> asyncMetadata = asyncMetadataPacket(this, formatter,
                player, formatString);

            boolean sentAsyncPacket = sendAsyncOrInitial(player, asyncMetadata);

            final Set<Entity> passengers = getPassengers();
            if (!passengers.isEmpty()) {
                for (Entity passenger : passengers) {
                    if (passenger != player) passenger.updateNewViewer(player);
                }
                player.sendPacket(getPassengersPacket());
            }
            player.sendPacket(new EntityHeadLookPacket(getEntityId(), position.yaw()));

            if (!sentAsyncPacket) {
                sendPacketWhenComplete(player, asyncMetadata);
            }
        }
    }

    /**
     * Creates a new instance of this class, whose holograms will be rendered at the given location, using the given
     * alignment, and only to players who satisfy the given predicate.
     *
     * @param location  the location of the instance
     * @param alignment the alignment method
     * @param canRender the predicate used to determine if this hologram should be visible
     */
    public ViewableHologram(@NotNull Point location, @NotNull Alignment alignment,
        @NotNull Predicate<? super Player> canRender) {
        super(location, alignment);
        this.canRender = Objects.requireNonNull(canRender);
    }

    /**
     * Creates a new instance of this class, whose holograms will be rendered at the given location, using the default
     * alignment {@link Alignment#UPPER}, and only to players who satisfy the given predicate.
     *
     * @param location  the location to render holograms
     * @param canRender the predicate used to determine if this hologram should be visible
     */
    public ViewableHologram(@NotNull Point location, @NotNull Predicate<? super Player> canRender) {
        this(location, Alignment.UPPER, canRender);
    }

    @Override
    protected @NotNull Entity constructEntity(@NotNull Component display) {
        Entity entity = super.constructEntity(display);
        entity.updateViewableRule(canRender);
        return entity;
    }

    @Override
    protected @NotNull Entity constructFormattedEntity(@NotNull String formatString, @NotNull LineFormatter formatter) {
        Entity entity = new FormattingEntity(formatter, formatString);
        ArmorStandMeta meta = (ArmorStandMeta) entity.getEntityMeta();
        meta.setMarker(true);
        meta.setHasNoGravity(true);
        meta.setCustomNameVisible(true);
        meta.setInvisible(true);

        entity.setHasPhysics(false);

        entity.updateViewableRule(canRender);
        return entity;
    }

    @Override
    protected void reformat(@NotNull Entity entity, @NotNull Player player) {
        if (!(entity instanceof FormattingEntity formattingEntity)) {
            return;
        }

        formattingEntity.reformat(player);
    }

    private static SendablePacket initialMetadataPacket(@NotNull Entity armorstand, @NotNull Component initialName) {
        Metadata metadata = buildMetadata(armorstand, initialName);
        return new LazyPacket(() -> new EntityMetaDataPacket(armorstand.getEntityId(), metadata.getEntries()));
    }

    private static CompletableFuture<SendablePacket> asyncMetadataPacket(@NotNull Entity armorstand,
        @NotNull LineFormatter lineFormatter, @NotNull Player player, @NotNull String formatString) {
        Reference<Entity> armorstandReference = new WeakReference<>(armorstand);
        return lineFormatter.apply(formatString, player).thenApply(customName -> {
            Entity referenced = armorstandReference.get();
            if (referenced == null || referenced.isRemoved()) {
                return null;
            }

            Metadata metadata = buildMetadata(referenced, customName);
            return new LazyPacket(() -> new EntityMetaDataPacket(referenced.getEntityId(), metadata.getEntries()));
        });
    }

    private static Metadata buildMetadata(Entity entity, Component name) {
        Metadata metadata = new Metadata(null);
        ArmorStandMeta meta = new ArmorStandMeta(entity, metadata);

        meta.setMarker(true);
        meta.setHasNoGravity(true);
        meta.setCustomNameVisible(true);
        meta.setInvisible(true);

        meta.setCustomName(name);

        return metadata;
    }

    public void updateViewableRules() {
        synchronized (super.sync) {
            for (Entry entry : super.entries) {
                entry.entity().updateViewableRule();
            }
        }
    }
}
