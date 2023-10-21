package org.phantazm.core.chat;

import it.unimi.dsi.fastutil.Pair;
import it.unimi.dsi.fastutil.objects.ObjectBooleanPair;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.audience.ForwardingAudience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.Instance;
import org.jetbrains.annotations.NotNull;
import org.phantazm.commons.FutureUtils;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Basic implementation of a {@link ChatChannel}.
 */
public class BasicChatChannel implements ChatChannel {

    private final MiniMessage miniMessage;

    private final String chatFormat;
    private final Function<? super Player, ? extends CompletableFuture<Component>> nameFormatter;

    /**
     * Creates a {@link BasicChatChannel}.
     */
    public BasicChatChannel(@NotNull MiniMessage miniMessage, @NotNull String chatFormat,
        @NotNull Function<? super Player, ? extends CompletableFuture<Component>> nameFormatter) {
        this.miniMessage = Objects.requireNonNull(miniMessage);
        this.chatFormat = Objects.requireNonNull(chatFormat);
        this.nameFormatter = Objects.requireNonNull(nameFormatter);
    }

    @Override
    public @NotNull CompletableFuture<Void> sendMessage(@NotNull Player from, @NotNull String message,
        @NotNull Consumer<ObjectBooleanPair<Component>> onFailure) {
        Pair<Audience, ObjectBooleanPair<Component>> audiencePair = getAudience(from);
        if (audiencePair.left() == null) {
            onFailure.accept(audiencePair.right());
            return FutureUtils.nullCompletedFuture();
        }

        return formatMessage(from, message).thenAccept(component -> {
            audiencePair.left().sendMessage(component);
        });
    }

    private CompletableFuture<Component> formatMessage(@NotNull Player player, @NotNull String message) {
        return nameFormatter.apply(player).thenApply((component) -> {
            TagResolver senderPlaceholder = Placeholder.component("sender", component);
            TagResolver messagePlaceholder = Placeholder.unparsed("message", message);

            return miniMessage.deserialize(chatFormat, senderPlaceholder, messagePlaceholder);
        });
    }

    /**
     * Gets an {@link Audience} from a {@link Player}.
     *
     * @param player The {@link Player} to retrieve the {@link Audience} from
     * @return A {@link Pair} representing the result. If the left {@link Audience} is present, an {@link Audience} was
     * successfully found. If the left {@link Audience} is null, then the right {@link Pair} has a {@link Component}
     * provides an error message and a boolean that determines whether the chat channel is now invalid.
     */
    private @NotNull Pair<Audience, ObjectBooleanPair<Component>> getAudience(@NotNull Player player) {
        Instance instance = player.getInstance();
        if (instance == null) {
            return Pair.of(null,
                ObjectBooleanPair.of(Component.text("You are not in an instance.", NamedTextColor.RED), true));
        }

        return Pair.of((ForwardingAudience) () -> List.of(instance), null);
    }
}
