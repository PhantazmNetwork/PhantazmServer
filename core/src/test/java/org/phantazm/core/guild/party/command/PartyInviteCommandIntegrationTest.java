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

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@EnvTest
public class PartyInviteCommandIntegrationTest extends AbstractPartyCommandIntegrationTest {

    @SuppressWarnings({"UnstableApiUsage", "JUnitMalformedDeclaration"})
    @Test
    public void testCanInviteWithInvitePermission(Env env) {
        PlayerViewProvider viewProvider = new BasicPlayerViewProvider(identitySource, env.process().connection());
        PartyCreator partyCreator = new PartyCreator(1, 0, 20, 1, 1);
        Command command = PartyCommand.command(parties, viewProvider, partyCreator);
        env.process().command().register(command);
        Instance instance = env.createFlatInstance();
        Player firstPlayer = env.createPlayer(instance, Pos.ZERO);
        firstPlayer.setUsernameField("first");
        env.process().command().execute(firstPlayer, "party create");
        Party party = parties.get(firstPlayer.getUuid());
        Player secondPlayer = env.createPlayer(instance, Pos.ZERO);
        secondPlayer.setUsernameField("second");

        env.process().command().execute(firstPlayer, "party invite second");

        assertTrue(party.getInvitationManager().hasInvitation(secondPlayer.getUuid()));
    }

    @SuppressWarnings({"UnstableApiUsage", "JUnitMalformedDeclaration"})
    @Test
    public void testCanInviteOtherPlayerInPartyWithInvitePermission(Env env) {
        PlayerViewProvider viewProvider = new BasicPlayerViewProvider(identitySource, env.process().connection());
        PartyCreator partyCreator = new PartyCreator(1, 0, 20, 1, 1);
        Command command = PartyCommand.command(parties, viewProvider, partyCreator);
        env.process().command().register(command);
        Instance instance = env.createFlatInstance();
        Player firstPlayer = env.createPlayer(instance, Pos.ZERO);
        firstPlayer.setUsernameField("first");
        env.process().command().execute(firstPlayer, "party create");
        Party party = parties.get(firstPlayer.getUuid());
        Player secondPlayer = env.createPlayer(instance, Pos.ZERO);
        secondPlayer.setUsernameField("second");
        env.process().command().execute(secondPlayer, "party create");

        env.process().command().execute(firstPlayer, "party invite second");

        assertTrue(party.getInvitationManager().hasInvitation(secondPlayer.getUuid()));
    }

    @SuppressWarnings({"UnstableApiUsage", "JUnitMalformedDeclaration"})
    @Test
    public void testCannotInviteWithoutInvitePermission(Env env) {
        PlayerViewProvider viewProvider = new BasicPlayerViewProvider(identitySource, env.process().connection());
        PartyCreator partyCreator = new PartyCreator(1, 0, 20, 1, 2);
        Command command = PartyCommand.command(parties, viewProvider, partyCreator);
        env.process().command().register(command);
        Instance instance = env.createFlatInstance();
        Player firstPlayer = env.createPlayer(instance, Pos.ZERO);
        firstPlayer.setUsernameField("first");
        env.process().command().execute(firstPlayer, "party create");
        Party party = parties.get(firstPlayer.getUuid());
        Player secondPlayer = env.createPlayer(instance, Pos.ZERO);
        secondPlayer.setUsernameField("second");

        env.process().command().execute(firstPlayer, "party invite second");

        assertFalse(party.getInvitationManager().hasInvitation(secondPlayer.getUuid()));
    }

    @SuppressWarnings({"UnstableApiUsage", "JUnitMalformedDeclaration"})
    @Test
    public void testCannotInviteSelf(Env env) {
        PlayerViewProvider viewProvider = new BasicPlayerViewProvider(identitySource, env.process().connection());
        PartyCreator partyCreator = new PartyCreator(1, 0, 20, 1, 1);
        Command command = PartyCommand.command(parties, viewProvider, partyCreator);
        env.process().command().register(command);
        Instance instance = env.createFlatInstance();
        Player player = env.createPlayer(instance, Pos.ZERO);
        env.process().command().execute(player, "party create");
        Party party = parties.get(player.getUuid());

        env.process().command().execute(player, "party invite " + player.getUsername());

        assertFalse(party.getInvitationManager().hasInvitation(player.getUuid()));
    }

}
