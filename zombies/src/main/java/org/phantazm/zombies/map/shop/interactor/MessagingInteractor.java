package org.phantazm.zombies.map.shop.interactor;

import com.github.steanky.element.core.annotation.Cache;
import com.github.steanky.element.core.annotation.DataObject;
import com.github.steanky.element.core.annotation.FactoryMethod;
import com.github.steanky.element.core.annotation.Model;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.Instance;
import org.jetbrains.annotations.NotNull;
import org.phantazm.zombies.map.shop.PlayerInteraction;

import java.util.List;
import java.util.Objects;

@Model("zombies.map.shop.interactor.messaging")
@Cache(false)
public class MessagingInteractor extends InteractorBase<MessagingInteractor.Data> {
    private final Instance instance;

    @FactoryMethod
    public MessagingInteractor(@NotNull Data data, @NotNull Instance instance) {
        super(data);
        this.instance = Objects.requireNonNull(instance);
    }

    @Override
    public boolean handleInteraction(@NotNull PlayerInteraction interaction) {
        if (data.broadcast) {
            interaction.player().module().getPlayerView().getPlayer()
                .ifPresent(player -> sendMessages(instance, player));

        } else {
            interaction.player().module().getPlayerView().getPlayer().ifPresent(player -> sendMessages(player, player));
        }

        return true;
    }

    private void sendMessages(Audience audience, Player player) {
        Component displayName = player.getDisplayName();
        if (displayName == null) {
            displayName = player.getName();
        }
        TagResolver sender = Placeholder.component("sender", displayName);
        for (String format : data.messages) {
            audience.sendMessage(MiniMessage.miniMessage().deserialize(format, sender));
        }
    }

    @DataObject
    public record Data(@NotNull List<String> messages,
        boolean broadcast) {
    }
}
