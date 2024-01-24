package org.phantazm.core.hologram;

import com.github.steanky.toolkit.collection.Containers;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.metadata.other.ArmorStandMeta;
import net.minestom.server.instance.Instance;
import org.jetbrains.annotations.NotNull;

import java.util.*;

/**
 * Instance-wide Hologram implementation. This object retains a strong reference to its instance, and therefore should
 * not be stored for longer than the lifetime of the instance.
 */
public class InstanceHologram extends AbstractList<Hologram.Line> implements Hologram, RandomAccess {
    public static final double MESSAGE_HEIGHT = 0.25;

    protected final ArrayList<Entry> entries;
    protected final Object sync;

    private Alignment alignment;
    private Point location;

    private Instance instance;

    protected record Entry(@NotNull Line line,
        @NotNull Entity entity) {
    }

    /**
     * Creates a new instance of this class, whose holograms will be rendered at the given location, using the given
     * alignment.
     *
     * @param location  the location of the instance
     * @param alignment the alignment method
     */
    public InstanceHologram(@NotNull Point location, @NotNull Alignment alignment) {
        this.entries = new ArrayList<>();
        this.sync = new Object();

        this.alignment = Objects.requireNonNull(alignment);
        this.location = Objects.requireNonNull(location);
    }

    /**
     * Creates a new instance of this class, whose holograms will be rendered at the given location, using the default
     * alignment {@link Alignment#UPPER}.
     *
     * @param location the location to render holograms
     */
    public InstanceHologram(@NotNull Point location) {
        this(location, Alignment.UPPER);
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
    public void reformatFor(int index, @NotNull Player player) {
        synchronized (sync) {
            reformat(entries.get(index).entity, player);
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
            entries.trimToSize();
        }
    }

    @Override
    public void addComponent(@NotNull Component component, double gap) {
        synchronized (sync) {
            entries.add(entryFromLine(Hologram.line(component, gap)));
            updateArmorStands();
        }
    }

    @Override
    public void addFormat(@NotNull String formatString, @NotNull LineFormatter lineFormatter, double gap) {
        synchronized (sync) {
            entries.add(entryFromLine(Hologram.line(formatString, lineFormatter, gap)));
            updateArmorStands();
        }
    }

    @Override
    public boolean addAllComponents(int index, @NotNull Collection<? extends Component> components, double gap) {
        synchronized (sync) {
            boolean result = entries.addAll(index, Containers.mappedView(component ->
                entryFromLine(Hologram.line(component, gap)), components));
            updateArmorStands();
            return result;
        }
    }

    @Override
    public boolean addAllComponents(@NotNull Collection<? extends Component> components, double gap) {
        synchronized (sync) {
            boolean result = entries.addAll(Containers.mappedView(component ->
                entryFromLine(Hologram.line(component, gap)), components));
            updateArmorStands();
            return result;
        }
    }

    @Override
    public boolean addAllFormats(@NotNull Collection<? extends String> formatStrings,
        @NotNull LineFormatter lineFormatter, double gap) {
        synchronized (sync) {
            boolean result = entries.addAll(Containers.mappedView(format ->
                entryFromLine(Hologram.line(format, lineFormatter, gap)), formatStrings));
            updateArmorStands();
            return result;
        }
    }

    @Override
    public @NotNull Component getComponent(int index) {
        synchronized (sync) {
            return Objects.requireNonNull(entries.get(index).entity.getCustomName());
        }
    }

    @Override
    public void destroy() {
        synchronized (sync) {
            clear0();
            this.instance = null;
        }
    }

    @Override
    public @NotNull Line get(int index) {
        synchronized (sync) {
            return Objects.requireNonNull(entries.get(index).line);
        }
    }

    @Override
    public @NotNull Line set(int index, @NotNull Line line) {
        synchronized (sync) {
            Entry old = entries.set(index, entryFromLine(line));
            old.entity.remove();
            updateArmorStands();
            return old.line;
        }
    }

    @Override
    public boolean addAll(@NotNull Collection<? extends Line> lines) {
        synchronized (sync) {
            boolean result = entries.addAll(Containers.mappedView(this::entryFromLine, lines));
            updateArmorStands();
            return result;
        }
    }

    @Override
    public void add(int index, @NotNull Line line) {
        synchronized (sync) {
            entries.add(index, entryFromLine(line));
            updateArmorStands();
        }
    }

    @Override
    public @NotNull Line remove(int index) {
        synchronized (sync) {
            Entry removedEntry = entries.remove(index);
            removedEntry.entity.remove();

            updateArmorStands();
            return removedEntry.line;
        }
    }

    private void clear0() {
        for (Entry entry : entries) {
            entry.entity.remove();
        }
        entries.clear();
        updateArmorStands();
    }

    @Override
    public void clear() {
        synchronized (sync) {
            clear0();
        }
    }

    @Override
    public int size() {
        synchronized (sync) {
            return entries.size();
        }
    }

    private void updateArmorStands() {
        if (instance == null) {
            return;
        }

        int armorStandCount = entries.size();
        if (armorStandCount == 0) {
            return;
        }

        double[] offsets = new double[armorStandCount];
        offsets[0] = 0;

        double gapSum = 0;
        for (int i = 0; i < armorStandCount - 1; i++) {
            gapSum += entries.get(i).line.gap();
            offsets[i + 1] = (i + 1) * MESSAGE_HEIGHT + gapSum;
        }

        double totalHeight = MESSAGE_HEIGHT * armorStandCount + gapSum;
        double topEdgeHeight = switch (alignment) {
            case UPPER -> location.y();
            case CENTERED -> location.y() + totalHeight / 2;
            case LOWER -> location.y() + totalHeight;
        };

        for (int i = 0; i < armorStandCount; i++) {
            Entity armorStand = entries.get(i).entity;
            Pos pos = new Pos(location.x(), topEdgeHeight - offsets[i], location.z());

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
        stand.setHasPhysics(false);
        return stand;
    }

    private Entry entryFromLine(Line line) {
        return new Entry(line, line.isComponent() ? constructEntity(line.component()) :
            constructFormattedEntity(line.format(), line.lineFormatter()));
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
     * given {@link MiniMessage} format string. Called to create entities that require custom formatting.
     * <p>
     * Implementations may override this method to provide custom formatting for holograms. The default implementation
     * does not make use of the line formatter at all.
     *
     * @param formatString the format string from which to create the custom name
     * @return the entity to be used for the hologram
     */
    protected @NotNull Entity constructFormattedEntity(@NotNull String formatString,
        @NotNull ViewableHologram.LineFormatter lineFormatter) {
        return makeArmorStand(MiniMessage.miniMessage().deserialize(formatString));
    }

    /**
     * Reformats a specific armor stand, causing its {@link Hologram.LineFormatter} function to be re-computed (if it
     * exists). The default implementation is a no-op. Subclasses that override
     * {@link InstanceHologram#constructFormattedEntity(String, LineFormatter)} should also override this method.
     *
     * @param entity the entity to reformat
     * @param player the player we are reformatting for
     */
    protected void reformat(@NotNull Entity entity, @NotNull Player player) {

    }
}
