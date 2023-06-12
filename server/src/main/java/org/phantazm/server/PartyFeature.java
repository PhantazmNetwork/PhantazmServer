package org.phantazm.server;

import net.minestom.server.command.CommandManager;
import org.jetbrains.annotations.NotNull;
import org.phantazm.core.guild.party.Party;
import org.phantazm.core.guild.party.PartyCreator;
import org.phantazm.core.guild.party.command.PartyCommand;
import org.phantazm.core.player.PlayerViewProvider;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PartyFeature {

    private static final Map<? super UUID, Party> parties = new HashMap<>();

    public static void initialize(@NotNull CommandManager commandManager, @NotNull PlayerViewProvider viewProvider) {
        commandManager.register(PartyCommand.command(parties, viewProvider, new PartyCreator(1, 1, 0)));
    }

    public static @NotNull Map<? super UUID, Party> getParties() {
        return parties;
    }
}
