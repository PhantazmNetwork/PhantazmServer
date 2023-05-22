package org.phantazm.zombies.mapeditor.client.ui;

import com.github.steanky.vector.Bounds3I;
import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.NotNull;
import org.phantazm.commons.LogicUtils;
import org.phantazm.commons.Namespaces;
import org.phantazm.zombies.map.DoorInfo;
import org.phantazm.zombies.map.MapInfo;
import org.phantazm.zombies.mapeditor.client.EditorSession;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * GUI class used to create {@link DoorInfo}.
 */
public class NewDoorGui extends NamedObjectGui {
    /**
     * Constructs a new instance of this GUI. The user may create a new door, or add regions to an existing door.
     *
     * @param session the current {@link EditorSession}
     */
    @SuppressWarnings("PatternValidation")
    public NewDoorGui(@NotNull EditorSession session) {
        super(LogicUtils.nullCoalesce(session.lastDoor(), door -> door.id().value()));

        Objects.requireNonNull(session, "session");

        MapInfo currentMap = session.getMap();
        Bounds3I selected = session.getSelection().shift(currentMap.settings().origin().mul(-1));
        buttonAdd.setOnClick(() -> {
            String value = textFieldName.getText();
            if (value.isEmpty()) {
                return;
            }

            Key doorKey = Key.key(Namespaces.PHANTAZM, value);
            for (DoorInfo doorInfo : currentMap.doors()) {
                if (doorInfo.id().equals(doorKey)) {
                    doorInfo.regions().add(selected);
                    session.refreshDoors();
                    session.setLastDoor(doorInfo);
                    ScreenUtils.closeCurrentScreen();
                    return;
                }
            }

            List<Bounds3I> bounds = new ArrayList<>(1);
            bounds.add(selected);

            DoorInfo newDoor = new DoorInfo(doorKey, bounds);
            currentMap.doors().add(newDoor);

            session.refreshDoors();
            session.setLastDoor(newDoor);
            ScreenUtils.closeCurrentScreen();
        });
    }
}

