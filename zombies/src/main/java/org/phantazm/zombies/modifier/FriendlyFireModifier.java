package org.phantazm.zombies.modifier;

import com.github.steanky.element.core.annotation.Cache;
import com.github.steanky.element.core.annotation.DataObject;
import com.github.steanky.element.core.annotation.FactoryMethod;
import com.github.steanky.element.core.annotation.Model;
import com.github.steanky.ethylene.core.ConfigElement;
import com.github.steanky.ethylene.core.ConfigPrimitive;
import com.github.steanky.ethylene.mapper.annotation.Default;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.phantazm.commons.DualComponent;
import org.phantazm.commons.InjectionStore;
import org.phantazm.core.player.PlayerView;
import org.phantazm.zombies.event.equipment.EntityDamageByGunEvent;
import org.phantazm.zombies.event.equipment.GunTargetSelectEvent;
import org.phantazm.zombies.player.ZombiesPlayer;
import org.phantazm.zombies.player.state.ZombiesPlayerStateKeys;
import org.phantazm.zombies.scene2.ZombiesScene;

import java.util.Objects;
import java.util.Optional;

@Model("zombies.modifier.friendly_fire")
@Cache
public class FriendlyFireModifier implements DualComponent<ZombiesScene, Modifier> {
    private final Data data;

    @FactoryMethod
    public FriendlyFireModifier(@NotNull Data data) {
        this.data = Objects.requireNonNull(data);
    }

    @Override
    public @NotNull Modifier apply(@NotNull InjectionStore injectionStore, @NotNull ZombiesScene scene) {
        return new Impl(data, scene);
    }

    private record Impl(Data data,
        ZombiesScene scene) implements Modifier {
        @Override
        public void apply() {
            scene.addListener(GunTargetSelectEvent.class, event -> {
                Entity entity = event.getEntity();
                if (!(entity instanceof Player player)) {
                    return;
                }

                Optional<? extends Entity> owner = event.gun().owner();
                if (owner.isPresent() && owner.get() == player) {
                    //you aren't allowed to shoot yourself
                    return;
                }

                scene.getAcquirable().sync(self -> {
                    ZombiesPlayer zombiesPlayer = self.managedPlayers().get(PlayerView.lookup(player.getUuid()));
                    if (zombiesPlayer != null && zombiesPlayer.isState(ZombiesPlayerStateKeys.ALIVE)) {
                        event.setForceSelect(true);
                    }
                });
            });

            scene.addListener(EntityDamageByGunEvent.class, event -> {
                if (!(event.getEntity() instanceof Player)) {
                    return;
                }

                event.setDamage((float) (event.getDamage() * data.playerDamageMultiplier));
            });
        }
    }

    @DataObject
    public record Data(double playerDamageMultiplier) {
        @Default("playerDamageMultiplier")
        public static @NotNull ConfigElement defaultPlayerDamageMultiplier() {
            return ConfigPrimitive.of(0.5);
        }
    }
}
