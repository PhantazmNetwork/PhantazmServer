package org.phantazm.zombies.powerup.predicate;

import com.github.steanky.element.core.annotation.Cache;
import com.github.steanky.element.core.annotation.FactoryMethod;
import com.github.steanky.element.core.annotation.Model;
import net.minestom.server.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.phantazm.zombies.scene2.ZombiesScene;

import java.util.Optional;

@Model("zombies.powerup.pickup_predicate.missing_health")
@Cache
public class PlayerHealthPredicate implements PickupPredicateComponent {
    private static final PickupPredicate INSTANCE = zombiesPlayer -> {
        Optional<Player> playerOptional = zombiesPlayer.getPlayer();
        if (playerOptional.isEmpty()) {
            return false;
        }

        Player player = playerOptional.get();
        return player.getHealth() < player.getMaxHealth();
    };

    @FactoryMethod
    public PlayerHealthPredicate() {
    }

    @Override
    public @NotNull PickupPredicate apply(@NotNull ZombiesScene scene) {
        return INSTANCE;
    }
}
