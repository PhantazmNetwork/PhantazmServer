package com.github.phantazmnetwork.core.hologram;

import net.kyori.adventure.text.Component;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.metadata.other.ArmorStandMeta;
import net.minestom.server.instance.Instance;
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
    private final Object sync;
    private Alignment alignment;
    private Instance instance;
    private Point location;

    /**
     * Creates a new instance of this class, whose holograms will be rendered at the given location, using the given
     * alignment.
     *
     * @param location  the location of the instance
     * @param gap       the distance between separate hologram messages
     * @param alignment the alignment method
     */
    public InstanceHologram(@NotNull Point location, double gap, @NotNull Alignment alignment) {
        this.alignment = Objects.requireNonNull(alignment, "alignment");
        this.location = Objects.requireNonNull(location, "location");
        armorStands = new ArrayList<>();
        components = new ArrayList<>();
        this.gap = gap;

        this.sync = new Object();
    }

    /**
     * Creates a new instance of this class, whose holograms will be rendered at the given location, using the default
     * alignment {@link Alignment#UPPER}.
     *
     * @param location the location to render holograms
     * @param gap      the distance between separate hologram messages
     */
    public InstanceHologram(@NotNull Point location, double gap) {
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
    public @NotNull Point getLocation() {
        return location;
    }

    @Override
    public void setLocation(@NotNull Point location) {
        Objects.requireNonNull(location, "location");
        synchronized (sync) {
            if (!location.equals(this.location)) {
                this.location = location;
                updateArmorStands();
            }
        }
    }

    @Override
    public void setInstance(@NotNull Instance instance) {
        Objects.requireNonNull(instance, "instance");
        synchronized (sync) {
            if (this.instance != instance) {
                this.instance = instance;
                updateArmorStands();
            }
        }
    }

    @Override
    public void setInstance(@NotNull Instance instance, @NotNull Point location) {
        Objects.requireNonNull(instance, "instance");
        Objects.requireNonNull(location, "location");
        synchronized (sync) {
            if (this.instance != instance || !location.equals(this.location)) {
                this.location = location;
                this.instance = instance;
                updateArmorStands();
            }
        }
    }

    @Override
    public void trimToSize() {
        synchronized (sync) {
            armorStands.trimToSize();
            components.trimToSize();
        }
    }

    @Override
    public @NotNull Component get(int index) {
        synchronized (sync) {
            return components.get(index);
        }
    }

    @Override
    public @NotNull Component set(int index, @NotNull Component element) {
        synchronized (sync) {
            armorStands.get(index).setCustomName(element);
            return components.set(index, Objects.requireNonNull(element, "element"));
        }
    }

    public void add(int index, @NotNull Component component) {
        synchronized (sync) {
            components.add(index, Objects.requireNonNull(component, "component"));
            armorStands.add(constructEntity(component));
            updateArmorStands();
        }
    }

    @Override
    public @NotNull Component remove(int index) {
        synchronized (sync) {
            armorStands.remove(index).remove();
            Component component = components.remove(index);
            updateArmorStands();
            return component;
        }
    }

    @Override
    public void clear() {
        synchronized (sync) {
            for (Entity entity : armorStands) {
                entity.remove();
            }
            armorStands.clear();
            components.clear();
        }
    }

    @Override
    public int size() {
        synchronized (sync) {
            return components.size();
        }
    }

    private void updateArmorStands() {
        if (instance == null) {
            return;
        }

        int armorStandCount = armorStands.size();
        double totalHeight = gap * (armorStandCount - 1) + armorStandCount * MESSAGE_HEIGHT;
        double topCornerHeight = location.y() + totalHeight / 2;

        for (int i = 0; i < armorStandCount; i++) {
            Entity armorStand = armorStands.get(i);
            Pos pos = new Pos(location.x(), topCornerHeight - (i * (gap + MESSAGE_HEIGHT)), location.z());
            switch (alignment) {
                case CENTERED -> pos = pos.add(0, totalHeight / 2, 0);
                case LOWER -> pos = pos.add(0, totalHeight, 0);
            }

            if (armorStand.getInstance() == instance) {
                armorStand.teleport(pos).join();
            }
            else {
                armorStand.setInstance(instance, pos).join();
            }
        }
    }

    /**
     * Constructs the entity used for each message in this hologram. This method may be overridden to modify the
     * entity's characteristics. The default implementation creates a marker armor stand with no gravity, a visible
     * custom name, invisibility, and a custom name from the given component.
     * <p>
     * The entity's instance is expected to be unset. It will be updated as appropriate by {@link InstanceHologram}.
     *
     * @return the entity to be used for the hologram
     */
    protected @NotNull Entity constructEntity(@NotNull Component display) {
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
