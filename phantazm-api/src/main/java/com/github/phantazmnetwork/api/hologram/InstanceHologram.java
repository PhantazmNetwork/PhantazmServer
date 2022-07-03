package com.github.phantazmnetwork.api.hologram;

import com.github.phantazmnetwork.api.VecUtils;
import com.github.phantazmnetwork.commons.vector.Vec3D;
import net.kyori.adventure.text.Component;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.metadata.other.ArmorStandMeta;
import net.minestom.server.instance.Instance;
import org.jetbrains.annotations.NotNull;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class InstanceHologram extends AbstractList<Component> implements Hologram {
    private static final double MESSAGE_HEIGHT = 0.25;

    private final Instance instance;
    private final List<Entity> armorStands;
    private final List<Component> components;
    private final Vec3D location;
    private final double gap;

    public InstanceHologram(@NotNull Instance instance, @NotNull Vec3D location, double gap) {
        this.instance = Objects.requireNonNull(instance, "instance");
        this.location = Objects.requireNonNull(location, "location");
        armorStands = new ArrayList<>();
        components = new ArrayList<>();
        this.gap = gap;
    }

    @Override
    public @NotNull Vec3D getLocation() {
        return location;
    }

    @Override
    public @NotNull Component remove(int index) {
        armorStands.remove(index).remove();
        updateArmorStands();
        return components.remove(index);
    }

    @Override
    public @NotNull Component set(int index, @NotNull Component element) {
        armorStands.get(index).setCustomName(element);
        return components.set(index, Objects.requireNonNull(element, "element"));
    }

    public void add(int index, @NotNull Component component) {
        components.add(index, Objects.requireNonNull(component, "component"));
        armorStands.add(makeArmorStand(component));
        updateArmorStands();
    }

    @Override
    public @NotNull Component get(int index) {
        return components.get(index);
    }

    @Override
    public int size() {
        return components.size();
    }

    private void updateArmorStands() {
        int armorStandsCount = armorStands.size();
        double totalHeight = gap * (armorStandsCount - 1) + armorStandsCount * MESSAGE_HEIGHT;
        double topCornerHeight = location.getY() + totalHeight / 2;

        for(int i = 0; i < armorStandsCount; i++) {
            armorStands.get(i).teleport(new Pos(location.getX(), topCornerHeight - (i * (gap + MESSAGE_HEIGHT)),
                    location.getZ()));
        }
    }

    private Entity makeArmorStand(Component display) {
        Entity stand = new Entity(EntityType.ARMOR_STAND);
        ArmorStandMeta meta = (ArmorStandMeta) stand.getEntityMeta();
        meta.setHasNoBasePlate(true);
        meta.setMarker(true);
        meta.setSmall(true);
        meta.setHasNoGravity(true);
        meta.setCustomNameVisible(true);
        meta.setInvisible(true);
        meta.setCustomName(display);
        stand.setInstance(instance, VecUtils.toPoint(location));

        return stand;
    }
}
