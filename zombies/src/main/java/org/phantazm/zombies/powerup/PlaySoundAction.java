package org.phantazm.zombies.powerup;

import com.github.steanky.element.core.annotation.DataObject;
import com.github.steanky.element.core.annotation.FactoryMethod;
import com.github.steanky.element.core.annotation.Model;
import net.kyori.adventure.sound.Sound;
import net.minestom.server.instance.Instance;
import org.jetbrains.annotations.NotNull;
import org.phantazm.zombies.player.ZombiesPlayer;

import java.util.Objects;
import java.util.function.Supplier;

@Model("zombies.powerup.action.play_sound")
public class PlaySoundAction implements Supplier<PowerupAction> {
    private final Data data;
    private final Instance instance;

    @FactoryMethod
    public PlaySoundAction(@NotNull Data data, @NotNull Instance instance) {
        this.data = Objects.requireNonNull(data, "data");
        this.instance = Objects.requireNonNull(instance, "instance");
    }

    @Override
    public PowerupAction get() {
        return new Action(data, instance);
    }

    @DataObject
    public record Data(Sound sound) {
        
    }

    private static class Action implements PowerupAction {
        private final Data data;
        private final Instance instance;

        private Action(Data data, Instance instance) {
            this.data = data;
            this.instance = instance;
        }

        @Override
        public void activate(@NotNull Powerup powerup, @NotNull ZombiesPlayer player, long time) {
            instance.playSound(data.sound, powerup.spawnLocation());
        }

        @Override
        public void deactivate(@NotNull ZombiesPlayer player) {

        }

        @Override
        public @NotNull DeactivationPredicate deactivationPredicate() {
            return ImmediateDeactivationPredicate.INSTANCE;
        }
    }
}
