package com.github.phantazmnetwork.zombies.mapeditor.client.ui;

import com.github.phantazmnetwork.commons.Namespaces;
import com.github.phantazmnetwork.zombies.map.DoorInfo;
import com.github.phantazmnetwork.zombies.map.RegionInfo;
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
import net.minecraft.text.TranslatableText;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

public class NewDoorGui extends LightweightGuiDescription {
    @SuppressWarnings("PatternValidation")
    public NewDoorGui(@NotNull MapeditorSession session) {
        WGridPanel root = new WGridPanel();
        setRootPanel(root);

        root.setSize(100, 150);
        root.setInsets(Insets.ROOT_PANEL);

        WTextField doorId = new WTextField();
        WButton add = new WButton(new TranslatableText(TranslationKeys.GUI_MAPEDITOR_ADD));

        DoorInfo lastDoor = session.lastDoor();
        doorId.setMaxLength(512);
        doorId.setText(lastDoor == null ? StringUtils.EMPTY : lastDoor.id().value());
        doorId.setTextPredicate(TextPredicates.validKeyPredicate());

        root.add(doorId, 0, 0, 5, 1);
        root.add(add, 0, 2, 5, 1);

        ZombiesMap currentMap = session.getMap();
        RegionInfo selected = RenderUtils.regionFromPoints(session.getFirstSelection(), session.getSecondSelection(),
                currentMap.info().origin());
        add.setOnClick(() -> {
            String value = doorId.getText();
            if(value.isEmpty()) {
                return;
            }

            Key doorKey = Key.key(Namespaces.PHANTAZM, doorId.getText());
            for(DoorInfo doorInfo : currentMap.doors()) {
                if(doorInfo.id().equals(doorKey)) {
                    doorInfo.regions().add(selected);
                    session.refreshDoors();
                    session.setLastDoor(doorInfo);
                    ScreenUtils.closeCurrentScreen();
                    return;
                }
            }

            ArrayList<RegionInfo> bounds = new ArrayList<>();
            bounds.add(selected);

            DoorInfo newDoor = new DoorInfo(doorKey, 0, new ArrayList<>(), bounds);
            session.setLastDoor(newDoor);
            currentMap.doors().add(newDoor);
            session.refreshDoors();
            ScreenUtils.closeCurrentScreen();
        });
    }
}

