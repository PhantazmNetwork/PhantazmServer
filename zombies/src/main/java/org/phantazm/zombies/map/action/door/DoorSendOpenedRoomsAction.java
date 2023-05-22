package org.phantazm.zombies.map.action.door;

import com.github.steanky.element.core.annotation.DataObject;
import com.github.steanky.element.core.annotation.FactoryMethod;
import com.github.steanky.element.core.annotation.Model;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.title.TitlePart;
import net.minestom.server.instance.Instance;
import org.jetbrains.annotations.NotNull;
import org.phantazm.zombies.map.Door;
import org.phantazm.zombies.map.Room;
import org.phantazm.zombies.map.action.Action;
import org.phantazm.zombies.map.objects.MapObjects;
import org.phantazm.zombies.player.ZombiesPlayer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;

@Model("zombies.map.door.action.send_opened_rooms")
public class DoorSendOpenedRoomsAction implements Action<Door> {
    private static final Logger LOGGER = LoggerFactory.getLogger(DoorSendOpenedRoomsAction.class);
    private static final Component UNKNOWN_COMPONENT = Component.text("...");

    private final Data data;
    private final Supplier<? extends MapObjects> mapObjects;
    private final Instance instance;

    @FactoryMethod
    public DoorSendOpenedRoomsAction(@NotNull Data data, @NotNull Supplier<? extends MapObjects> mapObjects,
            @NotNull Instance instance) {
        this.data = Objects.requireNonNull(data, "data");
        this.mapObjects = Objects.requireNonNull(mapObjects, "mapObjects");
        this.instance = Objects.requireNonNull(instance, "instance");
    }

    @Override
    public void perform(@NotNull Door door) {
        Optional<ZombiesPlayer> lastInteractorOptional = door.lastInteractor();
        if (lastInteractorOptional.isPresent()) {
            lastInteractorOptional.get().module().getPlayerView().getUsername().whenComplete((username, err) -> {
                if (err != null) {
                    LOGGER.warn("Error resolving display name of door-opening player", err);
                }

                if (username != null) {
                    instance.sendTitlePart(data.openerNameTitlePart,
                            Component.text().style(data.openerNameFormatStyle).append(Component.text(username))
                                    .build());
                }
                else {
                    LOGGER.warn("Null username");
                    instance.sendTitlePart(data.openerNameTitlePart, UNKNOWN_COMPONENT);
                }
            });
        }
        else {
            LOGGER.warn("Interacting player was null, cannot announce opener");
            instance.sendTitlePart(data.openerNameTitlePart, UNKNOWN_COMPONENT);
        }

        TextComponent.Builder builder =
                Component.text().style(data.openedRoomsFormatStyle).append(Component.text("opened "));

        Map<? super Key, ? extends Room> roomMap = mapObjects.get().roomMap();
        List<Key> opensTo = door.doorInfo().opensTo();

        boolean appendedRoom = false;
        for (int i = 0; i < opensTo.size(); i++) {
            Key target = opensTo.get(i);
            Room room = roomMap.get(target);
            if (room == null) {
                LOGGER.warn("Cannot announce the opening of nonexistent room named " + target);
                continue;
            }

            if (room.isOpen()) {
                continue;
            }

            builder.append(room.getRoomInfo().displayName());
            appendedRoom = true;

            if (i < opensTo.size() - 1) {
                builder.append(Component.text(", "));
            }
        }

        if (!appendedRoom) {
            builder.append(UNKNOWN_COMPONENT);
        }

        instance.sendTitlePart(data.openedRoomsTitlePart, builder.build());
    }

    @DataObject
    public record Data(@NotNull Style openerNameFormatStyle,
                       @NotNull Style openedRoomsFormatStyle,
                       @NotNull TitlePart<Component> openerNameTitlePart,
                       @NotNull TitlePart<Component> openedRoomsTitlePart) {

    }
}
