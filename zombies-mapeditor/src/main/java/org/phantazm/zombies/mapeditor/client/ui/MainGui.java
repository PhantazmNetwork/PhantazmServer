package org.phantazm.zombies.mapeditor.client.ui;

import com.github.steanky.ethylene.core.collection.LinkedConfigNode;
import io.github.cottonmc.cotton.gui.client.CottonClientScreen;
import io.github.cottonmc.cotton.gui.widget.*;
import net.kyori.adventure.key.Key;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.phantazm.commons.Namespaces;
import org.phantazm.zombies.map.LeaderboardInfo;
import org.phantazm.zombies.map.MapInfo;
import org.phantazm.zombies.map.MapSettingsInfo;
import org.phantazm.zombies.map.PlayerCoinsInfo;
import org.phantazm.zombies.mapeditor.client.EditorSession;
import org.phantazm.zombies.mapeditor.client.Identifiers;
import org.phantazm.zombies.mapeditor.client.TextPredicates;
import org.phantazm.zombies.mapeditor.client.TranslationKeys;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * The main configuration GUI for the Zombies Map Editor.
 */
public class MainGui extends SimplePanelGui {
    /**
     * Creates a new instance of the main GUI, using the provided {@link EditorSession}.
     *
     * @param session the current session
     */
    @SuppressWarnings("PatternValidation")
    public MainGui(@NotNull EditorSession session) {
        super(220, 150);

        Objects.requireNonNull(session, "session");

        //pre-initialization
        List<Key> mapNames = session.mapView().values().stream().map(map -> map.settings().id()).toList();

        //create GUI components...
        WSprite icon = new WSprite(Identifiers.ICON);
        WToggleButton mapeditorToggle = new WToggleButton();
        WText currentMap = new WText(Text.of(StringUtils.EMPTY));
        WTextField mapNameBox = new WTextField();
        WText feedback = new WText(Text.translatable(StringUtils.EMPTY));
        WButton newMap = new WButton(Text.translatable(TranslationKeys.GUI_MAPEDITOR_NEW_MAP));
        WButton deleteMap = new WButton(Text.translatable(TranslationKeys.GUI_MAPEDITOR_DELETE_MAP));
        WListPanel<Key, WButton> listPanel = new WListPanel<>(mapNames, WButton::new, (k, w) -> {
            w.setLabel(Text.of(k.value()));
            w.setSize(3, 1);

            w.setOnClick(() -> {
                session.setCurrent(k);
                updateCurrentMap(session, currentMap);
            });
        });
        //TODO: implement display settings at a later date
        //WButton displaySettings = new WButton(Text.translatable(TranslationKeys.GUI_MAPEDITOR_DISPLAY_SETTINGS));
        WButton save = new WButton(Text.translatable(TranslationKeys.GUI_MAPEDITOR_SAVE));
        WButton load = new WButton(Text.translatable(TranslationKeys.GUI_MAPEDITOR_LOAD));

        //...add them to root
        gridPanelRoot.add(icon, 0, 0, 2, 2);
        gridPanelRoot.add(mapeditorToggle, 3, 0);
        gridPanelRoot.add(currentMap, 3, 1, 8, 1);
        gridPanelRoot.add(mapNameBox, 0, 2, 6, 1);
        gridPanelRoot.add(feedback, 0, 3, 6, 1);
        gridPanelRoot.add(newMap, 0, 4, 6, 1);
        gridPanelRoot.add(deleteMap, 0, 5, 6, 1);
        gridPanelRoot.add(listPanel, 6, 2, 6, 6);
        //gridPanelRoot.add(displaySettings, 0, 6, 6, 1);
        gridPanelRoot.add(save, 0, 7, 3, 1);
        gridPanelRoot.add(load, 3, 7, 3, 1);

        //generic configuration
        updateMapeditorToggle(mapeditorToggle, session.isEnabled());
        updateCurrentMap(session, currentMap);

        mapNameBox.setMaxLength(512);
        mapNameBox.setTextPredicate(TextPredicates.validKeyPredicate());

        //events
        mapeditorToggle.setOnToggle(toggled -> {
            session.setEnabled(toggled);
            updateMapeditorToggle(mapeditorToggle, toggled);
        });

        newMap.setOnClick(() -> {
            String name = mapNameBox.getText();
            if (name.isEmpty()) {
                feedback.setText(Text.translatable(TranslationKeys.GUI_MAPEDITOR_FEEDBACK_EMPTY_MAP_NAME));
                return;
            }

            if (!session.hasSelection()) {
                feedback.setText(Text.translatable(TranslationKeys.GUI_MAPEDITOR_FEEDBACK_NO_SELECTION));
                return;
            }

            Key mapKey = Key.key(Namespaces.PHANTAZM, name);
            if (session.containsMap(mapKey)) {
                feedback.setText(Text.translatable(TranslationKeys.GUI_MAPEDITOR_FEEDBACK_MAP_NAME_EXISTS));
                return;
            }

            session.addMap(
                    new MapInfo(new MapSettingsInfo(mapKey, session.getFirstSelection()), PlayerCoinsInfo.DEFAULT,
                            new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>(),
                            new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), LeaderboardInfo.DEFAULT,
                            new LinkedConfigNode(0), new LinkedConfigNode()));
            session.setCurrent(mapKey);

            refreshMainGui(session);
        });

        deleteMap.setOnClick(() -> {
            if (requireMap(session, feedback)) {
                MinecraftClient.getInstance().setScreen(new CottonClientScreen(
                        new ConfirmationGui(Text.translatable(TranslationKeys.GUI_MAPEDITOR_DELETE_MAP_QUERY), () -> {
                            session.removeMap(session.getMap().settings().id());
                            refreshMainGui(session);
                        }, () -> refreshMainGui(session))));
            }
        });

        save.setOnClick(session::saveMapsToDisk);
        load.setOnClick(() -> {
            MinecraftClient.getInstance().setScreen(new CottonClientScreen(
                    new ConfirmationGui(Text.translatable(TranslationKeys.GUI_MAPEDITOR_OVERWRITE_MAP_QUERY),
                            () -> refreshMaps(session), () -> refreshMainGui(session))));
        });
    }

    private boolean requireMap(EditorSession session, WText feedback) {
        if (!session.hasMap()) {
            feedback.setText(Text.translatable(TranslationKeys.GUI_MAPEDITOR_FEEDBACK_NO_ACTIVE_MAP));
            return false;
        }

        return true;
    }

    private void updateMapeditorToggle(WToggleButton mapeditorToggle, boolean enabled) {
        mapeditorToggle.setToggle(enabled);
        mapeditorToggle.setLabel(enabled
                                 ? Text.translatable(TranslationKeys.GUI_MAPEDITOR_ENABLED)
                                 : Text.translatable(TranslationKeys.GUI_MAPEDITOR_DISABLED));
    }

    private void updateCurrentMap(EditorSession session, WText currentMap) {
        currentMap.setText(session.hasMap() ? Text.translatable(TranslationKeys.GUI_MAPEDITOR_CURRENT_MAP,
                session.getMap().settings().id().value()) : Text.translatable(TranslationKeys.GUI_MAPEDITOR_NO_MAP));
    }

    private void refreshMainGui(EditorSession session) {
        ScreenUtils.closeCurrentScreen();
        MinecraftClient.getInstance().setScreen(new CottonClientScreen(new MainGui(session)));
    }

    private void refreshMaps(EditorSession session) {
        session.loadMapsFromDisk();
        refreshMainGui(session);
    }

}
