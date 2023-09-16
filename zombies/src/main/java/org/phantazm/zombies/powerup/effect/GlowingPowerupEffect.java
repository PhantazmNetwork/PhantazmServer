package org.phantazm.zombies.powerup.effect;

import com.github.steanky.element.core.annotation.Cache;
import com.github.steanky.element.core.annotation.DataObject;
import com.github.steanky.element.core.annotation.FactoryMethod;
import com.github.steanky.element.core.annotation.Model;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.entity.LivingEntity;
import org.jetbrains.annotations.NotNull;
import org.phantazm.mob2.Mob;
import org.phantazm.zombies.scene2.ZombiesScene;

import java.util.Objects;

@Model("zombies.powerup.entity_effect.glow")
@Cache(false)
public class GlowingPowerupEffect implements PowerupEffectComponent {
    private final Data data;

    @FactoryMethod
    public GlowingPowerupEffect(@NotNull Data data) {
        this.data = Objects.requireNonNull(data);
    }

    @Override
    public @NotNull PowerupEffect apply(@NotNull ZombiesScene scene) {
        return new Effect(data, scene);
    }

    private record Effect(Data data,
        ZombiesScene scene) implements PowerupEffect {
        @Override
        public void apply(@NotNull LivingEntity entity) {
            if (entity instanceof Mob mob) {
                mob.getAcquirable().sync(self -> {
                    ((Mob) self).setTeamColor(data.glowColor);
                });
            }

            entity.setGlowing(true);
        }
    }

    @DataObject
    public record Data(@NotNull NamedTextColor glowColor) {
    }
}
