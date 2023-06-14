package org.phantazm.core.guild.party.command;

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

import static org.junit.jupiter.api.Assertions.*;

@EnvTest
public class PartyLeaveCommandIntegrationTest extends AbstractPartyCommandIntegrationTest {

    @SuppressWarnings({"UnstableApiUsage", "JUnitMalformedDeclaration"})
    @Test
    public void testNotInPartyAfterLeavingAndOwnerIsNullNoOtherMembers(Env env) {
        PlayerViewProvider viewProvider = new BasicPlayerViewProvider(identitySource, env.process().connection());
        PartyCreator partyCreator = new PartyCreator(1, 0, 20, 1, 1);
        Command command = PartyCommand.command(parties, viewProvider, partyCreator, new Random());
        env.process().command().register(command);
        Instance instance = env.createFlatInstance();
        Player player = env.createPlayer(instance, Pos.ZERO);
        env.process().command().execute(player, "party create");
        Party party = parties.get(player.getUuid());

        env.process().command().execute(player, "party leave");

        assertFalse(parties.containsKey(player.getUuid()));
        assertFalse(party.getMemberManager().hasMember(player.getUuid()));
        assertNull(party.getOwner().get());
    }

    @SuppressWarnings({"UnstableApiUsage", "JUnitMalformedDeclaration"})
    @Test
    public void testOwnerIsNotNullAfterLeavingWithOtherMembers(Env env) {
        PlayerViewProvider viewProvider = new BasicPlayerViewProvider(identitySource, env.process().connection());
        PartyCreator partyCreator = new PartyCreator(1, 0, 20, 1, 1);
        Command command = PartyCommand.command(parties, viewProvider, partyCreator, new Random());
        env.process().command().register(command);
        Instance instance = env.createFlatInstance();
        Player firstPlayer = env.createPlayer(instance, Pos.ZERO);
        firstPlayer.setUsernameField("first");
        env.process().command().execute(firstPlayer, "party create");
        Player secondPlayer = env.createPlayer(instance, Pos.ZERO);
        Party party = parties.get(firstPlayer.getUuid());
        secondPlayer.setUsernameField("second");
        env.process().command().execute(firstPlayer, "party invite second");
        env.process().command().execute(secondPlayer, "party join first");

        env.process().command().execute(firstPlayer, "party leave");

        assertFalse(parties.containsKey(firstPlayer.getUuid()));
        assertFalse(party.getMemberManager().hasMember(firstPlayer.getUuid()));
        assertNotNull(party.getOwner().get());
    }

}
