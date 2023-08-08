package org.phantazm.core.chat;

import it.unimi.dsi.fastutil.Pair;
import it.unimi.dsi.fastutil.objects.ObjectBooleanPair;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.minestom.server.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.phantazm.core.player.PlayerView;
import org.phantazm.core.player.PlayerViewProvider;

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

    private final MiniMessage miniMessage;

    private final String chatFormat;
    private final Function<? super Player, ? extends Component> nameFormatter;

    /**
     * Creates a {@link BasicChatChannel}.
     *
     * @param viewProvider The {@link BasicChatChannel}'s {@link PlayerViewProvider}
     */
    public BasicChatChannel(@NotNull PlayerViewProvider viewProvider, @NotNull MiniMessage miniMessage,
            @NotNull String chatFormat, @NotNull Function<? super Player, ? extends Component> nameFormatter) {
        this.viewProvider = Objects.requireNonNull(viewProvider, "viewProvider");
        this.miniMessage = Objects.requireNonNull(miniMessage, "miniMessage");
        this.chatFormat = Objects.requireNonNull(chatFormat, "chatFormat");
        this.nameFormatter = Objects.requireNonNull(nameFormatter, "nameFormatter");
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
    public @NotNull Component formatMessage(@NotNull Player player, @NotNull String message) {
        Component displayName = nameFormatter.apply(player);

        TagResolver senderPlaceholder = Placeholder.component("sender", displayName);
        TagResolver messagePlaceholder = Placeholder.unparsed("message", message);

        return miniMessage.deserialize(chatFormat, senderPlaceholder, messagePlaceholder);
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

    protected @NotNull PlayerViewProvider getViewProvider() {
        return viewProvider;
    }
}
