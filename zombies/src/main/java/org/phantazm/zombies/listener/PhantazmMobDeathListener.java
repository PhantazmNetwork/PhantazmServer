package org.phantazm.zombies.listener;

import com.github.steanky.element.core.key.Constants;
import com.github.steanky.element.core.key.KeyParser;
import com.github.steanky.element.core.key.KeyString;
import net.kyori.adventure.key.Key;
import net.minestom.server.entity.Entity;
import net.minestom.server.event.entity.EntityDeathEvent;
import net.minestom.server.instance.Instance;
import org.intellij.lang.annotations.Subst;
import org.jetbrains.annotations.NotNull;
import org.phantazm.mob.MobStore;
import org.phantazm.mob.PhantazmMob;
import org.phantazm.zombies.Tags;
import org.phantazm.zombies.map.Round;
import org.phantazm.zombies.powerup.PowerupHandler;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;

public class PhantazmMobDeathListener extends PhantazmMobEventListener<EntityDeathEvent> {
    private final KeyParser keyParser;
    private final Supplier<? extends Optional<Round>> roundSupplier;
    private final PowerupHandler powerupHandler;

    public PhantazmMobDeathListener(@NotNull KeyParser keyParser, @NotNull Instance instance,
            @NotNull MobStore mobStore, @NotNull Supplier<? extends Optional<Round>> roundSupplier,
            @NotNull PowerupHandler powerupHandler) {
        super(instance, mobStore);
        this.keyParser = Objects.requireNonNull(keyParser, "keyParser");
        this.roundSupplier = Objects.requireNonNull(roundSupplier, "roundSupplier");
        this.powerupHandler = Objects.requireNonNull(powerupHandler, "powerupHandler");
    }

    @Override
    public void accept(@NotNull PhantazmMob mob, @NotNull EntityDeathEvent event) {
        roundSupplier.get().ifPresent(round -> {
            round.removeMob(mob);
        });

        Entity entity = event.getEntity();
        @Subst(Constants.NAMESPACE_OR_KEY)
        String powerup = entity.getTag(Tags.POWERUP_TAG);
        if (powerup != null) {
            if (!keyParser.isValidKey(powerup)) {
                return;
            }

            Key key = keyParser.parseKey(powerup);
            powerupHandler.spawn(key, entity.getPosition());
        }

        getMobStore().onMobDeath(event);
    }
}
