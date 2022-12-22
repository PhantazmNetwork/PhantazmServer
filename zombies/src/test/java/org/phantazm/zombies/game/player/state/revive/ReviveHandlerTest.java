package org.phantazm.zombies.game.player.state.revive;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.phantazm.zombies.player.ZombiesPlayer;
import org.phantazm.zombies.player.ZombiesPlayerMeta;
import org.phantazm.zombies.player.ZombiesPlayerModule;
import org.phantazm.zombies.player.state.ZombiesPlayerState;
import org.phantazm.zombies.player.state.revive.ReviveHandler;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class ReviveHandlerTest {

    private ZombiesPlayerState aliveState;

    private ZombiesPlayerState deathState;

    private ZombiesPlayerMeta meta;

    private ZombiesPlayer reviver;

    @BeforeEach
    public void setup() {
        aliveState = mock(ZombiesPlayerState.class);
        deathState = mock(ZombiesPlayerState.class);
        meta = mock(ZombiesPlayerMeta.class);
        ZombiesPlayerModule module = mock(ZombiesPlayerModule.class);
        when(module.getMeta()).thenReturn(meta);
        reviver = mock(ZombiesPlayer.class);
        when(reviver.getModule()).thenReturn(module);
    }

    @Test
    public void testDeathContinuesWithoutReviver() {
        long initialDeathTime = 20L;
        ReviveHandler reviveHandler =
                new ReviveHandler(() -> aliveState, () -> deathState, () -> null, initialDeathTime);

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
                new ReviveHandler(() -> aliveState, () -> deathState, () -> reviver, initialDeathTime);

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
                new ReviveHandler(() -> aliveState, () -> deathState, () -> null, initialDeathTime);

        reviveHandler.tick(0L);

        assertTrue(reviveHandler.getSuggestedState().isPresent());
        Assertions.assertEquals(deathState, reviveHandler.getSuggestedState().get());
    }

    @Test
    public void testReviveSuggestsAliveState() {
        long reviveTime = 0L;
        when(reviver.getReviveTime()).thenReturn(reviveTime);
        ReviveHandler reviveHandler = new ReviveHandler(() -> aliveState, () -> deathState, () -> reviver, 20L);

        reviveHandler.tick(0L);
        reviveHandler.tick(0L);

        assertTrue(reviveHandler.getSuggestedState().isPresent());
        Assertions.assertEquals(aliveState, reviveHandler.getSuggestedState().get());
    }

    @Test
    public void testDisabledReviverResetsRevive() {
        when(reviver.getReviveTime()).thenReturn(10L);
        long initialDeathTime = 20L;
        ReviveHandler reviveHandler =
                new ReviveHandler(() -> aliveState, () -> deathState, () -> reviver, initialDeathTime);

        reviveHandler.tick(0L);
        when(meta.isCanRevive()).thenReturn(false);
        reviveHandler.tick(0L);

        assertFalse(reviveHandler.isReviving());
        verify(meta).setReviving(false);
        assertEquals(initialDeathTime, reviveHandler.getTicksUntilDeath());
        assertEquals(-1, reviveHandler.getTicksUntilRevive());
    }

}
