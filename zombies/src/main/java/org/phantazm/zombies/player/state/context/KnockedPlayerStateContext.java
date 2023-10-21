package org.phantazm.zombies.player.state.context;

import net.kyori.adventure.text.Component;
import net.minestom.server.coordinate.Point;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.metadata.other.ArmorStandMeta;
import net.minestom.server.instance.Instance;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.Optional;

public class KnockedPlayerStateContext {

    private final Point knockLocation;

    private final Component knockRoom;

    private final Component killer;

    private final Entity vehicle;

    public KnockedPlayerStateContext(@NotNull Instance instance, @NotNull Point knockLocation,
        @Nullable Component knockRoom, @Nullable Component killer) {
        this.knockLocation = Objects.requireNonNull(knockLocation);
        this.knockRoom = knockRoom;
        this.killer = killer;


        Entity vehicle = new Entity(EntityType.ARMOR_STAND);

        ArmorStandMeta armorStandMeta = (ArmorStandMeta) vehicle.getEntityMeta();
        armorStandMeta.setInvisible(true);
        armorStandMeta.setHasNoGravity(true);
        armorStandMeta.setMarker(true);
        vehicle.setInstance(instance, knockLocation.sub(0, 1, 0));

        this.vehicle = Objects.requireNonNull(vehicle);
    }

    public @NotNull Point getKnockLocation() {
        return knockLocation;
    }

    public Optional<Component> getKnockRoom() {
        return Optional.ofNullable(knockRoom);
    }

    public @NotNull Optional<Component> getKiller() {
        return Optional.ofNullable(killer);
    }

    public @NotNull Entity getVehicle() {
        return vehicle;
    }

}
