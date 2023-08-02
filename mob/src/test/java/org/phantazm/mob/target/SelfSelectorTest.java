package org.phantazm.mob.target;

import net.minestom.server.entity.Entity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.phantazm.mob.PhantazmMob;
import org.phantazm.proxima.bindings.minestom.ProximaEntity;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class SelfSelectorTest {

    private ProximaEntity entity;

    private PhantazmMob mob;

    private TargetSelector<Entity> selector;

    @BeforeEach
    public void setup() {
        entity = mock(ProximaEntity.class);
        mob = mock(PhantazmMob.class);
        when(mob.entity()).thenReturn(entity);
        selector = new SelfSelector();
    }

    @Test
    public void testReturnsSelf() {
        Optional<Entity> target = selector.selectTarget(mob);

        assertTrue(target.isPresent());
        assertEquals(entity, target.get());
    }

}
