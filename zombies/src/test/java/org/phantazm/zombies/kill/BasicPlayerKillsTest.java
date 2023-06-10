package org.phantazm.zombies.kill;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.phantazm.mob.PhantazmMob;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

public class BasicPlayerKillsTest {

    private PlayerKills kills;

    @BeforeEach
    public void setup() {
        kills = new BasicPlayerKills();
    }

    @Test
    public void testInitialZeroKills() {
        assertEquals(0, kills.getKills());
    }

    @Test
    public void testKillsIncreaseAfterKill() {
        PhantazmMob mob = mock(PhantazmMob.class);

        kills.onKill(mob);

        assertEquals(1, kills.getKills());
    }

}
