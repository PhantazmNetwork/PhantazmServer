package org.phantazm.server;

import com.github.steanky.element.core.context.ContextManager;
import com.github.steanky.ethylene.core.ConfigCodec;
import com.github.steanky.ethylene.core.ConfigElement;
import com.github.steanky.ethylene.core.bridge.Configuration;
import com.github.steanky.ethylene.core.processor.ConfigProcessor;
import com.github.steanky.ethylene.mapper.MappingProcessorSource;
import com.github.steanky.ethylene.mapper.type.Token;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.minestom.server.command.CommandManager;
import net.minestom.server.command.builder.Command;
import net.minestom.server.timer.SchedulerManager;
import net.minestom.server.timer.TaskSchedule;
import org.jetbrains.annotations.NotNull;
import org.phantazm.core.guild.GuildHolder;
import org.phantazm.core.guild.party.Party;
import org.phantazm.core.guild.party.PartyConfig;
import org.phantazm.core.guild.party.PartyCreator;
import org.phantazm.core.guild.party.command.PartyCommand;
import org.phantazm.core.player.PlayerViewProvider;
import org.phantazm.core.time.TickFormatter;

import java.io.IOException;
import java.nio.file.Path;
import java.util.*;

public class PartyFeature {

    private static final GuildHolder<Party> partyHolder = new GuildHolder<>(new HashMap<>(), new ArrayList<>());

    private PartyFeature() {
        throw new UnsupportedOperationException();
    }

    static void initialize(@NotNull CommandManager commandManager, @NotNull PlayerViewProvider viewProvider,
            @NotNull SchedulerManager schedulerManager, @NotNull MappingProcessorSource mappingProcessorSource,
            @NotNull ContextManager contextManager, @NotNull ConfigCodec partyCodec, @NotNull MiniMessage miniMessage)
            throws IOException {
        ConfigProcessor<PartyConfig> partyConfigProcessor =
                mappingProcessorSource.processorFor(Token.ofClass(PartyConfig.class));
        String partyFileName =
                partyCodec.getPreferredExtensions().isEmpty() ? "party" : "party." + partyCodec.getPreferredExtension();
        ConfigElement partyConfigNode = Configuration.read(Path.of(partyFileName), partyCodec);
        TickFormatter tickFormatter =
                contextManager.makeContext(partyConfigNode.getNodeOrThrow("tickFormatter")).provide();
        PartyConfig partyConfig = partyConfigProcessor.dataFromElement(partyConfigNode);

        PartyCreator partyCreator = new PartyCreator.Builder().setNotificationConfig(partyConfig.notificationConfig())
                .setTickFormatter(tickFormatter).setMiniMessage(miniMessage).setCreatorRank(partyConfig.creatorRank())
                .setDefaultRank(partyConfig.defaultRank()).setInvitationDuration(partyConfig.invitationDuration())
                .setMinimumKickRank(partyConfig.minimumKickRank()).setMinimumInviteRank(partyConfig.minimumInviteRank())
                .setMinimumJoinRank(partyConfig.minimumJoinRank()).build();
        Command partyCommand =
                PartyCommand.partyCommand(partyConfig.commandConfig(), miniMessage, partyHolder, viewProvider,
                        partyCreator, new Random());
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
