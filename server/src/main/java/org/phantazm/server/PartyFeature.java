package org.phantazm.server;

import net.minestom.server.command.CommandManager;
import net.minestom.server.command.builder.Command;
import net.minestom.server.timer.SchedulerManager;
import net.minestom.server.timer.TaskSchedule;
import org.jetbrains.annotations.NotNull;
import org.phantazm.core.guild.party.Party;
import org.phantazm.core.guild.party.PartyCreator;
import org.phantazm.core.guild.party.command.PartyCommand;
import org.phantazm.core.player.PlayerViewProvider;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

public class PartyFeature {

    private static final Map<? super UUID, Party> parties = new HashMap<>();

    private PartyFeature() {
    }

    public static void initialize(@NotNull CommandManager commandManager, @NotNull PlayerViewProvider viewProvider,
            @NotNull SchedulerManager schedulerManager) {
        PartyCreator partyCreator = new PartyCreator(1, 0, 20, 1, 1);
        Command partyCommand = PartyCommand.partyCommand(parties, viewProvider, partyCreator, new Random());
        commandManager.register(partyCommand);

        schedulerManager.scheduleTask(() -> {
            for (Party party : parties.values()) {
                party.tick(System.currentTimeMillis());
            }
        }, TaskSchedule.immediate(), TaskSchedule.nextTick());
    }

    public static @NotNull Map<? super UUID, Party> getParties() {
        return parties;
    }
}
