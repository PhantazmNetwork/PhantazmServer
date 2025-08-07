package org.phantazm.zombies.powerup.predicate;

import com.github.steanky.element.core.annotation.Cache;
import com.github.steanky.element.core.annotation.FactoryMethod;
import com.github.steanky.element.core.annotation.Model;
import com.github.steanky.toolkit.collection.Wrapper;
import org.jetbrains.annotations.NotNull;
import org.phantazm.zombies.powerup.Powerup;
import org.phantazm.zombies.scene2.ZombiesScene;

@Model("zombies.powerup.pickup_predicate.exclusive")
@Cache
public class ExclusivePickupPredicate implements PickupPredicateComponent {
    private static final PickupPredicate INSTANCE = (zombiesPlayer, powerup) -> {
        Wrapper<Boolean> wrapper = Wrapper.of(true);
        zombiesPlayer.getScene().getAcquirable().sync(zombiesScene -> {
            for (Powerup active : zombiesScene.map().powerupHandler().spawnedOrActivePowerups()) {
                if (active == powerup) continue;
                if (active.spawned()) continue;
                if (active.key().equals(powerup.key())) {
                    wrapper.set(false);
                    break;
                }
            }
        });

        return wrapper.get();
    };

    @FactoryMethod
    public ExclusivePickupPredicate() {
    }

    @Override
    public @NotNull PickupPredicate apply(@NotNull ZombiesScene scene) {
        return INSTANCE;
    }
}
