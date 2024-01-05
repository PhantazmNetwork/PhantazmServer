package org.phantazm.core.hologram;

import com.github.steanky.toolkit.collection.Containers;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.metadata.other.ArmorStandMeta;
import net.minestom.server.instance.Instance;
import org.jetbrains.annotations.NotNull;

import java.util.*;

/**
 * Instance-wide Hologram implementation. This object retains a strong reference to its instance, and therefore should
 * not be stored for longer than the lifetime of the instance.
 */
public class InstanceHologram extends AbstractList<Component> implements Hologram, RandomAccess {
    public static final double MESSAGE_HEIGHT = 0.25;

    protected final ArrayList<Entity> armorStands;
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
        this.alignment = Objects.requireNonNull(alignment);
        this.location = Objects.requireNonNull(location);
        armorStands = new ArrayList<>();
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
        Objects.requireNonNull(alignment);
        synchronized (sync) {
            if (alignment != this.alignment) {
                this.alignment = alignment;
                updateArmorStands();
            }
        }
    }

    @Override
    public @NotNull Point getLocation() {
        return location;
    }

    @Override
    public void setLocation(@NotNull Point location) {
        Objects.requireNonNull(location);
        synchronized (sync) {
            if (!location.equals(this.location)) {
                this.location = location;
                updateArmorStands();
            }
        }
    }

    @Override
    public void setInstance(@NotNull Instance instance) {
        Objects.requireNonNull(instance);
        synchronized (sync) {
            if (this.instance != instance) {
                this.instance = instance;
                updateArmorStands();
            }
        }
    }

    @Override
    public void setInstance(@NotNull Instance instance, @NotNull Point location) {
        Objects.requireNonNull(instance);
        Objects.requireNonNull(location);
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
        }
    }

    @Override
    public @NotNull Component get(int index) {
        synchronized (sync) {
            return Objects.requireNonNull(armorStands.get(index).getCustomName());
        }
    }

    @Override
    public @NotNull Component set(int index, @NotNull Component element) {
        synchronized (sync) {
            Entity armorStand = armorStands.get(index);
            Component oldName = Objects.requireNonNull(armorStand.getCustomName());
            armorStand.setCustomName(element);
            return oldName;
        }
    }

    @Override
    public void addFormatted(int index, @NotNull String formatString) {
        synchronized (sync) {
            armorStands.add(index, constructFormattedEntity(formatString));
            updateArmorStands();
        }
    }

    @Override
    public void addAllFormatted(int index, @NotNull Collection<? extends String> formatStrings) {
        synchronized (sync) {
            armorStands.addAll(index, Containers.mappedView(this::constructFormattedEntity, formatStrings));
            updateArmorStands();
        }
    }

    @Override
    public void addFormatted(@NotNull String formatString) {
        synchronized (sync) {
            armorStands.add(constructFormattedEntity(formatString));
            updateArmorStands();
        }
    }

    @Override
    public void addAllFormatted(@NotNull Collection<? extends String> formatStrings) {
        synchronized (sync) {
            armorStands.addAll(Containers.mappedView(this::constructFormattedEntity, formatStrings));
            updateArmorStands();
        }
    }

    @Override
    public void setFormatted(int index, @NotNull String formatString) {
        synchronized (sync) {
            armorStands.set(index, constructFormattedEntity(formatString));
            updateArmorStands();
        }
    }

    @Override
    public void add(int index, @NotNull Component component) {
        synchronized (sync) {
            armorStands.add(constructEntity(component));
            updateArmorStands();
        }
    }

    @Override
    public boolean addAll(int index, @NotNull Collection<? extends Component> c) {
        boolean changed;
        synchronized (sync) {
            changed = armorStands.addAll(index, c.stream().map(this::constructEntity).toList());
            updateArmorStands();
        }

        return changed;
    }

    @Override
    public boolean addAll(@NotNull Collection<? extends Component> c) {
        boolean changed;
        synchronized (sync) {
            changed = armorStands.addAll(c.stream().map(this::constructEntity).toList());
            updateArmorStands();
        }

        return changed;
    }

    @Override
    public @NotNull Component remove(int index) {
        synchronized (sync) {
            Entity armorStand = armorStands.remove(index);
            Component component = Objects.requireNonNull(armorStand.getCustomName());
            armorStand.remove();

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
        }
    }

    @Override
    public int size() {
        synchronized (sync) {
            return armorStands.size();
        }
    }

    private void updateArmorStands() {
        if (instance == null) {
            return;
        }

        int armorStandCount = armorStands.size();
        double totalHeight = gap * (armorStandCount - 1) + armorStandCount * MESSAGE_HEIGHT;
        double topEdgeHeight = location.y() + totalHeight / 2;

        for (int i = 0; i < armorStandCount; i++) {
            Entity armorStand = armorStands.get(i);
            Pos pos = new Pos(location.x(), topEdgeHeight - (i * (gap + MESSAGE_HEIGHT)), location.z());
            switch (alignment) {
                case CENTERED -> pos = pos.add(0, totalHeight / 2, 0);
                case LOWER -> pos = pos.add(0, totalHeight, 0);
            }

            if (armorStand.getInstance() == instance) {
                armorStand.teleport(pos).join();
            } else {
                armorStand.setInstance(instance, pos).join();
            }
        }
    }

    private Entity makeArmorStand(Component display) {
        Entity stand = new Entity(EntityType.ARMOR_STAND);

        ArmorStandMeta meta = (ArmorStandMeta) stand.getEntityMeta();
        meta.setMarker(true);
        meta.setHasNoGravity(true);
        meta.setCustomNameVisible(true);
        meta.setInvisible(true);
        meta.setCustomName(display);
        return stand;
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
        return makeArmorStand(display);
    }

    /**
     * Works identically to {@link InstanceHologram#constructEntity(Component)}, but its custom name is created by the
     * given {@link MiniMessage} format string. Called to create entities for the {@link Hologram#addFormatted(String)}
     * method (and derivatives).
     * <p>
     * Implementations may override this method to provide custom formatting for holograms.
     *
     * @param formatString the format string from which to create the custom name
     * @return the entity to be used for the hologram
     */
    protected @NotNull Entity constructFormattedEntity(@NotNull String formatString) {
        return makeArmorStand(MiniMessage.miniMessage().deserialize(formatString));
    }
}
