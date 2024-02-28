package org.phantazm.zombies.mob2;

import com.github.steanky.element.core.annotation.Cache;
import com.github.steanky.element.core.annotation.FactoryMethod;
import com.github.steanky.element.core.annotation.Model;
import org.jetbrains.annotations.NotNull;
import org.phantazm.commons.ExtensionHolder;
import org.phantazm.mob2.Mob;
import org.phantazm.mob2.skill.SpawnCallback;
import org.phantazm.mob2.skill.SpawnCallbackComponent;
import org.phantazm.zombies.scene2.ZombiesScene;

@Model("zombies.mob.skill.spawn_mob.callback.add_to_round")
@Cache
public class AddToRoundCallback implements SpawnCallbackComponent {
    @FactoryMethod
    public AddToRoundCallback() {

    }

    @Override
    public @NotNull SpawnCallback apply(@NotNull ExtensionHolder extensionHolder) {
        return new Internal();
    }

    private record Internal() implements SpawnCallback {
        @Override
        public void accept(@NotNull Mob mob) {
            ZombiesScene scene = mob.extensions().get(ZombiesMobSpawner.SCENE_KEY);
            scene.getAcquirable().sync(self -> {
                self.map().roundHandler().currentRound().ifPresent(round -> round.addMob(mob));
            });
        }
    }
}
