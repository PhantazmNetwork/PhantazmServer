package org.phantazm.zombies.mapeditor.client;

import net.minecraft.text.TranslatableTextContent;

/**
 * Contains resource keys for translatable text.
 *
 * @see TranslatableTextContent
 */
public final class TranslationKeys {
    /**
     * Keybinding category for all mapeditor keybindings.
     */
    public static final String CATEGORY_MAPEDITOR_ALL = "category.mapeditor.all";
    /**
     * Configuration keybinding.
     */
    public static final String KEY_MAPEDITOR_CONFIG = "key.mapeditor.config";
    /**
     * Map object creation keybinding.
     */
    public static final String KEY_MAPEDITOR_CREATE = "key.mapeditor.create";
    /**
     * Shown in the main configuration GUI when the mapeditor is enabled.
     */
    public static final String GUI_MAPEDITOR_ENABLED = "gui.mapeditor.enabled";
    /**
     * Shown in the main configuration GUI when the mapeditor is disabled.
     */
    public static final String GUI_MAPEDITOR_DISABLED = "gui.mapeditor.disabled";
    /**
     * Shown on the button used to create a new map.
     */
    public static final String GUI_MAPEDITOR_NEW_MAP = "gui.mapeditor.new_map";
    /**
     * Shown on the button used to create a new room.
     */
    public static final String GUI_MAPEDITOR_NEW_ROOM = "gui.mapeditor.new_room";
    /**
     * Shown on the button used to create a new door.
     */
    public static final String GUI_MAPEDITOR_NEW_DOOR = "gui.mapeditor.new_door";
    /**
     * Shown on the button used to create a new window.
     */
    public static final String GUI_MAPEDITOR_NEW_WINDOW = "gui.mapeditor.new_window";
    /**
     * Shown on the button used to create a new shop.
     */
    public static final String GUI_MAPEDITOR_NEW_SHOP = "gui.mapeditor.new_shop";
    /**
     * Shown on the button used to create a new spawnpoint.
     */
    public static final String GUI_MAPEDITOR_NEW_SPAWNPOINT = "gui.mapeditor.new_spawnpoint";
    /**
     * Shown on the save button in the main mapeditor GUI.
     */
    public static final String GUI_MAPEDITOR_SAVE = "gui.mapeditor.save";
    /**
     * Shown on the load button in the main mapeditor GUI.
     */
    public static final String GUI_MAPEDITOR_LOAD = "gui.mapeditor.load";
    /**
     * Shown on the delete button in the main mapeditor GUI.
     */
    public static final String GUI_MAPEDITOR_DELETE_MAP = "gui.mapeditor.delete_map";
    /**
     * Feedback shown when the user tries to add a new object without an active map.
     */
    public static final String GUI_MAPEDITOR_FEEDBACK_NO_ACTIVE_MAP = "gui.mapeditor.feedback.no_active_map";
    /**
     * Feedback shown when the user tries to create a map with an empty string for a name.
     */
    public static final String GUI_MAPEDITOR_FEEDBACK_EMPTY_MAP_NAME = "gui.mapeditor.feedback.empty_map_name";
    /**
     * Feedback shown when the user tries to create a map without any selection indicating where the origin should be.
     */
    public static final String GUI_MAPEDITOR_FEEDBACK_NO_SELECTION = "gui.mapeditor.feedback.no_selection";
    /**
     * Feedback shown when the user tries to create a map with a name that already exists.
     */
    public static final String GUI_MAPEDITOR_FEEDBACK_MAP_NAME_EXISTS = "gui.mapeditor.feedback.map_name_exists";
    /**
     * Shown on confirmation buttons.
     */
    public static final String GUI_MAPEDITOR_YES = "gui.mapeditor.yes";
    /**
     * Shown on denial buttons.
     */
    public static final String GUI_MAPEDITOR_NO = "gui.mapeditor.no";
    /**
     * Shown on buttons which add a new object to the map.
     */
    public static final String GUI_MAPEDITOR_ADD = "gui.mapeditor.add";
    /**
     * Shown to indicate the current map name. Takes a single argument (the map name).
     */
    public static final String GUI_MAPEDITOR_CURRENT_MAP = "gui.mapeditor.current_map";
    /**
     * Shown in the main mapeditor GUI when there is no active map.
     */
    public static final String GUI_MAPEDITOR_NO_MAP = "gui.mapeditor.no_map";
    /**
     * Shown in the confirmation dialog when attempting to delete a map, asking the player if they really want to do so.
     */
    public static final String GUI_MAPEDITOR_DELETE_MAP_QUERY = "gui.mapeditor.delete_map_query";
    /**
     * Shown in the confirmation dialog when attempting to load maps and thereby overwriting current changes to maps,
     * asking the player if they really want to do so.
     */
    public static final String GUI_MAPEDITOR_OVERWRITE_MAP_QUERY = "gui.mapeditor.overwrite_map_query";
    /**
     * The label shown on the toggle for controlling spawnpoint linking to windows.
     */
    public static final String GUI_MAPEDITOR_LINK_WINDOW = "gui.mapeditor.link_window";

    /**
     * The label shown when making an explicit window linking.
     */
    public static final String GUI_MAPEDITOR_EXPLICIT_WINDOW_LINK = "gui.mapeditor.explicit_window_link";

    /**
     * The label shown when making an inferred (distance-based) window linking.
     */
    public static final String GUI_MAPEDITOR_NEAREST_WINDOW_LINK = "gui.mapeditor.nearest_window_link";

    /**
     * The text displayed in the top-level corner of the screen while a map is active and the player is looking at a
     * block.
     */
    public static final String GUI_MAPEDITOR_COORDINATE_DISPLAY = "gui.mapeditor.coordinate_display";

    public static final String CHAT_MAPEDITOR_MAPDATA_VERSION_SYNC_SYNCED = "chat.mapeditor.mapdata_version_sync" +
            ".synced";

    public static final String CHAT_MAPEDITOR_MAPDATA_VERSION_SYNC_NOT_SYNCED = "chat.mapeditor.mapdata_version_sync" +
            ".not_synced";

    private TranslationKeys() {
        throw new UnsupportedOperationException();
    }

    //TODO: remove when adding display settings
    //public static final String GUI_MAPEDITOR_DISPLAY_SETTINGS = "gui.mapeditor.display_settings";
}
