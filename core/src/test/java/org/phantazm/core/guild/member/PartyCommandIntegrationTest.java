package org.phantazm.core.guild.member;

import net.minestom.server.command.builder.Command;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.Instance;
import net.minestom.testing.Env;
import net.minestom.testing.EnvTest;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.phantazm.core.guild.party.Party;
import org.phantazm.core.guild.party.PartyCreator;
import org.phantazm.core.guild.party.command.PartyCommand;
import org.phantazm.core.player.BasicPlayerViewProvider;
import org.phantazm.core.player.IdentitySource;
import org.phantazm.core.player.PlayerViewProvider;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;

@EnvTest
public class PartyCommandIntegrationTest {

    private Map<? super UUID, Party> parties;

    private IdentitySource identitySource;

    private PartyCreator partyCreator;

    @BeforeEach
    public void setup() {
        parties = new HashMap<>();
        identitySource = new IdentitySource() {
            @Override
            public @NotNull CompletableFuture<Optional<String>> getName(@NotNull UUID uuid) {
                return CompletableFuture.completedFuture(Optional.empty());
            }

            @Override
            public @NotNull CompletableFuture<Optional<UUID>> getUUID(@NotNull String name) {
                return CompletableFuture.completedFuture(Optional.empty());
            }
        };
        partyCreator = new PartyCreator(1, 1, 0);
    }

    @SuppressWarnings("UnstableApiUsage")
    @Test
    public void testCreateCreatesParty(Env env) {
        PlayerViewProvider viewProvider = new BasicPlayerViewProvider(identitySource, env.process().connection());
        Command command = PartyCommand.command(parties, viewProvider, partyCreator);
        env.process().command().register(command);
        Instance instance = env.createFlatInstance();
        Player player = env.createPlayer(instance, Pos.ZERO);

        env.process().command().execute(player, "party create");

        assertTrue(parties.containsKey(player.getUuid()));
    }

    @SuppressWarnings("UnstableApiUsage")
    @Test
    public void testNotInPartyAfterLeaving(Env env) {
        PlayerViewProvider viewProvider = new BasicPlayerViewProvider(identitySource, env.process().connection());
        Command command = PartyCommand.command(parties, viewProvider, partyCreator);
        env.process().command().register(command);
        Instance instance = env.createFlatInstance();
        Player player = env.createPlayer(instance, Pos.ZERO);
        env.process().command().execute(player, "party create");
        Party party = parties.get(player.getUuid());

        env.process().command().execute(player, "party leave");

        assertFalse(parties.containsKey(player.getUuid()));
        assertFalse(party.getMemberManager().hasMember(player.getUuid()));
    }

    @SuppressWarnings("UnstableApiUsage")
    @Test
    public void testNotInPartyAfterJoiningOtherPlayerNotInParty(Env env) {
        PlayerViewProvider viewProvider = new BasicPlayerViewProvider(identitySource, env.process().connection());
        Command command = PartyCommand.command(parties, viewProvider, partyCreator);
        env.process().command().register(command);
        Instance instance = env.createFlatInstance();
        Player firstPlayer = env.createPlayer(instance, Pos.ZERO);
        firstPlayer.setUsernameField("first");
        Player secondPlayer = env.createPlayer(instance, Pos.ZERO);
        secondPlayer.setUsernameField("second");

        env.process().command().execute(secondPlayer, "party join first");

        assertFalse(parties.containsKey(firstPlayer.getUuid()));
        assertFalse(parties.containsKey(secondPlayer.getUuid()));
    }

    @SuppressWarnings("UnstableApiUsage")
    @Test
    public void testInPartyAfterJoiningOtherPlayerInParty(Env env) {
        PlayerViewProvider viewProvider = new BasicPlayerViewProvider(identitySource, env.process().connection());
        Command command = PartyCommand.command(parties, viewProvider, partyCreator);
        env.process().command().register(command);
        Instance instance = env.createFlatInstance();
        Player firstPlayer = env.createPlayer(instance, Pos.ZERO);
        firstPlayer.setUsernameField("first");
        env.process().command().execute(firstPlayer, "party create");
        Party party = parties.get(firstPlayer.getUuid());
        Player secondPlayer = env.createPlayer(instance, Pos.ZERO);
        secondPlayer.setUsernameField("second");

        env.process().command().execute(secondPlayer, "party join first");

        assertEquals(party, parties.get(secondPlayer.getUuid()));
        assertTrue(party.getMemberManager().hasMember(secondPlayer.getUuid()));
    }

}
