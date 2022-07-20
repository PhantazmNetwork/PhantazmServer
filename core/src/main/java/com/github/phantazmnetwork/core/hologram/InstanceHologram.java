package com.github.phantazmnetwork.core.hologram;

import com.github.phantazmnetwork.commons.vector.Vec3D;
import net.kyori.adventure.text.Component;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.metadata.other.ArmorStandMeta;
import net.minestom.server.instance.Instance;
import net.minestom.server.scoreboard.Sidebar;
import org.jetbrains.annotations.NotNull;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Objects;

/**
 * Instance-wide Hologram implementation. This object retains a strong reference to its instance, and therefore should
 * not be stored for longer than the lifetime of the instance.
 */
public class InstanceHologram extends AbstractList<Component> implements Hologram {
    private static final double MESSAGE_HEIGHT = 0.25;

    private final ArrayList<Entity> armorStands;
    private final ArrayList<Component> components;
    private final double gap;
    private Alignment alignment;
    private Instance instance;
    private Vec3D location;

    /**
     * Creates a new instance of this class, whose holograms will be rendered at the given location, using the given
     * alignment.
     *
     * @param location  the location of the instance
     * @param gap       the distance between separate hologram messages
     * @param alignment the alignment method
     */
    public InstanceHologram(@NotNull Vec3D location, double gap, @NotNull Alignment alignment) {
        this.alignment = Objects.requireNonNull(alignment, "alignment");
        this.location = Objects.requireNonNull(location, "location");
        armorStands = new ArrayList<>();
        components = new ArrayList<>();
        this.gap = gap;
    }

    /**
     * Creates a new instance of this class, whose holograms will be rendered at the given location, using the default
     * alignment {@link Alignment#UPPER}.
     *
     * @param location the location to render holograms
     * @param gap      the distance between separate hologram messages
     */
    public InstanceHologram(@NotNull Vec3D location, double gap) {
        this(location, gap, Alignment.UPPER);
    }

    @Override
    public void setAlignment(@NotNull Alignment alignment) {
        if (alignment != this.alignment) {
            this.alignment = alignment;
            updateArmorStands();
        }
    }

    @Override
    public @NotNull Vec3D getLocation() {
        return location;
    }

    @Override
    public void setLocation(@NotNull Vec3D location) {
        Objects.requireNonNull(location, "location");
        if (!location.equals(this.location)) {
            this.location = location;
            updateArmorStands();
        }
    }

    @Override
    public void setInstance(@NotNull Instance instance) {
        Objects.requireNonNull(instance, "instance");
        if (this.instance != instance) {
            this.instance = instance;
            updateArmorStands();
        }
    }

    @Override
    public void trimToSize() {
        armorStands.trimToSize();
        components.trimToSize();
    }

    @Override
    public @NotNull Component remove(int index) {
        armorStands.remove(index).remove();
        Component component = components.remove(index);
        updateArmorStands();
        return component;
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
    public void clear() {
        for (Entity entity : armorStands) {
            entity.remove();
        }
        armorStands.clear();
        components.clear();
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
        if (instance == null) {
            return;
        }

        int armorStandCount = armorStands.size();
        double totalHeight = gap * (armorStandCount - 1) + armorStandCount * MESSAGE_HEIGHT;
        double topCornerHeight = location.getY() + totalHeight / 2;

        for (int i = 0; i < armorStandCount; i++) {
            Entity armorStand = armorStands.get(i);
            Pos pos = new Pos(location.getX(), topCornerHeight - (i * (gap + MESSAGE_HEIGHT)), location.getZ());
            switch (alignment) {
                case CENTERED -> pos = pos.add(0, totalHeight / 2, 0);
                case LOWER -> pos = pos.add(0, totalHeight, 0);
            }

            if (armorStand.getInstance() == instance) {
                armorStand.teleport(pos);
            }
            else {
                armorStand.setInstance(instance, pos);
            }
        }
    }

    private Entity makeArmorStand(Component display) {
        Entity stand = new Entity(EntityType.ARMOR_STAND);
        ArmorStandMeta meta = (ArmorStandMeta)stand.getEntityMeta();
        meta.setMarker(true);
        meta.setHasNoGravity(true);
        meta.setCustomNameVisible(true);
        meta.setInvisible(true);
        meta.setCustomName(display);
        return stand;
    }
}
