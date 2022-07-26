package com.github.phantazmnetwork.core.chat;

import com.github.phantazmnetwork.core.player.PlayerView;
import com.github.phantazmnetwork.core.player.PlayerViewProvider;
import it.unimi.dsi.fastutil.Pair;
import it.unimi.dsi.fastutil.objects.ObjectBooleanPair;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.entity.Player;
import net.minestom.server.event.player.PlayerChatEvent;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Basic implementation of a {@link ChatChannel}.
 */
public abstract class BasicChatChannel implements ChatChannel {

    private final PlayerViewProvider viewProvider;

    /**
     * Creates a {@link BasicChatChannel}.
     *
     * @param viewProvider The {@link BasicChatChannel}'s {@link PlayerViewProvider}
     */
    public BasicChatChannel(@NotNull PlayerViewProvider viewProvider) {
        this.viewProvider = Objects.requireNonNull(viewProvider, "viewProvider");
    }

    @Override
    public void findAudience(@NotNull UUID channelMember, @NotNull Consumer<Audience> onSuccess,
            @NotNull Consumer<ObjectBooleanPair<Component>> onFailure) {
        PlayerView view = viewProvider.fromUUID(channelMember);
        Optional<Player> playerOptional = view.getPlayer();
        if (playerOptional.isPresent()) {
            Player player = playerOptional.get();
            Pair<Audience, ObjectBooleanPair<Component>> audiencePair = getAudience(player);
            if (audiencePair.left() != null) {
                onSuccess.accept(audiencePair.left());
                return;
            }

            onFailure.accept(audiencePair.right());
        }
        else {
            onFailure.accept(ObjectBooleanPair.of(Component.text("You are offline.", NamedTextColor.RED), false));
        }
    }


    @Override
    public @NotNull Component formatMessage(@NotNull PlayerChatEvent chatEvent) {
        Function<PlayerChatEvent, Component> chatFormatter = chatEvent.getChatFormatFunction();
        if (chatFormatter != null) {
            return chatFormatter.apply(chatEvent);
        }

        return chatEvent.getDefaultChatFormat().get();
    }

    /**
     * Gets an {@link Audience} from a {@link Player}.
     *
     * @param player The {@link Player} to retrieve the {@link Audience} from
     * @return A {@link Pair} representing the result. If the left {@link Audience} is present, an {@link Audience} was
     * successfully found. If the left {@link Audience} is null,
     * then the right {@link Pair} has a {@link Component} provides an error message
     * and a boolean that determines whether the chat channel is now invalid.
     */
    protected abstract @NotNull Pair<Audience, ObjectBooleanPair<Component>> getAudience(@NotNull Player player);

}
