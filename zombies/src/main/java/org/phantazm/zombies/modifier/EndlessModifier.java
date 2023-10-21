package org.phantazm.zombies.modifier;

import com.github.steanky.element.core.annotation.Cache;
import com.github.steanky.element.core.annotation.FactoryMethod;
import com.github.steanky.element.core.annotation.Model;
import org.jetbrains.annotations.NotNull;
import org.phantazm.commons.DualComponent;
import org.phantazm.commons.InjectionStore;
import org.phantazm.zombies.scene2.ZombiesScene;

@Model("zombies.modifier.endless")
@Cache
public class EndlessModifier implements DualComponent<ZombiesScene, Modifier> {
    @FactoryMethod
    public EndlessModifier() {
    }

    @Override
    public @NotNull Modifier apply(@NotNull InjectionStore injectionStore, @NotNull ZombiesScene scene) {
        return new Impl(scene);
    }

    private record Impl(ZombiesScene scene) implements Modifier {
        @Override
        public void apply() {
            scene.map().roundHandler().enableEndless();
        }
    }
}
