package org.phantazm.zombies.powerup.action;

import com.github.steanky.element.core.annotation.DataObject;
import com.github.steanky.element.core.annotation.FactoryMethod;
import com.github.steanky.element.core.annotation.Model;
import net.kyori.adventure.sound.Sound;
import net.minestom.server.instance.Instance;
import org.jetbrains.annotations.NotNull;
import org.phantazm.zombies.player.ZombiesPlayer;
import org.phantazm.zombies.powerup.Powerup;

import java.util.Objects;
import java.util.Random;
import java.util.function.Supplier;

@Model("zombies.powerup.action.play_sound")
public class PlaySoundAction implements Supplier<PowerupAction> {
    private final Data data;
    private final Instance instance;
    private final Random random;

    @FactoryMethod
    public PlaySoundAction(@NotNull Data data, @NotNull Instance instance, @NotNull Random random) {
        this.data = Objects.requireNonNull(data, "data");
        this.instance = Objects.requireNonNull(instance, "instance");
        this.random = Objects.requireNonNull(random, "random");
    }

    @Override
    public PowerupAction get() {
        return new Action(data, instance, random);
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
