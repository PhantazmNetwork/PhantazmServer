package org.phantazm.zombies.command;

import com.github.steanky.element.core.key.Constants;
import com.github.steanky.element.core.key.KeyParser;
import net.kyori.adventure.key.Key;
import net.minestom.server.command.builder.CommandContext;
import net.minestom.server.command.builder.arguments.ArgumentString;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.entity.Player;
import net.minestom.server.permission.Permission;
import org.intellij.lang.annotations.Subst;
import org.jetbrains.annotations.NotNull;
import org.phantazm.zombies.scene2.ZombiesScene;

import java.util.Objects;

public class FlagToggleCommand extends SandboxLockedCommand {
    public static final Permission PERMISSION = new Permission("zombies.playtest.flag_toggle");

    private static final ArgumentString FLAG_ARGUMENT = ArgumentType.String("flag");

    private final KeyParser keyParser;

    public FlagToggleCommand(@NotNull KeyParser keyParser) {
        super("toggle_flag", PERMISSION, FLAG_ARGUMENT);
        this.keyParser = Objects.requireNonNull(keyParser);
    }

    @Override
    protected void runCommand(@NotNull CommandContext context, @NotNull ZombiesScene scene, @NotNull Player sender) {
        scene.setLegit(false);

        @Subst(Constants.NAMESPACE_OR_KEY)
        String flag = context.get(FLAG_ARGUMENT);

        if (keyParser.isValidKey(flag)) {
            Key key = keyParser.parseKey(flag);

            boolean res = scene.map().objects().module().flags().toggleFlag(key);
            sender.sendMessage("Toggled flag " + key + " to " + res);
        } else {
            sender.sendMessage("Invalid key " + flag);
        }
    }
}
