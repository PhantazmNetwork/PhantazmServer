package org.phantazm.server;

import com.github.steanky.element.core.context.ContextManager;
import com.github.steanky.ethylene.core.ConfigCodec;
import com.github.steanky.ethylene.core.ConfigElement;
import com.github.steanky.ethylene.core.bridge.Configuration;
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
import org.phantazm.core.guild.party.PartyMember;
import org.phantazm.core.guild.party.command.PartyCommand;
import org.phantazm.core.player.PlayerViewProvider;
import org.phantazm.core.time.TickFormatter;

import java.io.IOException;
import java.util.*;

public class PartyFeature {

    private static final GuildHolder<Party> partyHolder = new GuildHolder<>(new HashMap<>(), new ArrayList<>());

    private static PartyConfig config;

    private PartyFeature() {
        throw new UnsupportedOperationException();
    }

    static void initialize(@NotNull CommandManager commandManager, @NotNull PlayerViewProvider viewProvider,
            @NotNull SchedulerManager schedulerManager, @NotNull ContextManager contextManager,
            @NotNull PartyConfig config, @NotNull ConfigCodec partyCodec) {
        PartyFeature.config = config;

        TickFormatter tickFormatter;
        try {
            ConfigElement partyConfigNode = Configuration.read(ConfigFeature.PARTY_CONFIG_PATH, partyCodec);
            tickFormatter = contextManager.makeContext(partyConfigNode.getNodeOrThrow("tickFormatter")).provide();
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }

        MiniMessage miniMessage = MiniMessage.miniMessage();
        PartyCreator partyCreator = new PartyCreator.Builder().setNotificationConfig(config.notificationConfig())
                .setTickFormatter(tickFormatter).setMiniMessage(miniMessage).setCreatorRank(config.creatorRank())
                .setDefaultRank(config.defaultRank()).setInvitationDuration(config.invitationDuration())
                .setMinimumKickRank(config.minimumKickRank()).setMinimumInviteRank(config.minimumInviteRank())
                .setMinimumJoinRank(config.minimumJoinRank()).build();
        Command partyCommand =
                PartyCommand.partyCommand(config.commandConfig(), miniMessage, partyHolder, viewProvider, partyCreator,
                        new Random(), config.creatorRank());
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
