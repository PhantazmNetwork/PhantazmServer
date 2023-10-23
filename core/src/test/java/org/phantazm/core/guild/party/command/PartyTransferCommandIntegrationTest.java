package org.phantazm.core.guild.party.command;

import net.kyori.adventure.text.minimessage.MiniMessage;
import net.minestom.server.command.builder.Command;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.Instance;
import net.minestom.testing.Env;
import net.minestom.testing.EnvTest;
import org.junit.jupiter.api.Test;
import org.phantazm.core.guild.party.Party;
import org.phantazm.core.guild.party.PartyCreator;
import org.phantazm.core.player.BasicPlayerViewProvider;
import org.phantazm.core.player.PlayerViewProvider;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;

@EnvTest
public class PartyTransferCommandIntegrationTest extends AbstractPartyCommandIntegrationTest {

    @SuppressWarnings("UnstableApiUsage")
    @Test
    public void partyOwnerCanTransferToOtherPlayer(Env env) {
        PlayerViewProvider viewProvider = new BasicPlayerViewProvider(identitySource, env.process().connection());
        PartyCreator partyCreator = new PartyCreator.Builder().setCreatorRank(1).build();
        Command command = PartyCommand.partyCommand(commandConfig, env.process().connection(), MiniMessage.miniMessage(), partyHolder,
            viewProvider, partyCreator, new Random(), 1, 0);
        env.process().command().register(command);
        Instance instance = env.createFlatInstance();
        Player firstPlayer = env.createPlayer(instance, Pos.ZERO);
        firstPlayer.setUsernameField("first");
        env.process().command().execute(firstPlayer, "party create");
        Party party = partyHolder.uuidToGuild().get(firstPlayer.getUuid());
        Player secondPlayer = env.createPlayer(instance, Pos.ZERO);
        secondPlayer.setUsernameField("second");
        env.process().command().execute(firstPlayer, "party invite second");
        env.process().command().execute(secondPlayer, "party join first");

        env.process().command().execute(firstPlayer, "party transfer second");

        assertEquals(secondPlayer.getUuid(), party.getOwner().get().getPlayerView().getUUID());
        assertEquals(0, party.getMemberManager().getMember(firstPlayer.getUuid()).rank());
        assertEquals(1, party.getMemberManager().getMember(secondPlayer.getUuid()).rank());
    }

    @SuppressWarnings("UnstableApiUsage")
    @Test
    public void partyOwnerCannotTransferToSelf(Env env) {
        PlayerViewProvider viewProvider = new BasicPlayerViewProvider(identitySource, env.process().connection());
        PartyCreator partyCreator = new PartyCreator.Builder().setCreatorRank(1).build();
        Command command = PartyCommand.partyCommand(commandConfig, env.process().connection(), MiniMessage.miniMessage(), partyHolder,
            viewProvider, partyCreator, new Random(), 1, 0);
        env.process().command().register(command);
        Instance instance = env.createFlatInstance();
        Player firstPlayer = env.createPlayer(instance, Pos.ZERO);
        firstPlayer.setUsernameField("first");
        env.process().command().execute(firstPlayer, "party create");
        Party party = partyHolder.uuidToGuild().get(firstPlayer.getUuid());
        Player secondPlayer = env.createPlayer(instance, Pos.ZERO);
        secondPlayer.setUsernameField("second");
        env.process().command().execute(firstPlayer, "party invite second");
        env.process().command().execute(secondPlayer, "party join first");

        env.process().command().execute(firstPlayer, "party transfer first");

        assertEquals(firstPlayer.getUuid(), party.getOwner().get().getPlayerView().getUUID());
        assertEquals(1, party.getMemberManager().getMember(firstPlayer.getUuid()).rank());
        assertEquals(0, party.getMemberManager().getMember(secondPlayer.getUuid()).rank());
    }

    @SuppressWarnings("UnstableApiUsage")
    @Test
    public void nonOwnerCannotTransferToOtherPlayer(Env env) {
        PlayerViewProvider viewProvider = new BasicPlayerViewProvider(identitySource, env.process().connection());
        PartyCreator partyCreator = new PartyCreator.Builder().setCreatorRank(1).build();
        Command command = PartyCommand.partyCommand(commandConfig, env.process().connection(), MiniMessage.miniMessage(), partyHolder,
            viewProvider, partyCreator, new Random(), 1, 0);
        env.process().command().register(command);
        Instance instance = env.createFlatInstance();
        Player firstPlayer = env.createPlayer(instance, Pos.ZERO);
        firstPlayer.setUsernameField("first");
        env.process().command().execute(firstPlayer, "party create");
        Party party = partyHolder.uuidToGuild().get(firstPlayer.getUuid());
        Player secondPlayer = env.createPlayer(instance, Pos.ZERO);
        secondPlayer.setUsernameField("second");
        env.process().command().execute(firstPlayer, "party invite second");
        env.process().command().execute(secondPlayer, "party join first");
        Player thirdPlayer = env.createPlayer(instance, Pos.ZERO);
        thirdPlayer.setUsernameField("third");
        env.process().command().execute(firstPlayer, "party invite third");
        env.process().command().execute(thirdPlayer, "party join first");

        env.process().command().execute(secondPlayer, "party transfer third");

        assertEquals(firstPlayer.getUuid(), party.getOwner().get().getPlayerView().getUUID());
        assertEquals(1, party.getMemberManager().getMember(firstPlayer.getUuid()).rank());
        assertEquals(0, party.getMemberManager().getMember(secondPlayer.getUuid()).rank());
        assertEquals(0, party.getMemberManager().getMember(secondPlayer.getUuid()).rank());
    }

}
