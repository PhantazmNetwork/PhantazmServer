package org.phantazm.zombies.modifier;

import com.github.steanky.element.core.annotation.Cache;
import com.github.steanky.element.core.annotation.FactoryMethod;
import com.github.steanky.element.core.annotation.Model;
import org.jetbrains.annotations.NotNull;
import org.phantazm.commons.DualComponent;
import org.phantazm.commons.InjectionStore;
import org.phantazm.zombies.event.player.OpenDoorEvent;
import org.phantazm.zombies.scene2.ZombiesScene;

@Model("zombies.modifier.door_restricting")
@Cache
public class DoorRestrictingModifier implements DualComponent<ZombiesScene, Modifier> {
    @FactoryMethod
    public DoorRestrictingModifier() {
    }

    @Override
    public @NotNull Modifier apply(@NotNull InjectionStore injectionStore, @NotNull ZombiesScene scene) {
        return new Impl(scene);
    }

    private record Impl(ZombiesScene scene) implements Modifier {
        @Override
        public void apply() {
            scene.addListener(OpenDoorEvent.class, event -> event.setCancelled(true));
        }
    }
}
