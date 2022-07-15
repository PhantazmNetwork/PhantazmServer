package com.github.phantazmnetwork.zombies.mapeditor.client;

import com.github.phantazmnetwork.commons.vector.Region3I;
import com.github.phantazmnetwork.commons.vector.Vec3I;
import com.github.phantazmnetwork.zombies.map.DoorInfo;
import com.github.phantazmnetwork.zombies.map.MapInfo;
import com.github.phantazmnetwork.zombies.map.RoomInfo;
import net.kyori.adventure.key.Key;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnmodifiableView;

import java.util.Map;

/**
 * Represents an ongoing map editing session. Encapsulates some form of rendering maps visually, switching between
 * active maps, and getting basic feedback from the player (such as what blocks they have selected).
 */
public interface EditorSession {
    /**
     * Called whenever the player interacts with a block.
     *
     * @param player         the player
     * @param world          the world the player is in
     * @param hand           the {@link Hand} used for this interaction
     * @param blockHitResult the {@link BlockHitResult} for this interaction
     * @return the desired {@link ActionResult}
     */
    @NotNull ActionResult handleBlockUse(@NotNull PlayerEntity player, @NotNull World world, @NotNull Hand hand,
                                         @NotNull BlockHitResult blockHitResult);

    /**
     * Queries if the editor is enabled or not.
     *
     * @return true if the editor is enabled, false otherwise
     */
    boolean isEnabled();

    /**
     * "Enables" or "disables" the mapeditor. When the editor is enabled, map objects will be visually rendered, and the
     * EditorSession API can be used to modify or create maps. When the editor is disabled, modifications can be made,
     * but player interaction with this session is disabled, and nothing is rendered visually.
     *
     * @param enabled true to enable the editor, false otherwise
     */
    void setEnabled(boolean enabled);

    /**
     * Checks if the editor currently has a selection. The editor has a selection if it is enabled and the player has
     * right-clicked at least one block with a stick.
     *
     * @return true if a selection has been made, false otherwise
     */
    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    boolean hasSelection();

    /**
     * Gets the first selected block, which is the <i>last</i> block the player right-clicked. If there is no selection,
     * an {@link IllegalArgumentException} will be thrown.
     *
     * @return a {@link Vec3I} representing the last block right-clicked in world coordinate space
     * @throws IllegalStateException if there is no selection
     * @see EditorSession#hasSelection()
     */
    @NotNull Vec3I getFirstSelection();

    /**
     * Gets the second selected block, which is the <i>second-last</i> block the player right-clicked. If there is no
     * selection, an {@link IllegalArgumentException} will be thrown.
     *
     * @return a {@link Vec3I} representing the second-last block right-clicked in world coordinate space
     * @throws IllegalStateException if there is no selection
     * @see EditorSession#hasSelection()
     */
    @NotNull Vec3I getSecondSelection();

    /**
     * Gets the region defined by both the first and second selections.
     *
     * @return the region encompassing both the first and second selections
     */
    @NotNull Region3I getSelection();

    /**
     * Determines if this session has an active map.
     *
     * @return true if there is an active map, false otherwise
     */
    boolean hasMap();

    /**
     * Gets the currently active map.
     *
     * @return the currently active map
     */
    @NotNull MapInfo getMap();

    /**
     * Adds some MapInfo to the maps held by this session.
     *
     * @param map the map to add
     */
    void addMap(@NotNull MapInfo map);

    /**
     * Determines if this session contains a map with the given id.
     *
     * @param id the id to check for
     * @return true if a map with this id exists, false otherwise
     */
    boolean containsMap(@NotNull Key id);

    /**
     * Removes a map that has the given id. If no map with the given id exists, this method will do nothing.
     *
     * @param id the id of the map to remove
     */
    void removeMap(@NotNull Key id);

    /**
     * Returns an unmodifiable view of the maps managed by this EditorSession.
     *
     * @return an unmodifiable view of the maps managed by this EditorSession
     */
    @UnmodifiableView @NotNull Map<Key, MapInfo> mapView();

    /**
     * Sets the current map to the managed map with the given id. If no map with this id exists, an
     * {@link IllegalArgumentException} will be thrown. This method must (typically) refresh the map render, too.
     *
     * @param id the id of the map to make current
     */
    void setCurrent(@NotNull Key id);

    /**
     * Reloads all maps from disk, overwriting all existing maps.
     */
    void loadMapsFromDisk();

    /**
     * Saves all maps to disk, overwriting any changes there.
     */
    void saveMapsToDisk();

    /**
     * Sets the last room edited in this session. Used to autofill name information, when necessary.
     *
     * @param room the last room edited
     */
    void setLastRoom(@Nullable RoomInfo room);

    /**
     * Sets the last door edited in this session. Used to autofill name information, when necessary.
     *
     * @param door the last door edited
     */
    void setLastDoor(@Nullable DoorInfo door);

    /**
     * Sets the id of the last spawnrule edited in this session. Used to autofill name information, when necessary.
     *
     * @param spawnruleId the id of the last spawnrule
     */
    void setLastSpawnrule(@Nullable Key spawnruleId);

    /**
     * Refreshes the render for all rooms.
     */
    void refreshRooms();

    /**
     * Refreshes the render for all doors.
     */
    void refreshDoors();

    /**
     * Refreshes the render for all windows.
     */
    void refreshWindows();

    /**
     * Refreshes the render for all spawnpoints.
     */
    void refreshSpawnpoints();

    /**
     * Refreshes the render for all shops.
     */
    void refreshShops();

    /**
     * Gets the last edited room; used for autofilling name information.
     *
     * @return the last edited room
     */
    @Nullable RoomInfo lastRoom();

    /**
     * Gets the last edited door; used for autofilling name information.
     *
     * @return the last edited door
     */
    @Nullable DoorInfo lastDoor();

    /**
     * Gets the id of the last edited spawnrule; used for autofilling name information.
     *
     * @return the id of the last edited spawnrule
     */
    @Nullable Key lastSpawnrule();
}
