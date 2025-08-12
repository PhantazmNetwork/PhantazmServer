package org.phantazm.zombies.map.shop.predicate;

import com.github.steanky.element.core.annotation.Cache;
import com.github.steanky.element.core.annotation.DataObject;
import com.github.steanky.element.core.annotation.FactoryMethod;
import com.github.steanky.element.core.annotation.Model;
import net.kyori.adventure.key.Key;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.instance.EntityTracker;
import org.jetbrains.annotations.NotNull;
import org.phantazm.mob2.Mob;
import org.phantazm.zombies.map.shop.PlayerInteraction;
import org.phantazm.zombies.map.shop.Shop;

import java.util.Set;

@Model("zombies.map.shop.predicate.any_mob_in_map")
@Cache
public class AnyMobInMapPredicate extends PredicateBase<AnyMobInMapPredicate.Data> {
    @FactoryMethod
    public AnyMobInMapPredicate(@NotNull Data data) {
        super(data);
    }

    @Override
    public boolean canInteract(@NotNull PlayerInteraction interaction, @NotNull Shop shop) {
        return data.blacklist != anyMobsFound(interaction);
    }

    private boolean anyMobsFound(PlayerInteraction interaction) {
        for (LivingEntity le : interaction.player().getScene().instance().getEntityTracker().entities(EntityTracker.Target.LIVING_ENTITIES)) {
            if (!(le instanceof Mob mob)) continue;
            if (data.mobs.contains(mob.data().key())) return true;
        }

        return false;
    }

    @DataObject
    public record Data(@NotNull Set<Key> mobs,
        boolean blacklist) {
    }
}
