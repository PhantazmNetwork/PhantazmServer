package com.github.phantazmnetwork.zombies.mapeditor.client;

import net.minecraft.text.TranslatableText;

/**
 * Contains resource keys for translatable text.
 * @see TranslatableText
 */
public final class TranslationKeys {
    private TranslationKeys() { throw new UnsupportedOperationException(); }

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

    //TODO: remove when adding display settings
    //public static final String GUI_MAPEDITOR_DISPLAY_SETTINGS = "gui.mapeditor.display_settings";
}