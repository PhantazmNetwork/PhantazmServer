package org.phantazm.zombies.command;

import com.github.steanky.element.core.key.Constants;
import com.github.steanky.element.core.key.KeyParser;
import net.kyori.adventure.key.Key;
import net.minestom.server.command.builder.arguments.ArgumentString;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.entity.Player;
import net.minestom.server.permission.Permission;
import org.intellij.lang.annotations.Subst;
import org.jetbrains.annotations.NotNull;
import org.phantazm.core.command.CommandUtils;
import org.phantazm.core.player.PlayerView;
import org.phantazm.core.player.PlayerViewProvider;
import org.phantazm.core.scene2.SceneManager;
import org.phantazm.zombies.scene2.ZombiesScene;

public class FlagToggleCommand extends SandboxLockedCommand {
    public static final Permission PERMISSION = new Permission("zombies.playtest.flag_toggle");

    private static final ArgumentString FLAG_ARGUMENT = ArgumentType.String("flag");

    public FlagToggleCommand(@NotNull PlayerViewProvider viewProvider,
        @NotNull KeyParser keyParser) {
        super("toggle_flag", PERMISSION);

        addConditionalSyntax(CommandUtils.playerSenderCondition(), (sender, context) -> {
            PlayerView playerView = viewProvider.fromPlayer((Player) sender);
            SceneManager.Global.instance().currentScene(playerView, ZombiesScene.class).ifPresent(scene -> {
                if (super.cannotExecute(sender, scene)) {
                    return;
                }

                scene.getAcquirable().sync(self -> {
                    self.setLegit(false);

                    @Subst(Constants.NAMESPACE_OR_KEY)
                    String flag = context.get(FLAG_ARGUMENT);

                    if (keyParser.isValidKey(flag)) {
                        Key key = keyParser.parseKey(flag);

                        boolean res = self.map().mapObjects().module().flags().toggleFlag(key);
                        sender.sendMessage("Toggled flag " + key + " to " + res);
                    } else {
                        sender.sendMessage("Invalid key " + flag);
                    }
                });
            });
        }, FLAG_ARGUMENT);

    }
}
