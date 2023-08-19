package org.phantazm.zombies.map.shop.interactor;

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
import org.jetbrains.annotations.NotNull;
import org.phantazm.zombies.map.Room;
import org.phantazm.zombies.map.objects.MapObjects;
import org.phantazm.zombies.map.shop.InteractorGroupHandler;
import org.phantazm.zombies.map.shop.PlayerInteraction;

import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

@Model("zombies.map.shop.interactor.announce_active_selection_group")
@Cache(false)
public class AnnounceActiveSelectionGroup implements ShopInteractor {
    private final Data data;
    private final Supplier<? extends MapObjects> mapObjects;
    private final InteractorGroupHandler groupHandler;

    @FactoryMethod
    public AnnounceActiveSelectionGroup(@NotNull Data data, @NotNull Supplier<? extends MapObjects> mapObjects,
        @NotNull InteractorGroupHandler groupHandler) {
        this.data = data;
        this.mapObjects = mapObjects;
        this.groupHandler = groupHandler;
    }

    @Override
    public boolean handleInteraction(@NotNull PlayerInteraction interaction) {
        List<SelectionGroupInteractor> interactors = groupHandler.interactors(data.group);

        SelectionGroupInteractor active = null;
        for (SelectionGroupInteractor interactor : interactors) {
            if (interactor.isActive()) {
                active = interactor;
                break;
            }
        }

        if (active == null) {
            return false;
        }

        Optional<Room> roomOptional = mapObjects.get().roomTracker().atPoint(active.shop().center());
        if (roomOptional.isEmpty()) {
            return false;
        }

        TagResolver tag = Placeholder.component("active_room", roomOptional.get().getRoomInfo().displayName());
        Component message = MiniMessage.miniMessage().deserialize(data.format, tag);
        if (data.broadcast) {
            mapObjects.get().module().instance().sendMessage(message);
        } else {
            interaction.player().getPlayer().ifPresent(player -> player.sendMessage(message));
        }

        return true;
    }

    @DataObject
    public record Data(@NotNull Key group,
        @NotNull String format,
        boolean broadcast) {
        @Default("broadcast")
        public static @NotNull ConfigElement defaultBroadcast() {
            return ConfigPrimitive.of(false);
        }
    }
}
