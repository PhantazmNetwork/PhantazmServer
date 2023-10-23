package org.phantazm.core.command;

import net.minestom.server.command.builder.condition.CommandCondition;
import net.minestom.server.entity.Player;
import org.jetbrains.annotations.NotNull;

public class CommandUtils {
    private static final CommandCondition PLAYER_CONDITION = (sender, commandString) -> sender instanceof Player;

    private CommandUtils() {
        throw new UnsupportedOperationException();
    }

    public static @NotNull CommandCondition playerSenderCondition() {
        return PLAYER_CONDITION;
    }
}
