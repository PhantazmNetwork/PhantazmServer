package org.phantazm.server.command.server;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.command.builder.arguments.minecraft.ArgumentUUID;
import net.minestom.server.command.builder.suggestion.SuggestionEntry;
import net.minestom.server.entity.Player;
import net.minestom.server.permission.Permission;
import org.jetbrains.annotations.NotNull;
import org.phantazm.core.game.scene.RouterStore;
import org.phantazm.core.game.scene.Scene;
import org.phantazm.core.game.scene.SceneRouter;
import org.phantazm.core.game.scene.SceneTransferHelper;
import org.phantazm.core.player.PlayerViewProvider;

import java.util.UUID;

public class GhostCommand extends Command {
    public static final Permission PERMISSION = new Permission("admin.ghost");

    public GhostCommand(@NotNull PlayerViewProvider viewProvider, @NotNull SceneTransferHelper transferHelper,
            @NotNull RouterStore routerStore) {
        super("ghost");

        setCondition((sender, commandString) -> sender.hasPermission(PERMISSION));

        ArgumentUUID sceneArgument = ArgumentType.UUID("scene");
        sceneArgument.setSuggestionCallback((sender, context, suggestion) -> {
            for (SceneRouter<?, ?> router : routerStore.getRouters()) {
                if (!router.isGame()) {
                    continue;
                }

                for (Scene<?> scene : router.getScenes()) {
                    suggestion.addEntry(new SuggestionEntry(scene.getUUID().toString(), null));
                }
            }
        });

        addConditionalSyntax(getCondition(), ((sender, context) -> {
            if (!(sender instanceof Player player)) {
                sender.sendMessage(
                        Component.text("You must be a player to use this command!").color(NamedTextColor.RED));
                return;
            }

            UUID argument = context.get(sceneArgument);
            for (SceneRouter<?, ?> router : routerStore.getRouters()) {
                if (!router.isGame()) {
                    continue;
                }

                for (Scene<?> scene : router.getScenes()) {
                    if (scene.getUUID().equals(argument)) {
                        transferHelper.ghost(scene, viewProvider.fromPlayer(player));
                        return;
                    }
                }
            }

            sender.sendMessage(Component.text("No scene found with that UUID").color(NamedTextColor.RED));
        }), sceneArgument);
    }
}
