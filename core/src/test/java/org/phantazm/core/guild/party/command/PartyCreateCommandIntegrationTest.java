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
import static org.junit.jupiter.api.Assertions.assertNotNull;

@EnvTest
public class PartyCreateCommandIntegrationTest extends AbstractPartyCommandIntegrationTest {

    @SuppressWarnings({"UnstableApiUsage", "JUnitMalformedDeclaration"})
    @Test
    public void testCreateCreatesParty(Env env) {
        PlayerViewProvider viewProvider = new BasicPlayerViewProvider(identitySource, env.process().connection());
        PartyCreator partyCreator = new PartyCreator.Builder().setCreatorRank(1).build();
        Command command = PartyCommand.partyCommand(commandConfig, env.process().connection(), MiniMessage.miniMessage(), partyHolder,
                viewProvider,
                partyCreator, new Random(), 1, 0);
        env.process().command().register(command);
        Instance instance = env.createFlatInstance();
        Player player = env.createPlayer(instance, Pos.ZERO);

        env.process().command().execute(player, "party create");

        Party party = partyHolder.uuidToGuild().get(player.getUuid());
        assertNotNull(party);
        assertEquals(1, partyHolder.guilds().size());
        assertEquals(party, partyHolder.guilds().iterator().next());
    }

    @SuppressWarnings({"UnstableApiUsage", "JUnitMalformedDeclaration"})
    @Test
    public void testCreateDoesNotCreatePartyIfAlreadyInParty(Env env) {
        PlayerViewProvider viewProvider = new BasicPlayerViewProvider(identitySource, env.process().connection());
        PartyCreator partyCreator = new PartyCreator.Builder().setCreatorRank(1).build();
        Command command = PartyCommand.partyCommand(commandConfig, env.process().connection(), MiniMessage.miniMessage(), partyHolder, viewProvider,
                partyCreator, new Random(), 1, 0);
        env.process().command().register(command);
        Instance instance = env.createFlatInstance();
        Player player = env.createPlayer(instance, Pos.ZERO);
        env.process().command().execute(player, "party create");
        Party party = partyHolder.uuidToGuild().get(player.getUuid());

        env.process().command().execute(player, "party create");

        assertEquals(party, partyHolder.uuidToGuild().get(player.getUuid()));
    }

}
