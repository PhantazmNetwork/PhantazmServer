package org.phantazm.zombies.mob.skill;

import com.github.steanky.element.core.annotation.Cache;
import com.github.steanky.element.core.annotation.DataObject;
import com.github.steanky.element.core.annotation.FactoryMethod;
import com.github.steanky.element.core.annotation.Model;
import com.github.steanky.ethylene.core.ConfigElement;
import com.github.steanky.ethylene.core.ConfigPrimitive;
import com.github.steanky.ethylene.mapper.annotation.Default;
import net.kyori.adventure.key.Key;
import net.minestom.server.event.Event;
import net.minestom.server.event.EventNode;
import net.minestom.server.tag.Tag;
import org.jetbrains.annotations.NotNull;
import org.phantazm.mob.MobModel;
import org.phantazm.mob.MobStore;
import org.phantazm.mob.PhantazmMob;
import org.phantazm.mob.skill.Skill;
import org.phantazm.mob.spawner.MobSpawner;
import org.phantazm.zombies.event.PhantazmMobDeathEvent;
import org.phantazm.zombies.map.objects.MapObjects;

import java.util.UUID;

@Model("zombies.mob.skill.summon_mob")
@Cache(false)
public class SummonMobSkill implements Skill {
    private final Data data;
    private final MobSpawner mobSpawner;
    private final MobStore mobStore;

    private final Tag<Integer> mobCountTag;
    private final Tag<UUID> ownerUUID;

    private final MapObjects mapObjects;

    @FactoryMethod
    public SummonMobSkill(@NotNull Data data, @NotNull MobSpawner mobSpawner, @NotNull MobStore mobStore,
            @NotNull MapObjects mapObjects) {
        this.data = data;
        this.mobSpawner = mobSpawner;

        UUID uuid = UUID.randomUUID();
        this.mobCountTag = Tag.Integer("mob_count_" + uuid).defaultValue(0);
        this.ownerUUID = Tag.UUID("owner_" + uuid);
        this.mobStore = mobStore;

        this.mapObjects = mapObjects;

        EventNode<Event> node = mapObjects.module().eventNode().get();
        node.addListener(PhantazmMobDeathEvent.class, this::onMobDeath);
    }

    private void onMobDeath(PhantazmMobDeathEvent event) {
        PhantazmMob mob = mobStore.getMob(event.getEntity().getUuid());
        if (mob == null) {
            return;
        }

        UUID uuid = mob.entity().getTag(ownerUUID);
        if (uuid == null) {
            return;
        }

        PhantazmMob owner = mobStore.getMob(uuid);
        if (owner != null) {
            owner.entity().tagHandler().updateTag(mobCountTag, value -> value - 1);
        }
    }

    @Override
    public void use(@NotNull PhantazmMob self) {
        MobModel model = mapObjects.module().mobModelFunction().apply(data.mob);
        if (model == null) {
            return;
        }

        int spawned = self.entity().getTag(mobCountTag);
        for (int i = spawned, j = 0; i < data.maxSpawn && j < data.spawnAmount; i++, j++) {
            spawn(self, model);
        }
    }

    private void spawn(PhantazmMob self, MobModel model) {
        PhantazmMob mob = mobSpawner.spawn(mapObjects.module().instance(), self.entity().getPosition(), model);
        mob.entity().setTag(ownerUUID, self.entity().getUuid());
        self.entity().tagHandler().updateTag(mobCountTag, value -> value + 1);

        if (data.addToRound) {
            mapObjects.module().roundHandlerSupplier().get().currentRound().ifPresent(round -> {
                round.addMob(mob);
            });
        }
    }

    @DataObject
    public record Data(@NotNull Key mob, int spawnAmount, int maxSpawn, boolean addToRound) {
        @Default("addToRound")
        public static @NotNull ConfigElement defaultAddToRound() {
            return ConfigPrimitive.of(true);
        }
    }
}
