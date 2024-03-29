package org.phantazm.zombies.powerup.action.component;

import com.github.steanky.element.core.annotation.Cache;
import com.github.steanky.element.core.annotation.DataObject;
import com.github.steanky.element.core.annotation.FactoryMethod;
import com.github.steanky.element.core.annotation.Model;
import net.kyori.adventure.sound.Sound;
import net.minestom.server.instance.Instance;
import org.jetbrains.annotations.NotNull;
import org.phantazm.zombies.player.ZombiesPlayer;
import org.phantazm.zombies.powerup.Powerup;
import org.phantazm.zombies.powerup.action.InstantAction;
import org.phantazm.zombies.powerup.action.PowerupAction;
import org.phantazm.zombies.scene2.ZombiesScene;

import java.util.Random;

@Model("zombies.powerup.action.play_sound")
@Cache(false)
public class PlaySoundAction implements PowerupActionComponent {
    private final Data data;

    @FactoryMethod
    public PlaySoundAction(@NotNull Data data) {
        this.data = data;
    }

    @Override
    public @NotNull PowerupAction apply(@NotNull ZombiesScene scene) {
        return new Action(data, scene.instance(), scene.map().objects().module().random());
    }

    @DataObject
    public record Data(@NotNull Sound sound) {

    }

    private static class Action extends InstantAction {
        private final Data data;
        private final Instance instance;
        private final Random random;

        private Action(Data data, Instance instance, Random random) {
            this.data = data;
            this.instance = instance;
            this.random = random;
        }

        @Override
        public void activate(@NotNull Powerup powerup, @NotNull ZombiesPlayer player, long time) {
            instance.playSound(Sound.sound(data.sound).seed(random.nextLong()).build(), powerup.spawnLocation());
        }
    }
}
