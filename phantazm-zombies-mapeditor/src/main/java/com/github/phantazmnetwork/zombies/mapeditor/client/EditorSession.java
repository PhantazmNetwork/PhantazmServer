package com.github.phantazmnetwork.zombies.mapeditor.client;

import com.github.phantazmnetwork.commons.vector.Region3I;
import com.github.phantazmnetwork.commons.vector.Vec3I;
import com.github.phantazmnetwork.zombies.map.DoorInfo;
import com.github.phantazmnetwork.zombies.map.RoomInfo;
import com.github.phantazmnetwork.zombies.map.MapInfo;
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
     * @param player the player
     * @param world the world the player is in
     * @param hand the {@link Hand} used for this interaction
     * @param blockHitResult the {@link BlockHitResult} for this interaction
     * @return the desired {@link ActionResult}
     */
    @NotNull ActionResult handleBlockUse(@NotNull PlayerEntity player, @NotNull World world, @NotNull Hand hand,
                                         @NotNull BlockHitResult blockHitResult);

    /**
     * "Enables" or "disables" the mapeditor. When the editor is enabled, map objects will be visually rendered, and the
     * EditorSession API can be used to modify or create maps. When the editor is disabled, modifications can be made,
     * but player interaction with this session is disabled, and nothing is rendered visually.
     * @param enabled true to enable the editor, false otherwise
     */
    void setEnabled(boolean enabled);

    /**
     * Queries if the editor is enabled or not.
     * @return true if the editor is enabled, false otherwise
     */
    boolean isEnabled();

    /**
     * Checks if the editor currently has a selection. The editor has a selection if it is enabled and the player has
     * right-clicked at least one block with a stick.
     * @return true if a selection has been made, false otherwise
     */
    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    boolean hasSelection();

    /**
     * Gets the first selected block, which is the <i>last</i> block the player right-clicked. If there is no selection,
     * an {@link IllegalArgumentException} will be thrown.
     * @return a {@link Vec3I} representing the last block right-clicked in world coordinate space
     * @throws IllegalStateException if there is no selection
     * @see EditorSession#hasSelection()
     */
    @NotNull Vec3I getFirstSelection();

    /**
     * Gets the second selected block, which is the <i>second-last</i> block the player right-clicked. If there is no
     * selection, an {@link IllegalArgumentException} will be thrown.
     * @return a {@link Vec3I} representing the second-last block right-clicked in world coordinate space
     * @throws IllegalStateException if there is no selection
     * @see EditorSession#hasSelection()
     */
    @NotNull Vec3I getSecondSelection();

    @NotNull Region3I getSelection();

    boolean hasMap();

    @NotNull MapInfo getMap();

    void addMap(@NotNull Key id, @NotNull MapInfo map);

    boolean containsMap(@NotNull Key id);

    void removeMap(@NotNull Key id);

    @UnmodifiableView @NotNull Map<Key, MapInfo> mapView();

    void setCurrent(@NotNull Key id);

    void loadMapsFromDisk();

    void saveMaps();

    void setLastRoom(@Nullable RoomInfo room);

    void setLastDoor(@Nullable DoorInfo door);

    void setLastSpawnrule(@Nullable Key spawnruleId);

    void refreshRooms();

    void refreshDoors();

    void refreshWindows();

    void refreshSpawnpoints();

    void refreshShops();

    @Nullable RoomInfo lastRoom();

    @Nullable DoorInfo lastDoor();

    @Nullable Key lastSpawnrule();
}