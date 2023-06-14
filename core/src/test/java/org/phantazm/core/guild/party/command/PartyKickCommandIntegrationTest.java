package org.phantazm.core.guild.party.command;

import net.minestom.server.command.builder.Command;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.Instance;
import net.minestom.testing.Env;
import org.junit.jupiter.api.Test;
import org.phantazm.core.guild.party.Party;
import org.phantazm.core.guild.party.PartyCreator;
import org.phantazm.core.player.BasicPlayerViewProvider;
import org.phantazm.core.player.PlayerViewProvider;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

public class PartyKickCommandIntegrationTest extends AbstractPartyCommandIntegrationTest {

    @SuppressWarnings({"UnstableApiUsage", "JUnitMalformedDeclaration"})
    @Test
    public void testCanKickWithSufficientRankAndGreaterRankThanTarget(Env env) {
        PlayerViewProvider viewProvider = new BasicPlayerViewProvider(identitySource, env.process().connection());
        PartyCreator partyCreator = new PartyCreator(1, 0, 20, 1, 1);
        Command command = PartyCommand.command(parties, viewProvider, partyCreator, new Random());
        env.process().command().register(command);
        Instance instance = env.createFlatInstance();
        Player firstPlayer = env.createPlayer(instance, Pos.ZERO);
        firstPlayer.setUsernameField("first");
        env.process().command().execute(firstPlayer, "party create");
        Party party = parties.get(firstPlayer.getUuid());
        Player secondPlayer = env.createPlayer(instance, Pos.ZERO);
        secondPlayer.setUsernameField("second");
        env.process().command().execute(firstPlayer, "party invite second");
        env.process().command().execute(secondPlayer, "party join first");

        env.process().command().execute(firstPlayer, "party kick second");

        assertFalse(parties.containsKey(secondPlayer.getUuid()));
        assertFalse(party.getMemberManager().hasMember(secondPlayer.getUuid()));
    }

    @SuppressWarnings({"UnstableApiUsage", "JUnitMalformedDeclaration"})
    @Test
    public void testCannotKickWithoutSufficientRankAndGreaterRankThanTarget(Env env) {
        PlayerViewProvider viewProvider = new BasicPlayerViewProvider(identitySource, env.process().connection());
        PartyCreator partyCreator = new PartyCreator(1, 0, 20, 2, 1);
        Command command = PartyCommand.command(parties, viewProvider, partyCreator, new Random());
        env.process().command().register(command);
        Instance instance = env.createFlatInstance();
        Player firstPlayer = env.createPlayer(instance, Pos.ZERO);
        firstPlayer.setUsernameField("first");
        env.process().command().execute(firstPlayer, "party create");
        Party party = parties.get(firstPlayer.getUuid());
        Player secondPlayer = env.createPlayer(instance, Pos.ZERO);
        secondPlayer.setUsernameField("second");
        env.process().command().execute(firstPlayer, "party invite second");
        env.process().command().execute(secondPlayer, "party join first");

        env.process().command().execute(firstPlayer, "party kick second");

        assertEquals(party, parties.get(secondPlayer.getUuid()));
        assertTrue(party.getMemberManager().hasMember(secondPlayer.getUuid()));
    }

    @SuppressWarnings({"UnstableApiUsage", "JUnitMalformedDeclaration"})
    @Test
    public void testCannotKickWithSufficientRankAndEqualRankToTarget(Env env) {
        PlayerViewProvider viewProvider = new BasicPlayerViewProvider(identitySource, env.process().connection());
        PartyCreator partyCreator = new PartyCreator(1, 0, 20, 2, 1);
        Command command = PartyCommand.command(parties, viewProvider, partyCreator, new Random());
        env.process().command().register(command);
        Instance instance = env.createFlatInstance();
        Player firstPlayer = env.createPlayer(instance, Pos.ZERO);
        firstPlayer.setUsernameField("first");
        env.process().command().execute(firstPlayer, "party create");
        Party party = parties.get(firstPlayer.getUuid());
        Player secondPlayer = env.createPlayer(instance, Pos.ZERO);
        secondPlayer.setUsernameField("second");
        env.process().command().execute(firstPlayer, "party invite second");
        env.process().command().execute(secondPlayer, "party join first");
        party.getMemberManager().getMember(secondPlayer.getUuid()).setRank(1);

        env.process().command().execute(firstPlayer, "party kick second");

        assertEquals(party, parties.get(secondPlayer.getUuid()));
        assertTrue(party.getMemberManager().hasMember(secondPlayer.getUuid()));
    }

    @SuppressWarnings({"UnstableApiUsage", "JUnitMalformedDeclaration"})
    @Test
    public void testCannotKickSelf(Env env) {
        PlayerViewProvider viewProvider = new BasicPlayerViewProvider(identitySource, env.process().connection());
        PartyCreator partyCreator = new PartyCreator(1, 0, 20, 2, 1);
        Command command = PartyCommand.command(parties, viewProvider, partyCreator, new Random());
        env.process().command().register(command);
        Instance instance = env.createFlatInstance();
        Player player = env.createPlayer(instance, Pos.ZERO);
        env.process().command().execute(player, "party create");
        Party party = parties.get(player.getUuid());

        env.process().command().execute(player, "party kick " + player.getUsername());

        assertEquals(party, parties.get(player.getUuid()));
        assertTrue(party.getMemberManager().hasMember(player.getUuid()));
    }

}
