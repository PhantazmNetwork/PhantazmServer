package org.phantazm.mob.target;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.phantazm.mob.PhantazmMob;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

public class LastHitSelectorTest {

    private PhantazmMob mob;

    private LastHitSelector<Object> lastHitSelector;

    @BeforeEach
    public void setup() {
        mob = mock(PhantazmMob.class);
        lastHitSelector = new LastHitSelector<>();
    }

    @Test
    public void testInitiallyEmpty() {
        assertTrue(lastHitSelector.selectTarget(mob).isEmpty());
    }

    @Test
    public void testUpdatesSingle() {
        Object hit = new Object();

        lastHitSelector.setLastHit(hit);

        Optional<Object> target = lastHitSelector.selectTarget(mob);
        assertTrue(target.isPresent());
        assertEquals(hit, target.get());
    }

    @Test
    public void testUpdatesMultiple() {
        Object firstHit = new Object();
        Object secondHit = new Object();

        lastHitSelector.setLastHit(firstHit);
        lastHitSelector.setLastHit(secondHit);

        Optional<Object> target = lastHitSelector.selectTarget(mob);
        assertTrue(target.isPresent());
        assertEquals(secondHit, target.get());
    }

}
