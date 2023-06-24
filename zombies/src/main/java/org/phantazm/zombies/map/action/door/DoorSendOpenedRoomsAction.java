package org.phantazm.zombies.map.action.door;

import com.github.steanky.element.core.annotation.DataObject;
import com.github.steanky.element.core.annotation.FactoryMethod;
import com.github.steanky.element.core.annotation.Model;
import com.github.steanky.ethylene.core.ConfigElement;
import com.github.steanky.ethylene.core.ConfigPrimitive;
import com.github.steanky.ethylene.mapper.annotation.Default;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.title.TitlePart;
import net.minestom.server.instance.Instance;
import org.jetbrains.annotations.NotNull;
import org.phantazm.core.ComponentUtils;
import org.phantazm.zombies.map.Door;
import org.phantazm.zombies.map.Room;
import org.phantazm.zombies.map.action.Action;
import org.phantazm.zombies.map.objects.MapObjects;
import org.phantazm.zombies.player.ZombiesPlayer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
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
                    return;
                }

                if (username != null) {
                    instance.sendTitlePart(data.nameTitlePart,
                            ComponentUtils.tryFormat(data.nameFormatString, username));
                }
                else {
                    LOGGER.warn("Null username");
                    instance.sendTitlePart(data.nameTitlePart, UNKNOWN_COMPONENT);
                }
            });
        }
        else {
            LOGGER.warn("Interacting player was null, cannot announce opener");
            instance.sendTitlePart(data.nameTitlePart, UNKNOWN_COMPONENT);
        }

        Map<? super Key, ? extends Room> roomMap = mapObjects.get().roomMap();
        List<Room> opensTo =
                door.doorInfo().opensTo().stream().map(target -> (Room)roomMap.get(target)).filter(room -> {
                    if (room == null) {
                        return false;
                    }

                    return !room.isOpen();
                }).toList();

        StringBuilder builder = new StringBuilder();
        boolean appendedRoom = false;
        for (int i = 0; i < opensTo.size(); i++) {
            Room room = opensTo.get(i);

            builder.append(MiniMessage.miniMessage().serialize(room.getRoomInfo().displayName()));
            appendedRoom = true;

            if (i < opensTo.size() - 1) {
                builder.append(data.separator);
            }
        }

        if (!appendedRoom) {
            builder.append("...");
        }

        instance.sendTitlePart(data.openedRoomsTitlePart,
                ComponentUtils.tryFormat(data.openedRoomsFormatString, builder.toString()));
    }

    @DataObject
    public record Data(@NotNull String nameFormatString,
                       @NotNull String openedRoomsFormatString,
                       @NotNull String separator,
                       @NotNull TitlePart<Component> nameTitlePart,
                       @NotNull TitlePart<Component> openedRoomsTitlePart) {
        @Default("separator")
        public static ConfigElement separatorDefault() {
            return ConfigPrimitive.of(", ");
        }
    }
}
