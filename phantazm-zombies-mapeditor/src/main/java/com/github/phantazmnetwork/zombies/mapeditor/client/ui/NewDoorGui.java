package com.github.phantazmnetwork.zombies.mapeditor.client.ui;

import com.github.phantazmnetwork.commons.LogicUtils;
import com.github.phantazmnetwork.commons.Namespaces;
import com.github.phantazmnetwork.commons.vector.Region3I;
import com.github.phantazmnetwork.zombies.map.DoorInfo;
import com.github.phantazmnetwork.zombies.map.ZombiesMap;
import com.github.phantazmnetwork.zombies.mapeditor.client.EditorSession;
import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * GUI class used to create {@link DoorInfo}.
 */
public class NewDoorGui extends NamedObjectGui {
    /**
     * Constructs a new instance of this GUI. The user may create a new door, or add regions to an existing door.
     * @param session the current {@link EditorSession}
     */
    @SuppressWarnings("PatternValidation")
    public NewDoorGui(@NotNull EditorSession session) {
        super(LogicUtils.nullCoalesce(session.lastDoor(), door -> door.id().value()));

        Objects.requireNonNull(session, "session");

        ZombiesMap currentMap = session.getMap();
        Region3I selected = session.getSelection();
        buttonAdd.setOnClick(() -> {
            String value = textFieldName.getText();
            if(value.isEmpty()) {
                return;
            }

            Key doorKey = Key.key(Namespaces.PHANTAZM, value);
            for(DoorInfo doorInfo : currentMap.doors()) {
                if(doorInfo.id().equals(doorKey)) {
                    doorInfo.regions().add(selected);
                    session.refreshDoors();
                    session.setLastDoor(doorInfo);
                    ScreenUtils.closeCurrentScreen();
                    return;
                }
            }

            List<Region3I> bounds = new ArrayList<>(1);
            bounds.add(selected);

            DoorInfo newDoor = new DoorInfo(doorKey, bounds);
            currentMap.doors().add(newDoor);

            session.refreshDoors();
            session.setLastDoor(newDoor);
            ScreenUtils.closeCurrentScreen();
        });
    }
}

