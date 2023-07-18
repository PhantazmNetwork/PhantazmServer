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
import net.minestom.server.entity.Player;
import net.minestom.server.instance.Instance;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.phantazm.zombies.map.objects.MapObjects;
import org.phantazm.zombies.map.shop.InteractorGroupHandler;
import org.phantazm.zombies.map.shop.PlayerInteraction;

import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.function.Supplier;

@Model("zombies.map.shop.interactor.change_selection_group")
@Cache(false)
public class ChangeSelectionGroupInteractor implements ShopInteractor {
    private static final Component UNKNOWN_ROOM = Component.text("an unknown room");

    private final Data data;
    private final InteractorGroupHandler groupHandler;
    private final Random random;
    private final Supplier<? extends MapObjects> mapObjects;

    @FactoryMethod
    public ChangeSelectionGroupInteractor(@NotNull Data data, @NotNull InteractorGroupHandler groupHandler,
            @NotNull Random random, @NotNull Supplier<? extends MapObjects> mapObjects) {
        this.data = data;
        this.groupHandler = groupHandler;
        this.random = random;
        this.mapObjects = mapObjects;
    }

    @Override
    public boolean handleInteraction(@NotNull PlayerInteraction interaction) {
        List<SelectionGroupInteractor> interactors = groupHandler.interactors(data.group);
        if (interactors.isEmpty()) {
            return false;
        }

        boolean hasCurrentlyActiveInteractor = false;
        int currentActiveInteractorIndex = 0;
        for (SelectionGroupInteractor interactor : interactors) {
            if (interactor.isActive()) {
                hasCurrentlyActiveInteractor = true;
                break;
            }

            currentActiveInteractorIndex++;
        }

        if (!hasCurrentlyActiveInteractor) {
            SelectionGroupInteractor newActive = interactors.get(random.nextInt(interactors.size()));
            newActive.setActive(true);
            maybeSendMessage(newActive, interaction);
            return true;
        }

        if (data.excludeCurrent) {
            if (interactors.size() == 1) {
                return true;
            }

            int newIndex = random.nextInt(interactors.size() - 1);
            if (newIndex >= currentActiveInteractorIndex) {
                newIndex++;
            }

            interactors.get(currentActiveInteractorIndex).setActive(false);

            SelectionGroupInteractor newActive = interactors.get(newIndex);
            newActive.setActive(true);
            maybeSendMessage(newActive, interaction);
            return true;
        }

        SelectionGroupInteractor newInteractor = interactors.get(random.nextInt(interactors.size()));
        SelectionGroupInteractor oldInteractor = interactors.get(currentActiveInteractorIndex);
        if (newInteractor == oldInteractor) {
            return true;
        }

        oldInteractor.setActive(false);
        newInteractor.setActive(true);
        maybeSendMessage(newInteractor, interaction);
        return true;
    }

    private void maybeSendMessage(SelectionGroupInteractor newActive, PlayerInteraction interaction) {
        if (data.moveMessage == null) {
            return;
        }

        Optional<Player> playerOptional = interaction.player().getPlayer();
        if (playerOptional.isEmpty()) {
            return;
        }

        Player player = playerOptional.get();

        MapObjects mapObjects = this.mapObjects.get();
        Component roomName = mapObjects.roomTracker().atPoint(newActive.shop().center())
                .map(room -> room.getRoomInfo().displayName()).orElse(UNKNOWN_ROOM);

        TagResolver roomNameTag = Placeholder.component("room_name", roomName);
        Component message = MiniMessage.miniMessage().deserialize(data.moveMessage, roomNameTag);

        if (data.broadcast) {
            Instance instance = player.getInstance();
            if (instance == null) {
                return;
            }

            instance.sendMessage(message);
        }
        else {
            player.sendMessage(message);
        }
    }

    @DataObject
    public record Data(@NotNull Key group, boolean excludeCurrent, @Nullable String moveMessage, boolean broadcast) {
        @Default("excludeCurrent")
        public static @NotNull ConfigElement defaultExcludeCurrent() {
            return ConfigPrimitive.of(true);
        }

        @Default("moveMessage")
        public static @NotNull ConfigElement defaultMoveMessage() {
            return ConfigPrimitive.NULL;
        }

        @Default("broadcast")
        public static @NotNull ConfigElement defaultBroadcast() {
            return ConfigPrimitive.of(false);
        }
    }
}
