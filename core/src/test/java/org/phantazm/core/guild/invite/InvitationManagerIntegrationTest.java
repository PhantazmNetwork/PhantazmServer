package org.phantazm.core.guild.invite;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.Instance;
import net.minestom.testing.Env;
import net.minestom.testing.EnvTest;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.phantazm.core.guild.GuildMember;
import org.phantazm.core.guild.GuildMemberManager;
import org.phantazm.core.player.BasicPlayerViewProvider;
import org.phantazm.core.player.IdentitySource;
import org.phantazm.core.player.PlayerView;
import org.phantazm.core.player.PlayerViewProvider;

import java.util.HashMap;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.function.IntFunction;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@EnvTest
public class InvitationManagerIntegrationTest {

    private IdentitySource identitySource;

    private GuildMemberManager<GuildMember> memberManager;

    private Function<? super PlayerView, ? extends GuildMember> memberCreator;

    private IntFunction<InvitationManager<GuildMember>> invitationManagerCreator;

    @BeforeEach
    public void setup() {
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

        memberManager = new GuildMemberManager<>(new HashMap<>());
        memberCreator = GuildMember::new;
        InvitationNotification<GuildMember> notification = new InvitationNotification<>() {
            @Override
            public void notifyJoin(@NotNull GuildMember member) {

            }

            @Override
            public void notifyInvitation(@NotNull GuildMember inviter, @NotNull PlayerView invitee,
                long invitationDuration) {

            }

            @Override
            public void notifyExpiry(@NotNull PlayerView invitee) {

            }
        };

        invitationManagerCreator = (duration) -> {
            return new InvitationManager<>(memberManager, memberCreator, notification, duration);
        };
    }

    @SuppressWarnings({"UnstableApiUsage", "JUnitMalformedDeclaration"})
    @Test
    public void testHasInviteAfterInvite(Env env) {
        PlayerViewProvider viewProvider = new BasicPlayerViewProvider(identitySource, env.process().connection());
        InvitationManager<GuildMember> invitationManager = invitationManagerCreator.apply(1);
        Instance instance = env.createFlatInstance();
        Player firstPlayer = env.createPlayer(instance, Pos.ZERO);
        firstPlayer.setUsernameField("first");
        GuildMember guildMember = memberCreator.apply(viewProvider.fromPlayer(firstPlayer));
        Player secondPlayer = env.createPlayer(instance, Pos.ZERO);
        secondPlayer.setUsernameField("second");

        invitationManager.invite(guildMember, viewProvider.fromPlayer(secondPlayer));

        assertTrue(invitationManager.hasInvitation(secondPlayer.getUuid()));
    }

    @SuppressWarnings({"UnstableApiUsage", "JUnitMalformedDeclaration"})
    @Test
    public void testDoesNotHaveInviteAfterInviteExpired(Env env) {
        PlayerViewProvider viewProvider = new BasicPlayerViewProvider(identitySource, env.process().connection());
        InvitationManager<GuildMember> invitationManager = invitationManagerCreator.apply(1);
        Instance instance = env.createFlatInstance();
        Player firstPlayer = env.createPlayer(instance, Pos.ZERO);
        firstPlayer.setUsernameField("first");
        GuildMember guildMember = memberCreator.apply(viewProvider.fromPlayer(firstPlayer));
        Player secondPlayer = env.createPlayer(instance, Pos.ZERO);
        secondPlayer.setUsernameField("second");

        invitationManager.invite(guildMember, viewProvider.fromPlayer(secondPlayer));
        invitationManager.tick(System.currentTimeMillis());

        assertFalse(invitationManager.hasInvitation(secondPlayer.getUuid()));
    }

    @SuppressWarnings({"UnstableApiUsage", "JUnitMalformedDeclaration"})
    @Test
    public void testInviteIsExpiredWithZeroExpirtyTime(Env env) {
        PlayerViewProvider viewProvider = new BasicPlayerViewProvider(identitySource, env.process().connection());
        InvitationManager<GuildMember> invitationManager = invitationManagerCreator.apply(0);
        Instance instance = env.createFlatInstance();
        Player firstPlayer = env.createPlayer(instance, Pos.ZERO);
        firstPlayer.setUsernameField("first");
        GuildMember guildMember = memberCreator.apply(viewProvider.fromPlayer(firstPlayer));
        Player secondPlayer = env.createPlayer(instance, Pos.ZERO);
        secondPlayer.setUsernameField("second");

        invitationManager.invite(guildMember, viewProvider.fromPlayer(secondPlayer));

        assertFalse(invitationManager.hasInvitation(secondPlayer.getUuid()));
    }

    @SuppressWarnings({"UnstableApiUsage", "JUnitMalformedDeclaration"})
    @Test
    public void testDoesNotHaveInvitationAndInPartyAfterInviteAccepted(Env env) {
        PlayerViewProvider viewProvider = new BasicPlayerViewProvider(identitySource, env.process().connection());
        InvitationManager<GuildMember> invitationManager = invitationManagerCreator.apply(1);
        Instance instance = env.createFlatInstance();
        Player firstPlayer = env.createPlayer(instance, Pos.ZERO);
        firstPlayer.setUsernameField("first");
        GuildMember guildMember = memberCreator.apply(viewProvider.fromPlayer(firstPlayer));
        Player secondPlayer = env.createPlayer(instance, Pos.ZERO);
        secondPlayer.setUsernameField("second");
        PlayerView invitee = viewProvider.fromPlayer(secondPlayer);
        invitationManager.invite(guildMember, invitee);

        invitationManager.acceptInvitation(invitee);

        assertFalse(invitationManager.hasInvitation(secondPlayer.getUuid()));
        assertTrue(memberManager.hasMember(secondPlayer.getUuid()));
    }

    @SuppressWarnings({"UnstableApiUsage", "JUnitMalformedDeclaration"})
    @Test
    public void testStillHasInvitationAfterSecondInvitationMadeAndFirstInvitationExpired(Env env) {
        PlayerViewProvider viewProvider = new BasicPlayerViewProvider(identitySource, env.process().connection());
        InvitationManager<GuildMember> invitationManager = invitationManagerCreator.apply(2);
        Instance instance = env.createFlatInstance();
        Player firstPlayer = env.createPlayer(instance, Pos.ZERO);
        firstPlayer.setUsernameField("first");
        GuildMember guildMember = memberCreator.apply(viewProvider.fromPlayer(firstPlayer));
        Player secondPlayer = env.createPlayer(instance, Pos.ZERO);
        secondPlayer.setUsernameField("second");
        invitationManager.invite(guildMember, viewProvider.fromPlayer(secondPlayer));

        invitationManager.tick(System.currentTimeMillis());
        invitationManager.invite(guildMember, viewProvider.fromPlayer(secondPlayer));
        invitationManager.tick(System.currentTimeMillis());

        assertTrue(invitationManager.hasInvitation(secondPlayer.getUuid()));
    }

}
