package com.github.phantazmnetwork.zombies.mapeditor.client.ui;

import com.github.phantazmnetwork.commons.Namespaces;
import com.github.phantazmnetwork.zombies.map.RoomInfo;
import com.github.phantazmnetwork.zombies.map.ZombiesMap;
import com.github.phantazmnetwork.zombies.mapeditor.client.MapeditorSession;
import com.github.phantazmnetwork.zombies.mapeditor.client.TextPredicates;
import com.github.phantazmnetwork.zombies.mapeditor.client.TranslationKeys;
import com.github.phantazmnetwork.zombies.mapeditor.client.render.RenderUtils;
import io.github.cottonmc.cotton.gui.client.LightweightGuiDescription;
import io.github.cottonmc.cotton.gui.widget.WButton;
import io.github.cottonmc.cotton.gui.widget.WGridPanel;
import io.github.cottonmc.cotton.gui.widget.WTextField;
import io.github.cottonmc.cotton.gui.widget.data.Insets;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.minecraft.text.TranslatableText;

import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

public class NewRoomGui extends LightweightGuiDescription {
    @SuppressWarnings("PatternValidation")
    public NewRoomGui(@NotNull MapeditorSession session) {
        WGridPanel root = new WGridPanel();
        setRootPanel(root);

        root.setSize(100, 150);
        root.setInsets(Insets.ROOT_PANEL);

        WTextField roomId = new WTextField();
        WButton add = new WButton(new TranslatableText(TranslationKeys.GUI_MAPEDITOR_ADD));

        RoomInfo lastRoom = session.lastRoom();
        roomId.setMaxLength(512);
        roomId.setText(lastRoom == null ? StringUtils.EMPTY : lastRoom.id().value());
        roomId.setTextPredicate(TextPredicates.validKeyPredicate());

        root.add(roomId, 0, 0, 5, 1);
        root.add(add, 0, 2, 5, 1);

        ZombiesMap currentMap = session.getMap();
        RegionInfo selected = RenderUtils.regionFromPoints(session.getFirstSelection(), session.getSecondSelection(),
                currentMap.info().origin());
        add.setOnClick(() -> {
            String value = roomId.getText();
            if(value.isEmpty()) {
                return;
            }

            Key roomKey = Key.key(Namespaces.PHANTAZM, roomId.getText());
            for(RoomInfo roomInfo : currentMap.rooms()) {
                if(roomInfo.id().equals(roomKey)) {
                    roomInfo.regions().add(selected);
                    session.refreshRooms();
                    session.setLastRoom(roomInfo);
                    ScreenUtils.closeCurrentScreen();
                    return;
                }
            }

            ArrayList<RegionInfo> bounds = new ArrayList<>(1);
            bounds.add(selected);

            RoomInfo newRoom = new RoomInfo(roomKey, Component.text(roomKey.value()), bounds);
            session.setLastRoom(newRoom);
            currentMap.rooms().add(newRoom);
            session.refreshRooms();
            ScreenUtils.closeCurrentScreen();
        });
    }
}
