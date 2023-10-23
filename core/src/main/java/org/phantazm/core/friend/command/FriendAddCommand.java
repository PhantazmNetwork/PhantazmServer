package org.phantazm.core.friend.command;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.arguments.Argument;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.entity.Player;
import net.minestom.server.network.ConnectionManager;
import org.jetbrains.annotations.NotNull;
import org.phantazm.core.friend.FriendDatabase;
import org.phantazm.core.friend.FriendRequestManager;
import org.phantazm.core.player.PlayerView;
import org.phantazm.core.player.PlayerViewProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;

public class FriendAddCommand {

    private static final Logger LOGGER = LoggerFactory.getLogger(FriendAddCommand.class);

    private FriendAddCommand() {
        throw new UnsupportedOperationException();
    }

    public static @NotNull Command addCommand(@NotNull FriendCommandConfig config, @NotNull MiniMessage miniMessage, @NotNull ConnectionManager connectionManager, @NotNull FriendDatabase database, @NotNull FriendRequestManager requestManager, @NotNull PlayerViewProvider viewProvider) {
        Objects.requireNonNull(config);
        Objects.requireNonNull(miniMessage);
        Objects.requireNonNull(connectionManager);
        Objects.requireNonNull(database);
        Objects.requireNonNull(requestManager);
        Objects.requireNonNull(viewProvider);

        Argument<String> nameArgument = ArgumentType.String("name");
        Command command = new Command("add");
        command.addConditionalSyntax((sender, commandString) -> {
            if (commandString == null) {
                return sender instanceof Player;
            }

            if (!(sender instanceof Player)) {
                sender.sendMessage(config.mustBeAPlayer());
                return false;
            }

            return true;
        }, (sender, context) -> {
            PlayerView requester = viewProvider.fromUUID(((Player) sender).getUuid());

            String name = context.get(nameArgument);
            Player targetPlayer = connectionManager.getPlayer(name);
            if (targetPlayer != null) {
                if (requestManager.hasOutgoingRequest(requester.getUUID(), targetPlayer.getUuid())) {
                    sender.sendMessage(config.alreadyRequested());
                    return;
                }

                PlayerView target = viewProvider.fromPlayer(targetPlayer);
                if (requestManager.hasIncomingRequest(targetPlayer.getUuid(), requester.getUUID())) {
                    requestManager.acceptRequest(requester, target);
                    return;
                }

                addInternal(config, miniMessage, database, requestManager, requester, target);
                return;
            }

            viewProvider.fromName(name).whenComplete((playerViewOptional, throwable) -> {
                if (throwable != null) {
                    LOGGER.warn("Failed to create PlayerView for {}", name, throwable);
                    return;
                }

                if (playerViewOptional.isEmpty()) {
                    requester.getPlayer().ifPresent(player -> {
                        player.sendMessage(config.mustBeAPlayer());
                    });
                    return;
                }

                PlayerView target = playerViewOptional.get();
                if (requestManager.hasOutgoingRequest(requester.getUUID(), target.getUUID())) {
                    requester.getPlayer().ifPresent(player -> {
                        player.sendMessage(config.alreadyRequested());
                    });
                    return;
                }
                if (requestManager.hasIncomingRequest(requester.getUUID(), target.getUUID())) {
                    requestManager.acceptRequest(requester, target);
                    return;
                }

                addInternal(config, miniMessage, database, requestManager, requester, target);
            });
        }, nameArgument);

        return command;
    }

    private static void addInternal(FriendCommandConfig config, MiniMessage miniMessage, FriendDatabase database,
        FriendRequestManager requestManager, PlayerView requester, PlayerView target) {
        CompletableFuture<? extends Component> nameFuture = target.getDisplayName();
        CompletableFuture<Boolean> hasFriendFuture = database.hasFriend(requester.getUUID(), target.getUUID());
        hasFriendFuture.whenComplete((result, throwable) -> {
            if (throwable != null) {
                LOGGER.warn("Failed to check if {} is a friend of {}", requester.getUUID(), target.getUUID(), throwable);
            }
        });

        CompletableFuture.allOf(nameFuture, hasFriendFuture).thenRun(() -> {
            if (hasFriendFuture.join()) {
                requester.getPlayer().ifPresent(player -> {
                    TagResolver namePlaceholder = Placeholder.component("target", nameFuture.join());
                    Component message = miniMessage.deserialize(config.alreadyFriendsFormat(), namePlaceholder);
                    player.sendMessage(message);
                });
                return;
            }

            requestManager.sendRequest(requester, target);
        });
    }

}
