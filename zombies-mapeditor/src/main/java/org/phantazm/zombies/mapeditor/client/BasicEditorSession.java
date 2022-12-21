package org.phantazm.zombies.mapeditor.client;

import com.github.steanky.vector.Bounds3I;
import com.github.steanky.vector.Vec3I;
import me.x150.renderer.renderer.color.Color;
import net.kyori.adventure.key.Key;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Items;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnmodifiableView;
import org.phantazm.commons.FileUtils;
import org.phantazm.commons.Namespaces;
import org.phantazm.zombies.map.*;
import org.phantazm.zombies.mapeditor.client.render.ObjectRenderer;
import org.phantazm.zombies.mapeditor.client.render.RenderUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

/**
 * Basic implementation of {@link EditorSession}.
 */
public class BasicEditorSession implements EditorSession {
    private static final Logger LOGGER = LoggerFactory.getLogger(BasicEditorSession.class);

    private static final Color SELECTION_COLOR = new Color(0, 255, 0, 128);
    private static final Color CURSOR_COLOR = Color.RED;
    private static final Color OUTLINE_COLOR = Color.BLACK;
    private static final Color ORIGIN_COLOR = new Color(0, 0, 255, 32);
    private static final Color ROOM_COLOR = new Color(255, 255, 255, 64);
    private static final Color DOOR_COLOR = new Color(189, 0, 255, 64);
    private static final Color WINDOW_COLOR = new Color(0, 251, 201, 64);
    private static final Color SPAWNPOINT_COLOR = new Color(252, 243, 1, 64);
    private static final Color SHOP_COLOR = new Color(255, 72, 5, 64);

    private static final Vec3i ONE = new Vec3i(1, 1, 1);
    private static final Vec3d HALF = new Vec3d(0.5, 0.5, 0.5);

    private static final Key SELECTION_KEY = Key.key(Namespaces.PHANTAZM, "mapeditor.selection");
    private static final Key OUTLINE_KEY = Key.key(Namespaces.PHANTAZM, "mapeditor.selection_outline");
    private static final Key CURSOR_KEY = Key.key(Namespaces.PHANTAZM, "mapeditor.cursor");
    private static final Key ORIGIN_KEY = Key.key(Namespaces.PHANTAZM, "mapeditor.origin");

    private final ObjectRenderer renderer;
    private final Path mapFolder;
    private final Loader<MapInfo> loader;
    private final Map<Key, MapInfo> maps;
    private final Map<Key, MapInfo> unmodifiableMaps;
    private RoomInfo lastRoom;
    private DoorInfo lastDoor;
    private Key lastSpawnrule;
    private boolean enabled;
    private Vec3i firstSelected;
    private Vec3i secondSelected;
    private MapInfo currentMap;

    /**
     * Constructs a new instance of this class using the provided parameters.
     *
     * @param renderer  the {@link ObjectRenderer} used to render map objects
     * @param loader    the {@link Loader} used to save or load map data
     * @param mapFolder the {@link Path} which contains saved map data
     */
    public BasicEditorSession(@NotNull ObjectRenderer renderer, @NotNull Loader<MapInfo> loader,
            @NotNull Path mapFolder) {
        this.renderer = Objects.requireNonNull(renderer, "renderer");
        this.mapFolder = Objects.requireNonNull(mapFolder, "mapFolder");
        this.loader = Objects.requireNonNull(loader, "loader");
        this.maps = new HashMap<>();
        this.unmodifiableMaps = Collections.unmodifiableMap(maps);
    }

    @Override
    public @NotNull ActionResult handleBlockUse(@NotNull PlayerEntity player, @NotNull World world, @NotNull Hand hand,
            @NotNull BlockHitResult blockHitResult) {
        if (!enabled || !player.getInventory().getMainHandStack().getItem().equals(Items.STICK)) {
            return ActionResult.PASS;
        }

        if (hand == Hand.MAIN_HAND) {
            Vec3i first = firstSelected;
            Vec3i newSelection = blockHitResult.getBlockPos();

            if (first == null) {
                firstSelected = newSelection;
                secondSelected = newSelection;
                updateSelectionRender(newSelection, ONE, newSelection);
            }
            else {
                Vec3i min = new Vec3i(Math.min(newSelection.getX(), first.getX()),
                        Math.min(newSelection.getY(), first.getY()), Math.min(newSelection.getZ(), first.getZ()));
                Vec3i max = new Vec3i(Math.max(newSelection.getX(), first.getX()),
                        Math.max(newSelection.getY(), first.getY()), Math.max(newSelection.getZ(), first.getZ()));

                secondSelected = firstSelected;
                firstSelected = newSelection;

                updateSelectionRender(min, max.subtract(min).add(1, 1, 1), newSelection);
            }

            return ActionResult.CONSUME;
        }

        return ActionResult.PASS;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
        this.renderer.setEnabled(enabled);

        if (enabled) {
            refreshMap();
        }
        else {
            firstSelected = null;
            secondSelected = null;

            renderer.clear();
        }
    }

    @Override
    public boolean hasSelection() {
        return firstSelected != null && secondSelected != null;
    }

    @Override
    public @NotNull Vec3I getFirstSelection() {
        if (firstSelected == null) {
            throw new IllegalStateException("No selection");
        }

        return Vec3I.immutable(firstSelected.getX(), firstSelected.getY(), firstSelected.getZ());
    }

    @Override
    public @NotNull Vec3I getSecondSelection() {
        if (secondSelected == null) {
            throw new IllegalStateException("No selection");
        }

        return Vec3I.immutable(secondSelected.getX(), secondSelected.getY(), secondSelected.getZ());
    }

    @Override
    public @NotNull Bounds3I getSelection() {
        Vec3I first = getFirstSelection();
        Vec3I second = getSecondSelection();

        int minX = Math.min(first.x(), second.x());
        int minY = Math.min(first.y(), second.y());
        int minZ = Math.min(first.z(), second.z());

        int lenX = Math.abs(second.x() - first.x());
        int lenY = Math.abs(second.y() - first.y());
        int lenZ = Math.abs(second.z() - first.z());


        return Bounds3I.immutable(minX, minY, minZ, lenX, lenY, lenZ);
    }

    @Override
    public boolean hasMap() {
        return currentMap != null;
    }

    @Override
    public @NotNull MapInfo getMap() {
        if (currentMap == null) {
            throw new IllegalStateException("No map");
        }

        return currentMap;
    }

    @Override
    public void addMap(@NotNull MapInfo map) {
        Objects.requireNonNull(map, "map");

        Key id = map.settings().id();
        if (maps.containsKey(id)) {
            throw new IllegalArgumentException("A map with id " + id + " already exists");
        }

        maps.put(id, map);
    }

    @Override
    public boolean containsMap(@NotNull Key id) {
        Objects.requireNonNull(id, "id");
        return maps.containsKey(id);
    }

    @Override
    public void removeMap(@NotNull Key id) {
        Objects.requireNonNull(id, "id");
        maps.remove(id);

        if (currentMap != null && currentMap.settings().id().equals(id)) {
            currentMap = null;
            refreshMap();
        }
    }

    @Override
    public @UnmodifiableView @NotNull Map<Key, MapInfo> mapView() {
        return unmodifiableMaps;
    }

    @Override
    public void setCurrent(@NotNull Key id) {
        Objects.requireNonNull(id, "id");

        MapInfo newCurrent = maps.get(id);
        if (newCurrent == null) {
            throw new IllegalArgumentException("A map with id " + id + " does not exist");
        }

        currentMap = newCurrent;
        refreshMap();
    }

    @Override
    public void loadMapsFromDisk() {
        try {
            Key oldId = null;
            if (currentMap != null) {
                oldId = currentMap.settings().id();
            }

            Map<Key, MapInfo> newMaps = loadMaps();
            maps.clear();
            maps.putAll(newMaps);
            currentMap = maps.get(oldId);

            refreshMap();
        }
        catch (IOException e) {
            LOGGER.warn("IOException when loading maps", e);
        }
    }

    @Override
    public void saveMapsToDisk() {
        Set<String> savedMaps = new HashSet<>(maps.values().size());
        for (MapInfo map : maps.values()) {
            try {
                loader.save(map);
                savedMaps.add(map.settings().id().value());
            }
            catch (IOException e) {
                LOGGER.warn("Error when trying to save map " + map.settings().id(), e);
            }
        }

        try {
            FileUtils.forEachFileMatching(mapFolder, (path, attr) -> attr.isDirectory() && !path.equals(mapFolder) &&
                    !savedMaps.contains(path.getFileName().toString()), mapFolder -> {
                String name = mapFolder.getFileName().toString();

                try {
                    loader.delete(name);
                    LOGGER.info("Successfully deleted map " + name);
                }
                catch (IOException e) {
                    LOGGER.warn("IOException when deleting map " + name, e);
                }
            });
        }
        catch (IOException e) {
            LOGGER.warn("IOException when deleting maps", e);
        }
    }

    @Override
    public void setLastRoom(@Nullable RoomInfo room) {
        this.lastRoom = room;
    }

    @Override
    public void setLastDoor(@Nullable DoorInfo door) {
        this.lastDoor = door;
    }

    @Override
    public void setLastSpawnrule(@Nullable Key spawnruleId) {
        this.lastSpawnrule = spawnruleId;
    }

    @Override
    @SuppressWarnings("PatternValidation")
    public void refreshRooms() {
        requireMap();

        for (RoomInfo room : currentMap.rooms()) {
            renderer.putObject(
                    new ObjectRenderer.RenderObject(Key.key(Namespaces.PHANTAZM, "room." + room.id().value()),
                            ObjectRenderer.RenderType.FILLED, ROOM_COLOR, true, false,
                            RenderUtils.arrayFromRegions(room.regions(), currentMap.settings().origin())));
        }
    }

    @SuppressWarnings("PatternValidation")
    @Override
    public void refreshDoors() {
        requireMap();

        for (DoorInfo door : currentMap.doors()) {
            renderer.putObject(
                    new ObjectRenderer.RenderObject(Key.key(Namespaces.PHANTAZM, "door." + door.id().value()),
                            ObjectRenderer.RenderType.FILLED, DOOR_COLOR, true, false,
                            RenderUtils.arrayFromRegions(door.regions(), currentMap.settings().origin())));
        }
    }

    @Override
    public void refreshWindows() {
        requireMap();

        renderer.removeIf(key -> key.value().startsWith("window."));
        int i = 0;
        for (WindowInfo window : currentMap.windows()) {
            renderer.putObject(new ObjectRenderer.RenderObject(Key.key(Namespaces.PHANTAZM, "window." + i++),
                    ObjectRenderer.RenderType.FILLED, WINDOW_COLOR, true, false,
                    RenderUtils.arrayFromRegion(window.frameRegion(), currentMap.settings().origin(), new Vec3d[2],
                            0)));
        }
    }

    @Override
    public void refreshSpawnpoints() {
        requireMap();

        renderer.removeIf(key -> key.value().startsWith("spawnpoint."));
        int i = 0;
        for (SpawnpointInfo spawnpointInfo : currentMap.spawnpoints()) {
            renderer.putObject(new ObjectRenderer.RenderObject(Key.key(Namespaces.PHANTAZM, "spawnpoint." + i++),
                    ObjectRenderer.RenderType.FILLED, SPAWNPOINT_COLOR, true, false,
                    RenderUtils.arrayFromRegion(Bounds3I.immutable(spawnpointInfo.position(), Vec3I.immutable(1, 1, 1)),
                            currentMap.settings().origin(), new Vec3d[2], 0)));
        }
    }

    @Override
    public void refreshShops() {
        requireMap();

        renderer.removeIf(key -> key.value().startsWith("shop."));
        int i = 0;
        for (ShopInfo shopInfo : currentMap.shops()) {
            renderer.putObject(new ObjectRenderer.RenderObject(Key.key(Namespaces.PHANTAZM, "shop." + i++),
                    ObjectRenderer.RenderType.FILLED, SHOP_COLOR, true, false, RenderUtils.arrayFromRegion(
                    Bounds3I.immutable(shopInfo.triggerLocation(), Vec3I.immutable(1, 1, 1)),
                    currentMap.settings().origin(), new Vec3d[2], 0)));
        }
    }

    @Override
    public @Nullable RoomInfo lastRoom() {
        return lastRoom;
    }

    @Override
    public @Nullable DoorInfo lastDoor() {
        return lastDoor;
    }

    @Override
    public @Nullable Key lastSpawnrule() {
        return lastSpawnrule;
    }

    private Map<Key, MapInfo> loadMaps() throws IOException {
        Map<Key, MapInfo> newMaps = new HashMap<>();
        Files.createDirectories(mapFolder);
        FileUtils.forEachFileMatching(mapFolder, (path, attr) -> attr.isDirectory() && !path.equals(mapFolder),
                mapFolder -> {
                    LOGGER.info("Trying to load map from " + mapFolder);
                    String name = mapFolder.getFileName().toString();

                    try {
                        MapInfo map = loader.load(name);
                        newMaps.put(map.settings().id(), map);
                        LOGGER.info("Successfully loaded map " + name);
                    }
                    catch (IOException e) {
                        LOGGER.warn("IOException when loading map " + name, e);
                    }
                });

        return newMaps;
    }

    private void refreshMap() {
        renderer.removeIf(key -> !(key.equals(CURSOR_KEY) || key.equals(OUTLINE_KEY) || key.equals(SELECTION_KEY)));

        if (currentMap == null) {
            return;
        }

        MapSettingsInfo info = currentMap.settings();
        renderer.putObject(
                new ObjectRenderer.RenderObject(ORIGIN_KEY, ObjectRenderer.RenderType.FILLED, ORIGIN_COLOR, true, true,
                        RenderUtils.arrayFromRegion(Bounds3I.immutable(info.origin(), Vec3I.immutable(1, 1, 1)),
                                Vec3I.ORIGIN, new Vec3d[2], 0)));

        refreshRooms();
        refreshDoors();
        refreshWindows();
        refreshSpawnpoints();
        refreshShops();
    }

    private void updateSelectionRender(Vec3i areaStart, Vec3i dimensions, Vec3i clicked) {
        Vec3d startVec = new Vec3d(areaStart.getX() - ObjectRenderer.EPSILON, areaStart.getY() - ObjectRenderer.EPSILON,
                areaStart.getZ() - ObjectRenderer.EPSILON);
        Vec3d dimensionsVec = new Vec3d(dimensions.getX() + ObjectRenderer.DOUBLE_EPSILON,
                dimensions.getY() + ObjectRenderer.DOUBLE_EPSILON, dimensions.getZ() + ObjectRenderer.DOUBLE_EPSILON);
        Vec3d clickedVec = new Vec3d(clicked.getX(), clicked.getY(), clicked.getZ());

        renderer.putObject(
                new ObjectRenderer.RenderObject(SELECTION_KEY, ObjectRenderer.RenderType.FILLED, SELECTION_COLOR, true,
                        false, startVec, dimensionsVec));
        renderer.putObject(
                new ObjectRenderer.RenderObject(OUTLINE_KEY, ObjectRenderer.RenderType.OUTLINE, OUTLINE_COLOR, true,
                        false, startVec, dimensionsVec));
        renderer.putObject(
                new ObjectRenderer.RenderObject(CURSOR_KEY, ObjectRenderer.RenderType.OUTLINE, CURSOR_COLOR, true, true,
                        clickedVec.add(0.25, 0.25, 0.25), HALF));
    }

    private void requireMap() {
        if (currentMap == null) {
            throw new IllegalStateException("No map");
        }
    }
}
