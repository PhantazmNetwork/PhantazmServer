package org.phantazm.core.guild.party;

import it.unimi.dsi.fastutil.Pair;
import it.unimi.dsi.fastutil.objects.ObjectBooleanPair;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.minestom.server.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.phantazm.core.chat.ChatChannel;

import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.function.Function;

public class PartyChatChannel implements ChatChannel {

    public static final String CHANNEL_NAME = "party";

    private final Map<? super UUID, ? extends Party> parties;

    private final MiniMessage miniMessage;

    private final String chatFormat;

    private final String spyChatFormat;

    private final Function<? super Player, ? extends CompletableFuture<Component>> nameFormatter;

    public PartyChatChannel(@NotNull Map<? super UUID, ? extends Party> parties, @NotNull MiniMessage miniMessage,
        @NotNull String chatFormat, @NotNull String spyChatFormat,
        @NotNull Function<? super Player, ? extends CompletableFuture<Component>> nameFormatter) {
        this.parties = Objects.requireNonNull(parties);
        this.miniMessage = Objects.requireNonNull(miniMessage);
        this.chatFormat = Objects.requireNonNull(chatFormat);
        this.spyChatFormat = Objects.requireNonNull(spyChatFormat);
        this.nameFormatter = Objects.requireNonNull(nameFormatter);
    }

    @Override
    public @NotNull CompletableFuture<Void> sendMessage(@NotNull Player from, @NotNull String message,
        @NotNull Consumer<ObjectBooleanPair<Component>> onFailure) {
        Pair<Party, ObjectBooleanPair<Component>> partyPair = getParty(from);
        if (partyPair.left() == null) {
            onFailure.accept(partyPair.right());
            return CompletableFuture.completedFuture(null);
        }

        return nameFormatter.apply(from).thenAccept(component -> {
            TagResolver senderPlaceholder = Placeholder.component("sender", component);
            TagResolver messagePlaceholder = Placeholder.unparsed("message", message);

            Component mainMessage = miniMessage.deserialize(chatFormat, senderPlaceholder, messagePlaceholder);
            partyPair.left().getAudience().sendMessage(mainMessage);

            Component spyMessage = miniMessage.deserialize(spyChatFormat, senderPlaceholder, messagePlaceholder);
            partyPair.left().getSpyAudience().sendMessage(spyMessage);
        });
    }

    private @NotNull Pair<Party, ObjectBooleanPair<Component>> getParty(@NotNull Player player) {
        Party party = parties.get(player.getUuid());
        if (party == null) {
            return Pair.of(null,
                ObjectBooleanPair.of(Component.text("You are not in a party.", NamedTextColor.RED), true));
        }

        return Pair.of(party, null);
    }
}
