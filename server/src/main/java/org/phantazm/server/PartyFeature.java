package org.phantazm.server;

import com.github.steanky.element.core.context.ContextManager;
import com.github.steanky.ethylene.core.ConfigElement;
import com.github.steanky.ethylene.core.bridge.Configuration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.minestom.server.MinecraftServer;
import net.minestom.server.command.CommandManager;
import net.minestom.server.command.builder.Command;
import net.minestom.server.timer.SchedulerManager;
import net.minestom.server.timer.TaskSchedule;
import org.jetbrains.annotations.NotNull;
import org.phantazm.core.guild.GuildHolder;
import org.phantazm.core.guild.party.Party;
import org.phantazm.core.guild.party.PartyConfig;
import org.phantazm.core.guild.party.PartyCreator;
import org.phantazm.core.guild.party.PartyMember;
import org.phantazm.core.guild.party.command.PartyCommand;
import org.phantazm.core.player.PlayerViewProvider;
import org.phantazm.core.time.TickFormatter;
import org.phantazm.server.context.ConfigContext;
import org.phantazm.server.context.DataLoadingContext;
import org.phantazm.server.context.EthyleneContext;

import java.io.IOException;
import java.util.*;

public class PartyFeature {

    private static final GuildHolder<Party> partyHolder =
        new GuildHolder<>(new HashMap<>(), Collections.newSetFromMap(new IdentityHashMap<>()));

    private static PartyConfig config;

    private PartyFeature() {
        throw new UnsupportedOperationException();
    }

    static void initialize(@NotNull ConfigContext configContext, @NotNull EthyleneContext ethyleneContext,
        @NotNull DataLoadingContext dataLoadingContext) {
        PartyFeature.config = configContext.partyConfig();

        ContextManager contextManager = dataLoadingContext.contextManager();
        PlayerViewProvider viewProvider = PlayerViewProvider.Global.instance();
        CommandManager commandManager = MinecraftServer.getCommandManager();
        SchedulerManager schedulerManager = MinecraftServer.getSchedulerManager();

        TickFormatter tickFormatter;
        try {
            ConfigElement partyConfigNode = Configuration.read(ConfigFeature.PARTY_CONFIG_PATH, ethyleneContext.tomlCodec());
            tickFormatter = contextManager.makeContext(partyConfigNode.getNodeOrThrow("tickFormatter")).provide();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        MiniMessage miniMessage = MiniMessage.miniMessage();
        PartyCreator partyCreator = new PartyCreator.Builder().setNotificationConfig(config.notificationConfig())
            .setTickFormatter(tickFormatter).setMiniMessage(miniMessage).setCreatorRank(config.creatorRank())
            .setDefaultRank(config.defaultRank()).setInvitationDuration(config.invitationDuration())
            .setMinimumKickRank(config.minimumKickRank()).setMinimumInviteRank(config.minimumInviteRank())
            .setMinimumAllInviteRank(config.minimumAllInviteRank())
            .setMinimumJoinRank(config.minimumJoinRank())
            .setMinimumWarpRank(config.minimumWarpRank())
            .build();
        Command partyCommand =
            PartyCommand.partyCommand(config.commandConfig(), MinecraftServer.getConnectionManager(), miniMessage,
                partyHolder, viewProvider, partyCreator, new Random(), config.creatorRank(),
                config.defaultRank());
        commandManager.register(partyCommand);

        schedulerManager.scheduleTask(() -> {
            partyHolder.guilds().removeIf(party -> {
                if (party.getMemberManager().getMembers().isEmpty()) {
                    return true;
                }

                for (PartyMember member : party.getMemberManager().getMembers().values()) {
                    if (member.isOnline()) {
                        return false;
                    }
                }

                for (PartyMember member : party.getMemberManager().getMembers().values()) {
                    partyHolder.uuidToGuild().remove(member.getPlayerView().getUUID());
                }

                return true;
            });

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

    public static @NotNull PartyConfig getConfig() {
        return FeatureUtils.check(config);
    }
}
