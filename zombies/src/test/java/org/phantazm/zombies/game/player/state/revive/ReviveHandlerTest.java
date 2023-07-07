package org.phantazm.zombies.game.player.state.revive;

import net.minestom.server.coordinate.Vec;
import net.minestom.server.instance.Instance;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.phantazm.core.player.PlayerView;
import org.phantazm.stats.zombies.ZombiesPlayerMapStats;
import org.phantazm.zombies.player.ZombiesPlayer;
import org.phantazm.zombies.player.ZombiesPlayerMeta;
import org.phantazm.zombies.player.ZombiesPlayerModule;
import org.phantazm.zombies.player.action_bar.ZombiesPlayerActionBar;
import org.phantazm.zombies.player.state.ZombiesPlayerState;
import org.phantazm.zombies.player.state.context.KnockedPlayerStateContext;
import org.phantazm.zombies.player.state.revive.ReviveHandler;

import java.util.Collection;
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class ReviveHandlerTest {

    private KnockedPlayerStateContext context;

    private Collection<? extends ZombiesPlayer> zombiesPlayers;

    private ZombiesPlayerState aliveState;

    private ZombiesPlayerState deathState;

    private ZombiesPlayerMeta meta;

    private ZombiesPlayer reviver;

    @BeforeEach
    public void setup() {
        context = new KnockedPlayerStateContext(mock(Instance.class), Vec.ZERO, null, null);
        aliveState = mock(ZombiesPlayerState.class);
        deathState = mock(ZombiesPlayerState.class);
        meta = mock(ZombiesPlayerMeta.class);
        ZombiesPlayerModule module = mock(ZombiesPlayerModule.class);
        when(module.getMeta()).thenReturn(meta);
        when(module.getStats()).thenReturn(mock(ZombiesPlayerMapStats.class));
        PlayerView playerView = mock(PlayerView.class);
        when(playerView.getPlayer()).thenReturn(Optional.empty());
        when(module.getPlayerView()).thenReturn(playerView);
        ZombiesPlayerActionBar actionBar = new ZombiesPlayerActionBar(playerView);
        when(module.getActionBar()).thenReturn(actionBar);
        reviver = mock(ZombiesPlayer.class);
        when(reviver.module()).thenReturn(module);
        zombiesPlayers = Collections.singleton(reviver);
    }

    @Test
    public void testDeathContinuesWithoutReviver() {
        long initialDeathTime = 20L;
        ReviveHandler reviveHandler =
                new ReviveHandler(context, zombiesPlayers, ignored -> aliveState, () -> deathState, ignored -> false,
                        initialDeathTime);

        reviveHandler.tick(0L);

        assertFalse(reviveHandler.isReviving());
        assertEquals(initialDeathTime - 1, reviveHandler.getTicksUntilDeath());
        assertEquals(-1, reviveHandler.getTicksUntilRevive());
    }

    @Test
    public void testDeathStopsWithReviverAndReviverSet() {
        long reviveTime = 10L;
        when(reviver.getReviveTime()).thenReturn(reviveTime);
        long initialDeathTime = 20L;
        ReviveHandler reviveHandler =
                new ReviveHandler(context, zombiesPlayers, ignored -> aliveState, () -> deathState,
                        candidate -> candidate == reviver, initialDeathTime);

        reviveHandler.tick(0L);

        assertTrue(reviveHandler.isReviving());
        verify(meta).setReviving(true);
        assertEquals(initialDeathTime, reviveHandler.getTicksUntilDeath());
        assertEquals(reviveTime, reviveHandler.getTicksUntilRevive());
    }

    @Test
    public void testDeathSuggestsDeathState() {
        long initialDeathTime = 0L;
        ReviveHandler reviveHandler =
                new ReviveHandler(context, zombiesPlayers, ignored -> aliveState, () -> deathState, ignored -> false,
                        initialDeathTime);

        reviveHandler.tick(0L);

        assertTrue(reviveHandler.getSuggestedState().isPresent());
        Assertions.assertEquals(deathState, reviveHandler.getSuggestedState().get());
    }

    @Test
    public void testReviveSuggestsAliveState() {
        long reviveTime = 0L;
        when(reviver.getReviveTime()).thenReturn(reviveTime);
        ReviveHandler reviveHandler =
                new ReviveHandler(context, zombiesPlayers, ignored -> aliveState, () -> deathState,
                        candidate -> candidate == reviver, 20L);

        reviveHandler.tick(0L);
        reviveHandler.tick(0L);

        assertTrue(reviveHandler.getSuggestedState().isPresent());
        Assertions.assertEquals(aliveState, reviveHandler.getSuggestedState().get());
    }

}
