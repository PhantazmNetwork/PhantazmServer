package org.phantazm.zombies.mapeditor.client.ui;

import com.github.steanky.ethylene.core.collection.ArrayConfigList;
import com.github.steanky.vector.Bounds3I;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;
import org.phantazm.commons.LogicUtils;
import org.phantazm.commons.Namespaces;
import org.phantazm.zombies.map.MapInfo;
import org.phantazm.zombies.map.RoomInfo;
import org.phantazm.zombies.mapeditor.client.EditorSession;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * General UI for creating a new room.
 */
public class NewRoomGui extends NamedObjectGui {
    /**
     * Constructs a new instance of this GUI, which allows the user to either create a new room or add regions to an
     * existing room.
     *
     * @param session the current {@link EditorSession}
     */
    @SuppressWarnings("PatternValidation")
    public NewRoomGui(@NotNull EditorSession session) {
        super(LogicUtils.nullCoalesce(session.lastRoom(), room -> room.id().value()));

        Objects.requireNonNull(session, "session");

        MapInfo currentMap = session.getMap();
        Bounds3I selected = session.getSelection();

        buttonAdd.setOnClick(() -> {
            String value = textFieldName.getText();
            if (value.isEmpty()) {
                return;
            }

            Key roomKey = Key.key(Namespaces.PHANTAZM, value);
            for (RoomInfo roomInfo : currentMap.rooms()) {
                if (roomInfo.id().equals(roomKey)) {
                    roomInfo.regions().add(selected);
                    session.refreshRooms();
                    session.setLastRoom(roomInfo);
                    ScreenUtils.closeCurrentScreen();
                    return;
                }
            }

            List<Bounds3I> bounds = new ArrayList<>(1);
            bounds.add(selected);

            RoomInfo newRoom = new RoomInfo(roomKey, Component.text(roomKey.value()), bounds, new ArrayConfigList(0));
            session.setLastRoom(newRoom);
            currentMap.rooms().add(newRoom);
            session.refreshRooms();
            ScreenUtils.closeCurrentScreen();
        });
    }
}
