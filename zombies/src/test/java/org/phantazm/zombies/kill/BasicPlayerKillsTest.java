package org.phantazm.zombies.kill;

import net.kyori.adventure.key.Key;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.phantazm.commons.Namespaces;
import org.phantazm.mob.PhantazmMob;
import org.phantazm.stats.zombies.BasicZombiesPlayerMapStats;
import org.phantazm.stats.zombies.ZombiesPlayerMapStats;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

public class BasicPlayerKillsTest {

    private PlayerKills kills;

    @BeforeEach
    public void setup() {
        ZombiesPlayerMapStats mapStats = BasicZombiesPlayerMapStats.createBasicStats(UUID.randomUUID(),
                Key.key(Namespaces.PHANTAZM, "test_map"));
        kills = new BasicPlayerKills(mapStats);
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
