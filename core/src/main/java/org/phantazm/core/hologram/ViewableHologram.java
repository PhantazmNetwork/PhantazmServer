package org.phantazm.core.hologram;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.minestom.server.coordinate.Point;
import net.minestom.server.entity.*;
import net.minestom.server.entity.metadata.other.ArmorStandMeta;
import net.minestom.server.network.packet.server.LazyPacket;
import net.minestom.server.network.packet.server.SendablePacket;
import net.minestom.server.network.packet.server.play.EntityHeadLookPacket;
import net.minestom.server.network.packet.server.play.EntityMetaDataPacket;
import org.jetbrains.annotations.NotNull;
import org.phantazm.commons.FutureUtils;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiFunction;
import java.util.function.Predicate;

/**
 * A subclass of {@link InstanceHologram} which additionally may be set up to only render for players who satisfy a
 * certain predicate. A {@link LineFormatter} may be provided to (optionally asynchronously) format lines passed to
 * {@link Hologram#addFormatted(String)} and derivatives.
 */
public class ViewableHologram extends InstanceHologram {
    /**
     * A function that accepts a {@link String} and {@link Player} and returns a {@link CompletableFuture} that computes
     * a (possibly new) Component that will be shown only to that specific player.
     */
    public interface LineFormatter extends BiFunction<String, Player, CompletableFuture<Component>> {
        /**
         * The initial value of the line. Will be shown while formatting is ongoing.
         *
         * @return the initial format value
         */
        @NotNull Component initialValue();
    }

    public static final LineFormatter DEFAULT_LINE_FORMATTER = new LineFormatter() {
        @Override
        public @NotNull Component initialValue() {
            return Component.empty();
        }

        @Override
        public CompletableFuture<Component> apply(String string, Player player) {
            return FutureUtils.completedFuture(MiniMessage.miniMessage().deserialize(string));
        }
    };

    private final Predicate<? super Player> canRender;
    private final LineFormatter lineFormatter;

    /**
     * Creates a new instance of this class, whose holograms will be rendered at the given location, using the given
     * alignment, and only to players who satisfy the given predicate.
     *
     * @param location      the location of the instance
     * @param gap           the distance between separate hologram messages
     * @param alignment     the alignment method
     * @param canRender     the predicate used to determine if this hologram should be visible
     * @param lineFormatter the formatter used to create player-specific hologram lines
     */
    public ViewableHologram(@NotNull Point location, double gap, @NotNull Alignment alignment,
        @NotNull Predicate<? super Player> canRender, @NotNull LineFormatter lineFormatter) {
        super(location, gap, alignment);
        this.canRender = Objects.requireNonNull(canRender);
        this.lineFormatter = Objects.requireNonNull(lineFormatter);
    }

    /**
     * Creates a new instance of this class, whose holograms will be rendered at the given location, using the given
     * alignment, and only to players who satisfy the given predicate.
     *
     * @param location  the location of the instance
     * @param gap       the distance between separate hologram messages
     * @param alignment the alignment method
     * @param canRender the predicate used to determine if this hologram should be visible
     */
    public ViewableHologram(@NotNull Point location, double gap, @NotNull Alignment alignment,
        @NotNull Predicate<? super Player> canRender) {
        this(location, gap, alignment, canRender, DEFAULT_LINE_FORMATTER);
    }

    /**
     * Creates a new instance of this class, whose holograms will be rendered at the given location, using the default
     * alignment {@link Alignment#UPPER}, and only to players who satisfy the given predicate.
     *
     * @param location      the location to render holograms
     * @param gap           the distance between separate hologram messages
     * @param canRender     the predicate used to determine if this hologram should be visible
     * @param lineFormatter the formatter used to create player-specific hologram lines
     */
    public ViewableHologram(@NotNull Point location, double gap, @NotNull Predicate<? super Player> canRender,
        @NotNull LineFormatter lineFormatter) {
        this(location, gap, Alignment.UPPER, canRender, lineFormatter);
    }

    /**
     * Creates a new instance of this class, whose holograms will be rendered at the given location, using the default
     * alignment {@link Alignment#UPPER}, and only to players who satisfy the given predicate.
     *
     * @param location  the location to render holograms
     * @param gap       the distance between separate hologram messages
     * @param canRender the predicate used to determine if this hologram should be visible
     */
    public ViewableHologram(@NotNull Point location, double gap, @NotNull Predicate<? super Player> canRender) {
        this(location, gap, Alignment.UPPER, canRender, DEFAULT_LINE_FORMATTER);
    }

    @Override
    protected @NotNull Entity constructFormattedEntity(@NotNull String formatString) {
        LineFormatter formatter = this.lineFormatter;
        Entity entity = new Entity(EntityType.ARMOR_STAND) {
            @Override
            public void updateNewViewer(@NotNull Player player) {
                player.sendPacket(getEntityType().registry().spawnType().getSpawnPacket(this));
                if (hasVelocity()) player.sendPacket(getVelocityPacket());

                CompletableFuture<SendablePacket> asyncMetadata = asyncMetadataPacket(this, formatter,
                    player, formatString);

                boolean sentAsyncPacket;
                SendablePacket packet;
                if (asyncMetadata.isDone()) {
                    packet = asyncMetadata.join();
                    sentAsyncPacket = true;
                } else {
                    //used as an initial value for the metadata while we wait for the async metadata
                    packet = initialMetadataPacket(this, formatter.initialValue());
                    sentAsyncPacket = false;
                }

                player.sendPacket(packet);
                final Set<Entity> passengers = getPassengers();
                if (!passengers.isEmpty()) {
                    for (Entity passenger : passengers) {
                        if (passenger != player) passenger.updateNewViewer(player);
                    }
                    player.sendPacket(getPassengersPacket());
                }
                player.sendPacket(new EntityHeadLookPacket(getEntityId(), position.yaw()));

                if (sentAsyncPacket) {
                    return;
                }

                //don't hold the player in memory the entire time we wait for the metadata!
                Reference<Player> playerReference = new WeakReference<>(player);
                asyncMetadata.thenAccept(asyncPacket -> {
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
        };

        entity.updateViewableRule(canRender);
        return entity;
    }

    private static SendablePacket initialMetadataPacket(@NotNull Entity armorstand, @NotNull Component initialName) {
        Metadata metadata = buildMetadata(armorstand, initialName);
        return new LazyPacket(() -> new EntityMetaDataPacket(armorstand.getEntityId(), metadata.getEntries()));
    }

    private static CompletableFuture<SendablePacket> asyncMetadataPacket(@NotNull Entity armorstand,
        @NotNull LineFormatter lineFormatter, @NotNull Player player, @NotNull String formatString) {
        Reference<Entity> armorstandReference = new WeakReference<>(armorstand);
        return lineFormatter.apply(formatString, player).thenApply(customName -> {
            Entity referencedEntity = armorstandReference.get();
            if (referencedEntity == null || referencedEntity.isRemoved()) {
                return null;
            }

            Metadata metadata = buildMetadata(referencedEntity, customName);
            return new LazyPacket(() -> new EntityMetaDataPacket(referencedEntity.getEntityId(), metadata.getEntries()));
        });
    }

    private static Metadata buildMetadata(Entity entity, Component name) {
        Metadata metadata = new Metadata(entity);
        ArmorStandMeta meta = new ArmorStandMeta(entity, metadata);

        meta.setMarker(true);
        meta.setHasNoGravity(true);
        meta.setCustomNameVisible(true);
        meta.setInvisible(true);

        meta.setCustomName(name);

        return metadata;
    }

    public void updateViewableRules() {
        for (Entity armorStand : super.armorStands) {
            armorStand.updateViewableRule();
        }
    }
}
