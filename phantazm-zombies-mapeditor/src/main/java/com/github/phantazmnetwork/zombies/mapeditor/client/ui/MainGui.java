package com.github.phantazmnetwork.zombies.mapeditor.client.ui;

import com.github.phantazmnetwork.commons.Namespaces;
import com.github.phantazmnetwork.zombies.map.MapInfo;
import com.github.phantazmnetwork.zombies.map.MapProcessors;
import com.github.phantazmnetwork.zombies.map.ZombiesMap;
import com.github.phantazmnetwork.zombies.mapeditor.client.Identifiers;
import com.github.phantazmnetwork.zombies.mapeditor.client.MapeditorSession;
import com.github.phantazmnetwork.zombies.mapeditor.client.TranslationKeys;
import io.github.cottonmc.cotton.gui.client.LightweightGuiDescription;
import io.github.cottonmc.cotton.gui.widget.*;
import io.github.cottonmc.cotton.gui.widget.data.Insets;
import net.kyori.adventure.key.InvalidKeyException;
import net.kyori.adventure.key.Key;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.LiteralText;
import net.minecraft.text.TranslatableText;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class MainGui extends LightweightGuiDescription {
    private static final String DEFAULT_ITEM_NBT = "{id:\"stone\",Count:1,tag:{Name:\"New Map\"}}";

    @SuppressWarnings("PatternValidation")
    public MainGui(@NotNull MapeditorSession session) {
        WGridPanel root = new WGridPanel();
        setRootPanel(root);

        root.setSize(220, 150);
        root.setInsets(Insets.ROOT_PANEL);

        //pre-initialization
        List<Key> mapNames = session.mapView().values().stream().map(map -> map.info().id()).toList();

        //create GUI components...
        WSprite icon = new WSprite(Identifiers.ICON);
        WToggleButton mapeditorToggle = new WToggleButton();
        WText currentMap = new WText(new LiteralText(StringUtils.EMPTY));
        WTextField mapNameBox = new WTextField();
        WText feedback = new WText(new LiteralText(StringUtils.EMPTY));
        WButton newMap = new WButton(new TranslatableText(TranslationKeys.GUI_MAPEDITOR_NEW_MAP));
        WButton editMap = new WButton(new TranslatableText(TranslationKeys.GUI_MAPEDITOR_EDIT_MAP));
        WButton deleteMap = new WButton(new TranslatableText(TranslationKeys.GUI_MAPEDITOR_DELETE_MAP));
        WListPanel<Key, WButton> listPanel = new WListPanel<>(mapNames, WButton::new, (k, w) -> {
            w.setLabel(new LiteralText(k.value()));
            w.setSize(3, 1);

            w.setOnClick(() -> {
                session.setCurrent(k);
                updateCurrentMap(session, currentMap);
            });
        });
        WButton displaySettings = new WButton(new TranslatableText(TranslationKeys.GUI_MAPEDITOR_DISPLAY_SETTINGS));

        //...add them to root
        root.add(icon, 0, 0, 2, 2);
        root.add(mapeditorToggle, 3, 0);
        root.add(currentMap, 3, 1, 8, 1);
        root.add(mapNameBox, 0, 2, 6, 1);
        root.add(feedback, 0, 3, 6, 1);
        root.add(newMap, 0, 4, 6, 1);
        root.add(editMap, 0, 5, 6, 1);
        root.add(deleteMap, 0, 6, 6, 1);
        root.add(listPanel, 6, 2, 6, 6);
        root.add(displaySettings, 0, 7, 6, 1);

        //generic configuration
        updateMapeditorToggle(mapeditorToggle, session.isEnabled());
        updateCurrentMap(session, currentMap);

        mapNameBox.setMaxLength(512);
        mapNameBox.setTextPredicate(string -> {
            try {
                Key.key(Namespaces.PHANTAZM, string);
                return true;
            }
            catch (InvalidKeyException ignored) {
                return false;
            }
        });

        //events
        mapeditorToggle.setOnToggle(toggled -> {
            session.setEnabled(toggled);
            updateMapeditorToggle(mapeditorToggle, toggled);
        });

        newMap.setOnClick(() -> {
            String name = mapNameBox.getText();
            if(name.isEmpty()) {
                feedback.setText(new TranslatableText(TranslationKeys.GUI_MAPEDITOR_FEEDBACK_EMPTY_MAP_NAME));
                return;
            }

            if(!session.hasSelection()) {
                feedback.setText(new TranslatableText(TranslationKeys.GUI_MAPEDITOR_FEEDBACK_NO_SELECTION));
                return;
            }

            Key mapKey = Key.key(Namespaces.PHANTAZM, name);
            if(session.containsMap(mapKey)) {
                feedback.setText(new TranslatableText(TranslationKeys.GUI_MAPEDITOR_FEEDBACK_MAP_NAME_EXISTS));
                return;
            }

            ZombiesMap newMapData = new ZombiesMap(new MapInfo(mapKey, StringUtils.EMPTY, DEFAULT_ITEM_NBT, session
                    .getFirstSelection(), MapProcessors.DEFAULT_ROOMS_PATH, MapProcessors.DEFAULT_DOORS_PATH,
                    MapProcessors.DEFAULT_SHOPS_PATH, MapProcessors.DEFAULT_WINDOWS_PATH, MapProcessors
                    .DEFAULT_ROUNDS_PATH), new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>(),
                    new ArrayList<>());

            MinecraftClient.getInstance().setScreen(new MapeditorScreen(new EditMapGui(session, newMapData)));
        });

        editMap.setOnClick(() -> {
            if(hasMap(session, feedback)) {
                MinecraftClient.getInstance().setScreen(new MapeditorScreen(new EditMapGui(session, session.getMap())));
            }
        });

        deleteMap.setOnClick(() -> {
            if(hasMap(session, feedback)) {
                MinecraftClient.getInstance().setScreen(new MapeditorScreen(new ConfirmationGui(new TranslatableText(
                        TranslationKeys.GUI_MAPEDITOR_DELETE_MAP_QUERY), () -> session.removeMap(session.getMap().info()
                        .id()))));
            }
        });
    }

    private boolean hasMap(MapeditorSession session, WText feedback) {
        if(!session.hasMap()) {
            feedback.setText(new TranslatableText(TranslationKeys.GUI_MAPEDITOR_FEEDBACK_NO_ACTIVE_MAP));
            return false;
        }

        return true;
    }

    private void updateMapeditorToggle(WToggleButton mapeditorToggle, boolean enabled) {
        mapeditorToggle.setToggle(enabled);
        mapeditorToggle.setLabel(enabled ? new TranslatableText(TranslationKeys.GUI_MAPEDITOR_ENABLED) :
                new TranslatableText(TranslationKeys.GUI_MAPEDITOR_DISABLED));
    }

    private void updateCurrentMap(MapeditorSession session, WText currentMap) {
        currentMap.setText(session.hasMap() ? new TranslatableText(TranslationKeys.GUI_MAPEDITOR_CURRENT_MAP, session
                .getMap().info().id().value()) : new TranslatableText(TranslationKeys.GUI_MAPEDITOR_NO_MAP));
    }
}