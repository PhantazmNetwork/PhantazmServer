package com.github.phantazmnetwork.zombies.mapeditor.client.ui;

import com.github.phantazmnetwork.commons.Namespaces;
import com.github.phantazmnetwork.commons.vector.Vec3I;
import com.github.phantazmnetwork.zombies.map.MapInfo;
import com.github.phantazmnetwork.zombies.map.MapProcessors;
import com.github.phantazmnetwork.zombies.map.ZombiesMap;
import com.github.phantazmnetwork.zombies.mapeditor.client.MapeditorSession;
import com.github.phantazmnetwork.zombies.mapeditor.client.TranslationKeys;
import io.github.cottonmc.cotton.gui.client.LightweightGuiDescription;
import io.github.cottonmc.cotton.gui.widget.WButton;
import io.github.cottonmc.cotton.gui.widget.WGridPanel;
import io.github.cottonmc.cotton.gui.widget.WText;
import io.github.cottonmc.cotton.gui.widget.WTextField;
import io.github.cottonmc.cotton.gui.widget.data.Insets;
import net.kyori.adventure.key.Key;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.*;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

public class NewMapGui extends LightweightGuiDescription {
    private static final String DEFAULT_ITEM_NBT = "{id:\"stone\",Count:1,tag:{Name:\"Map\"}}";

    public NewMapGui(@NotNull MapeditorSession session, @NotNull String mapName) {
        WGridPanel root = new WGridPanel();
        setRootPanel(root);

        root.setSize(200, 150);
        root.setInsets(Insets.ROOT_PANEL);

        Vec3I originVector = session.getFirstSelection();

        WText mapNameText = new WText(new TranslatableText(TranslationKeys.GUI_MAPEDITOR_CREATING_MAP, mapName));
        WText origin = new WText(new TranslatableText(TranslationKeys.GUI_MAPEDITOR_ORIGIN, formatVector(originVector)));
        WText mapDisplayNameText = new WText(new TranslatableText(TranslationKeys.GUI_MAPEDITOR_DISPLAY_NAME));
        WText mapDisplayItemText = new WText(new TranslatableText(TranslationKeys.GUI_MAPEDITOR_DISPLAY_ITEM_NBT));
        WTextField displayNameBox = new WTextField();
        WTextField displayItemNBTBox = new WTextField();
        WButton create = new WButton(new TranslatableText(TranslationKeys.GUI_MAPEDITOR_CREATE));

        root.add(mapNameText, 0, 0, 10, 1);
        root.add(origin, 8, 0, 6, 1);
        root.add(mapDisplayNameText, 7, 1, 6, 1);
        root.add(mapDisplayItemText, 7, 3, 6, 1);
        root.add(displayNameBox, 0, 1, 6, 1);
        root.add(displayItemNBTBox, 0, 3, 6, 1);
        root.add(create, 0, 6, 5, 1);

        //configuration
        displayNameBox.setMaxLength(512);
        displayItemNBTBox.setMaxLength(65535);
        displayItemNBTBox.setText(DEFAULT_ITEM_NBT);

        //events
        create.setOnClick(() -> {
            Key key = Key.key(Namespaces.PHANTAZM, mapName);
            MapInfo info = new MapInfo(key, displayNameBox.getText(), displayItemNBTBox.getText(), session.getFirstSelection(),
                    MapProcessors.DEFAULT_ROOMS_PATH, MapProcessors.DEFAULT_DOORS_PATH, MapProcessors
                    .DEFAULT_SHOPS_PATH, MapProcessors.DEFAULT_WINDOWS_PATH, MapProcessors.DEFAULT_ROUNDS_PATH);
            ZombiesMap newMap = new ZombiesMap(info, new ArrayList<>(), new ArrayList<>(), new ArrayList<>(),
                    new ArrayList<>(), new ArrayList<>());
            session.addMap(key, newMap);
            session.setCurrent(key);
            MinecraftClient.getInstance().setScreen(null);
        });
    }

    private static String formatVector(Vec3I vec) {
        return "(" + vec.getX() + ", " + vec.getY() + ", " + vec.getZ() + ")";
    }
}
