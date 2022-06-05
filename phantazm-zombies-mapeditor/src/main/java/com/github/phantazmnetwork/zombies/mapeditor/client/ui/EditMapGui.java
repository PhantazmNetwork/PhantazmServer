package com.github.phantazmnetwork.zombies.mapeditor.client.ui;

import com.github.phantazmnetwork.commons.vector.Vec3I;
import com.github.phantazmnetwork.zombies.map.MapInfo;
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
import net.minecraft.text.*;
import org.jetbrains.annotations.NotNull;

public class EditMapGui extends LightweightGuiDescription {
    public EditMapGui(@NotNull MapeditorSession session, @NotNull ZombiesMap map) {
        WGridPanel root = new WGridPanel();
        setRootPanel(root);

        root.setSize(200, 150);
        root.setInsets(Insets.ROOT_PANEL);

        MapInfo info = map.info();

        WText mapNameText = new WText(new TranslatableText(TranslationKeys.GUI_MAPEDITOR_CREATING_MAP, info.id()
                .value()));
        WText origin = new WText(new TranslatableText(TranslationKeys.GUI_MAPEDITOR_ORIGIN, formatVector(info
                .origin())));
        WText mapDisplayNameText = new WText(new TranslatableText(TranslationKeys.GUI_MAPEDITOR_DISPLAY_NAME));
        WText mapDisplayItemText = new WText(new TranslatableText(TranslationKeys.GUI_MAPEDITOR_DISPLAY_ITEM_NBT));
        WTextField displayNameBox = new WTextField();
        WTextField displayItemTagBox = new WTextField();
        WButton save = new WButton(new TranslatableText(TranslationKeys.GUI_MAPEDITOR_SAVE));

        root.add(mapNameText, 0, 0, 10, 1);
        root.add(origin, 8, 0, 6, 1);
        root.add(mapDisplayNameText, 7, 1, 7, 1);
        root.add(mapDisplayItemText, 7, 3, 7, 1);
        root.add(displayNameBox, 0, 1, 6, 1);
        root.add(displayItemTagBox, 0, 3, 6, 1);
        root.add(save, 0, 6, 5, 1);

        //configuration
        displayNameBox.setMaxLength(512);
        displayNameBox.setText(info.displayName().toString());
        displayItemTagBox.setMaxLength(65535);
        displayItemTagBox.setText(info.displayItemTag());

        //events
        save.setOnClick(() -> {
            Key id = info.id();
            ZombiesMap newMap = new ZombiesMap(new MapInfo(id, displayNameBox.getText(), displayItemTagBox.getText(),
                    info.origin(), info.roomsPath(), info.doorsPath(), info.shopsPath(), info.windowsPath(), info
                    .roundsPath()), map.rooms(), map.doors(), map.shops(), map.windows(), map.rounds());
            session.removeMap(id);
            session.addMap(id, newMap);
            session.setCurrent(id);

            ScreenUtils.closeCurrentScreen();
        });
    }

    private static String formatVector(Vec3I vec) {
        return "(" + vec.getX() + ", " + vec.getY() + ", " + vec.getZ() + ")";
    }
}
