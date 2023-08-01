package org.phantazm.core.friend.command;

import net.kyori.adventure.text.minimessage.MiniMessage;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.arguments.Argument;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.entity.Player;
import net.minestom.server.network.ConnectionManager;
import org.jetbrains.annotations.NotNull;
import org.phantazm.core.friend.FriendDatabase;
import org.phantazm.core.friend.FriendNotification;
import org.phantazm.core.player.PlayerView;
import org.phantazm.core.player.PlayerViewProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class FriendRemoveCommand {

    private static final Logger LOGGER = LoggerFactory.getLogger(FriendRemoveCommand.class);

    private FriendRemoveCommand() {
        throw new UnsupportedOperationException();
    }

    public static @NotNull Command removeCommand(@NotNull FriendCommandConfig config, @NotNull MiniMessage miniMessage,
            @NotNull ConnectionManager connectionManager, @NotNull FriendDatabase database,
            @NotNull FriendNotification notification, @NotNull PlayerViewProvider viewProvider) {
        Objects.requireNonNull(config, "config");
        Objects.requireNonNull(miniMessage, "miniMessage");
        Objects.requireNonNull(connectionManager, "connectionManager");

        Argument<String> nameArgument = ArgumentType.String("name");
        Command command = new Command("remove");
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
            String name = context.get(nameArgument);

            PlayerView remover = viewProvider.fromPlayer(((Player)sender));
            CompletableFuture<Optional<PlayerView>> targetFuture = viewProvider.fromName(name);
            viewProvider.fromName(name).whenComplete((playerViewOptional, throwable) -> {
                if (throwable != null) {
                    LOGGER.warn("Failed to get PlayerView for {}", name);
                    return;
                }

                if (playerViewOptional.isEmpty()) {
                    remover.getPlayer().ifPresent(player -> {
                        player.sendMessage(config.mustBeAPlayer());
                    });
                    return;
                }

                PlayerView target = playerViewOptional.get();
                database.hasFriend(remover.getUUID(), target.getUUID()).whenComplete((hasFriend, throwable1) -> {
                    if (throwable1 != null) {
                        LOGGER.warn("Failed to check if {} is a friend of {}", target.getUUID(), remover.getUUID());
                        return;
                    }

                    if (!hasFriend) {
                        remover.getPlayer().ifPresent(player -> {
                            player.sendMessage(config.mustBeFriends());
                        });
                        return;
                    }

                    database.removeFriend(remover.getUUID(), target.getUUID());
                    notification.notifyRemove(remover, target);
                });
            });
        }, nameArgument);

        return command;
    }

}
