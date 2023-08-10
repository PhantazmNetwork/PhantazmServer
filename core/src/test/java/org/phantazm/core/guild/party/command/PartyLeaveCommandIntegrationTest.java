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
import org.phantazm.core.guild.party.PartyMember;
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
        PartyCreator partyCreator = new PartyCreator.Builder().setCreatorRank(1).build();
        Command command = PartyCommand.partyCommand(commandConfig, MiniMessage.miniMessage(), partyHolder,
                viewProvider, partyCreator, new Random(), 1, 0);
        env.process().command().register(command);
        Instance instance = env.createFlatInstance();
        Player player = env.createPlayer(instance, Pos.ZERO);
        env.process().command().execute(player, "party create");
        Party party = partyHolder.uuidToGuild().get(player.getUuid());

        env.process().command().execute(player, "party leave");

        assertFalse(partyHolder.uuidToGuild().containsKey(player.getUuid()));
        assertFalse(party.getMemberManager().hasMember(player.getUuid()));
        assertNull(party.getOwner().get());
    }

    @SuppressWarnings({"UnstableApiUsage", "JUnitMalformedDeclaration"})
    @Test
    public void testOwnerIsNotNullAfterLeavingWithOtherMembersAndNewOwnerHasRankOfFormerOwner(Env env) {
        PlayerViewProvider viewProvider = new BasicPlayerViewProvider(identitySource, env.process().connection());
        PartyCreator partyCreator = new PartyCreator.Builder().setCreatorRank(1).build();
        Command command = PartyCommand.partyCommand(commandConfig, MiniMessage.miniMessage(), partyHolder,
                viewProvider, partyCreator, new Random(), 1, 0);
        env.process().command().register(command);
        Instance instance = env.createFlatInstance();
        Player firstPlayer = env.createPlayer(instance, Pos.ZERO);
        firstPlayer.setUsernameField("first");
        env.process().command().execute(firstPlayer, "party create");
        Player secondPlayer = env.createPlayer(instance, Pos.ZERO);
        Party party = partyHolder.uuidToGuild().get(firstPlayer.getUuid());
        int oldRank = party.getOwner().get().rank();
        secondPlayer.setUsernameField("second");
        env.process().command().execute(firstPlayer, "party invite second");
        env.process().command().execute(secondPlayer, "party join first");

        env.process().command().execute(firstPlayer, "party leave");

        assertFalse(partyHolder.uuidToGuild().containsKey(firstPlayer.getUuid()));
        assertFalse(party.getMemberManager().hasMember(firstPlayer.getUuid()));
        PartyMember owner = party.getOwner().get();
        assertNotNull(owner);
        assertEquals(oldRank, owner.rank());
        assertNotEquals(firstPlayer.getUuid(), party.getOwner().get().getPlayerView().getUUID());
    }

}
