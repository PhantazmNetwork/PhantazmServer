package org.phantazm.server.command.server;

import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.command.builder.arguments.minecraft.ArgumentUUID;
import net.minestom.server.command.builder.suggestion.SuggestionEntry;
import net.minestom.server.entity.Player;
import net.minestom.server.permission.Permission;
import org.jetbrains.annotations.NotNull;
import org.phantazm.core.command.CommandUtils;
import org.phantazm.core.command.PermissionLockedCommand;
import org.phantazm.core.player.PlayerViewProvider;
import org.phantazm.core.scene2.InstanceScene;
import org.phantazm.core.scene2.SceneManager;
import org.phantazm.core.scene2.join.JoinSpectator;

import java.util.Set;
import java.util.UUID;

public class GhostCommand extends PermissionLockedCommand {
    public static final Permission PERMISSION = new Permission("admin.ghost");

    public GhostCommand(@NotNull PlayerViewProvider viewProvider) {
        super("ghost", PERMISSION);

        ArgumentUUID sceneArgument = ArgumentType.UUID("scene");
        sceneArgument.setSuggestionCallback((sender, context, suggestion) -> {
            SceneManager.Global.instance().forEachScene(scene -> {
                if (scene.isGame() && scene instanceof InstanceScene instanceScene) {
                    suggestion.addEntry(new SuggestionEntry(instanceScene.identity().toString(), null));
                }
            });
        });

        addConditionalSyntax(CommandUtils.playerSenderCondition(), ((sender, context) -> {
            UUID argument = context.get(sceneArgument);
            SceneManager.Global.instance().joinScene(new JoinSpectator<>(Set.of(viewProvider
                .fromPlayer((Player) sender)), InstanceScene.class, true, argument));
        }), sceneArgument);
    }
}