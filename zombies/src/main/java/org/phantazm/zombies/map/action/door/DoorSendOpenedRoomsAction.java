package org.phantazm.zombies.map.action.door;

import com.github.steanky.element.core.annotation.Cache;
import com.github.steanky.element.core.annotation.DataObject;
import com.github.steanky.element.core.annotation.FactoryMethod;
import com.github.steanky.element.core.annotation.Model;
import com.github.steanky.ethylene.core.ConfigElement;
import com.github.steanky.ethylene.core.ConfigPrimitive;
import com.github.steanky.ethylene.mapper.annotation.Default;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.kyori.adventure.title.TitlePart;
import net.minestom.server.instance.Instance;
import org.jetbrains.annotations.NotNull;
import org.phantazm.commons.MiniMessageUtils;
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
@Cache(false)
public class DoorSendOpenedRoomsAction implements Action<Door> {
    private static final Logger LOGGER = LoggerFactory.getLogger(DoorSendOpenedRoomsAction.class);
    private static final Component UNKNOWN_COMPONENT = Component.text("...");

    private final Data data;
    private final Supplier<? extends MapObjects> mapObjects;
    private final Instance instance;
    private final MiniMessage miniMessage = MiniMessage.miniMessage();

    @FactoryMethod
    public DoorSendOpenedRoomsAction(@NotNull Data data, @NotNull Supplier<? extends MapObjects> mapObjects,
        @NotNull Instance instance) {
        this.data = Objects.requireNonNull(data);
        this.mapObjects = Objects.requireNonNull(mapObjects);
        this.instance = Objects.requireNonNull(instance);
    }

    @Override
    public void perform(@NotNull Door door) {
        Optional<ZombiesPlayer> lastInteractorOptional = door.lastInteractor();
        if (lastInteractorOptional.isPresent()) {
            lastInteractorOptional.get().module().getPlayerView().getDisplayName()
                .whenComplete((displayName, throwable) -> {
                    if (throwable != null) {
                        LOGGER.warn("Error resolving display name of door-opening player", throwable);
                        return;
                    }

                    TagResolver openerPlaceholder = Placeholder.component("opener", displayName);
                    instance.sendTitlePart(data.nameTitlePart,
                        miniMessage.deserialize(data.nameFormat, openerPlaceholder));
                });
        } else {
            LOGGER.warn("Interacting player was null, cannot announce opener");
            TagResolver openerPlaceholder = Placeholder.component("opener", UNKNOWN_COMPONENT);
            instance.sendTitlePart(data.nameTitlePart, miniMessage.deserialize(data.nameFormat, openerPlaceholder));
        }

        Map<? super Key, ? extends Room> roomMap = mapObjects.get().roomMap();
        List<Room> opensTo =
            door.doorInfo().opensTo().stream().map(target -> (Room) roomMap.get(target)).filter(room -> {
                if (room == null) {
                    return false;
                }

                return !room.isOpen();
            }).toList();

        List<Component> roomNames = new ArrayList<>(opensTo.size());
        if (opensTo.isEmpty()) {
            roomNames.add(Component.text("..."));
        } else {
            for (Room room : opensTo) {
                roomNames.add(room.getRoomInfo().displayName());
            }
        }
        TagResolver roomsPlaceholder = MiniMessageUtils.list("rooms", roomNames);

        instance.sendTitlePart(data.openedRoomsTitlePart,
            miniMessage.deserialize(data.openedRoomsFormat, roomsPlaceholder));
    }

    @DataObject
    public record Data(
        @NotNull String nameFormat,
        @NotNull String openedRoomsFormat,
        @NotNull String separator,
        @NotNull TitlePart<Component> nameTitlePart,
        @NotNull TitlePart<Component> openedRoomsTitlePart) {
        @Default("separator")
        public static ConfigElement separatorDefault() {
            return ConfigPrimitive.of(", ");
        }
    }
}
