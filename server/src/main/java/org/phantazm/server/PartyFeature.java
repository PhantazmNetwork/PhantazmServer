package org.phantazm.server;

import net.minestom.server.command.CommandManager;
import net.minestom.server.command.builder.Command;
import net.minestom.server.timer.SchedulerManager;
import net.minestom.server.timer.TaskSchedule;
import org.jetbrains.annotations.NotNull;
import org.phantazm.core.guild.GuildHolder;
import org.phantazm.core.guild.party.Party;
import org.phantazm.core.guild.party.PartyCreator;
import org.phantazm.core.guild.party.command.PartyCommand;
import org.phantazm.core.player.PlayerViewProvider;

import java.util.*;

public class PartyFeature {

    private static final GuildHolder<Party> partyHolder = new GuildHolder<>(new HashMap<>(), new ArrayList<>());

    private PartyFeature() {
        throw new UnsupportedOperationException();
    }

    public static void initialize(@NotNull CommandManager commandManager, @NotNull PlayerViewProvider viewProvider,
            @NotNull SchedulerManager schedulerManager) {
        PartyCreator partyCreator = new PartyCreator(1, 0, 400, 1, 1, 1);
        Command partyCommand = PartyCommand.partyCommand(partyHolder, viewProvider, partyCreator, new Random());
        commandManager.register(partyCommand);

        schedulerManager.scheduleTask(() -> {
            Set<Party> ticked = Collections.newSetFromMap(new IdentityHashMap<>());
            long time = System.currentTimeMillis();
            for (Party party : partyHolder.guilds()) {
                if (ticked.add(party)) {
                    party.tick(time);
                }
            }
        }, TaskSchedule.immediate(), TaskSchedule.nextTick());
    }

    public static @NotNull GuildHolder<Party> getPartyHolder() {
        return partyHolder;
    }
}
