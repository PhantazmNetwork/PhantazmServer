package org.phantazm.zombies.powerup.effect;

import com.github.steanky.element.core.annotation.Cache;
import com.github.steanky.element.core.annotation.FactoryMethod;
import com.github.steanky.element.core.annotation.Model;
import org.jetbrains.annotations.NotNull;
import org.phantazm.zombies.scene.ZombiesScene;

@Model("zombies.powerup.entity_effect.none")
@Cache(false)
public class NoPowerupEffect implements PowerupEffectComponent {
    public static PowerupEffect INSTANCE = entity -> {
    };

    @FactoryMethod
    public NoPowerupEffect() {
    }

    @Override
    public @NotNull PowerupEffect apply(@NotNull ZombiesScene scene) {
        return INSTANCE;
    }
}
