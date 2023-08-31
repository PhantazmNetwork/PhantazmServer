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
import org.phantazm.core.command.PermissionLockedCommand;
import org.phantazm.zombies.scene.ZombiesScene;

import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;

public class FlagToggleCommand extends PermissionLockedCommand {
    public static final Permission PERMISSION = new Permission("zombies.playtest.flag_toggle");

    private static final ArgumentString FLAG_ARGUMENT = ArgumentType.String("flag");

    public FlagToggleCommand(@NotNull Function<? super UUID, Optional<ZombiesScene>> sceneMapper,
        @NotNull KeyParser keyParser) {
        super("toggle_flag", PERMISSION);

        addConditionalSyntax(CommandUtils.playerSenderCondition(), (sender, context) -> {
            Player player = (Player) sender;
            UUID uuid = player.getUuid();

            sceneMapper.apply(uuid).ifPresent(scene -> {
                scene.setLegit(false);

                @Subst(Constants.NAMESPACE_OR_KEY)
                String flag = context.get(FLAG_ARGUMENT);

                if (keyParser.isValidKey(flag)) {
                    Key key = keyParser.parseKey(flag);

                    boolean res = scene.getMap().mapObjects().module().flags().toggleFlag(key);
                    sender.sendMessage("Toggled flag " + key + " to " + res);
                } else {
                    sender.sendMessage("Invalid key " + flag);
                }
            });
        }, FLAG_ARGUMENT);

    }
}
