package org.phantazm.mob.target;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityType;
import net.minestom.server.instance.Instance;
import net.minestom.testing.Env;
import net.minestom.testing.EnvTest;
import org.junit.jupiter.api.Test;
import org.phantazm.mob.PhantazmMob;
import org.phantazm.mob.validator.AlwaysValid;
import org.phantazm.mob.validator.NotSelfValidator;
import org.phantazm.mob.validator.TargetValidator;
import org.phantazm.proxima.bindings.minestom.Pathfinding;
import org.phantazm.proxima.bindings.minestom.ProximaEntity;

import java.util.Iterator;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@EnvTest
public class NearestEntitiesSelectorIntegrationTest {

    @Test
    public void testNoRangeIncludesSelf(Env env) {
        NearestEntitiesSelector.Data data = new NearestEntitiesSelector.Data(0.0, 1, "");
        TargetValidator validator = new AlwaysValid();
        TargetSelector<Iterable<Entity>> selector = new NearestEntitiesSelector(data, validator);
        Instance instance = env.createFlatInstance();
        ProximaEntity entity = new ProximaEntity(EntityType.PIG, UUID.randomUUID(), mock(Pathfinding.class));
        entity.setInstance(instance).join();
        PhantazmMob mob = mock(PhantazmMob.class);
        when(mob.entity()).thenReturn(entity);

        Optional<Iterable<Entity>> target = selector.selectTarget(mob);

        assertTrue(target.isPresent());
        Iterator<Entity> iterator = target.get().iterator();
        assertTrue(iterator.hasNext());
        assertEquals(entity, iterator.next());
    }

    @Test
    public void testNoRangeDoesNotIncludeFarther(Env env) {
        NearestEntitiesSelector.Data data = new NearestEntitiesSelector.Data(0.0, 1, "");
        TargetValidator validator = new NotSelfValidator();
        TargetSelector<Iterable<Entity>> selector = new NearestEntitiesSelector(data, validator);
        Instance instance = env.createFlatInstance();
        ProximaEntity entity = new ProximaEntity(EntityType.PIG, UUID.randomUUID(), mock(Pathfinding.class));
        entity.setInstance(instance).join();
        PhantazmMob mob = mock(PhantazmMob.class);
        when(mob.entity()).thenReturn(entity);
        Entity second = new Entity(EntityType.PIG);
        second.setInstance(instance, new Pos(1, 0, 0));

        Optional<Iterable<Entity>> target = selector.selectTarget(mob);

        assertTrue(target.isPresent());
        assertFalse(target.get().iterator().hasNext());
    }

    @Test
    public void testIncludesOthersBesidesSelf(Env env) {
        NearestEntitiesSelector.Data data = new NearestEntitiesSelector.Data(2.0, 1, "");
        TargetValidator validator = new NotSelfValidator();
        TargetSelector<Iterable<Entity>> selector = new NearestEntitiesSelector(data, validator);
        Instance instance = env.createFlatInstance();
        ProximaEntity entity = new ProximaEntity(EntityType.PIG, UUID.randomUUID(), mock(Pathfinding.class));
        entity.setInstance(instance).join();
        PhantazmMob mob = mock(PhantazmMob.class);
        when(mob.entity()).thenReturn(entity);
        Entity second = new Entity(EntityType.PIG);
        second.setInstance(instance, new Pos(1, 0, 0));

        Optional<Iterable<Entity>> target = selector.selectTarget(mob);

        assertTrue(target.isPresent());
        Iterator<Entity> iterator = target.get().iterator();
        assertTrue(iterator.hasNext());
        assertEquals(second, iterator.next());
    }

    @Test
    public void testBottleneck(Env env) {
        NearestEntitiesSelector.Data data = new NearestEntitiesSelector.Data(2.0, 1, "");
        TargetValidator validator = new NotSelfValidator();
        TargetSelector<Iterable<Entity>> selector = new NearestEntitiesSelector(data, validator);
        Instance instance = env.createFlatInstance();
        ProximaEntity entity = new ProximaEntity(EntityType.PIG, UUID.randomUUID(), mock(Pathfinding.class));
        entity.setInstance(instance).join();
        PhantazmMob mob = mock(PhantazmMob.class);
        when(mob.entity()).thenReturn(entity);
        Entity second = new Entity(EntityType.PIG);
        second.setInstance(instance, new Pos(1, 0, 0));
        Entity third = new Entity(EntityType.PIG);
        third.setInstance(instance, new Pos(0.5, 0, 0.5));

        Optional<Iterable<Entity>> target = selector.selectTarget(mob);

        assertTrue(target.isPresent());
        Iterator<Entity> iterator = target.get().iterator();
        assertTrue(iterator.hasNext());
        assertEquals(third, iterator.next());
    }

}
