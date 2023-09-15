package org.phantazm.server.command.server;

import com.github.steanky.toolkit.collection.Wrapper;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.command.builder.arguments.minecraft.ArgumentUUID;
import net.minestom.server.command.builder.suggestion.SuggestionEntry;
import net.minestom.server.entity.Player;
import net.minestom.server.permission.Permission;
import org.jetbrains.annotations.NotNull;
import org.phantazm.core.command.CommandUtils;
import org.phantazm.core.command.PermissionLockedCommand;
import org.phantazm.core.player.PlayerView;
import org.phantazm.core.player.PlayerViewProvider;
import org.phantazm.core.scene2.*;
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
                if (scene.isGame() && scene instanceof IdentifiableScene identifiableScene &&
                    scene instanceof WatchableScene) {
                    suggestion.addEntry(new SuggestionEntry(identifiableScene.identity().toString(), null));
                }
            });
        });

        addConditionalSyntax(CommandUtils.playerSenderCondition(), ((sender, context) -> {
            Player player = (Player) sender;
            PlayerView playerView = viewProvider.fromPlayer(player);

            UUID argument = context.get(sceneArgument);

            Wrapper<InstanceScene> sceneWrapper = Wrapper.ofNull();
            SceneManager.Global.instance().forEachScene(scene -> {
                if (scene.isGame() && scene instanceof InstanceScene instanceScene) {
                    sceneWrapper.set(instanceScene);
                }
            });

            Scene target = sceneWrapper.get();
            if (target == null) {
                sender.sendMessage(Component.text("No scene found with that UUID").color(NamedTextColor.RED));
                return;
            }

            SceneManager.Global.instance().joinScene(new JoinSpectator<>(Set.of(playerView), InstanceScene.class,
                true, argument));
        }), sceneArgument);
    }
}
