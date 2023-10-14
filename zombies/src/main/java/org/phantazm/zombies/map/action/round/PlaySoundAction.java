package org.phantazm.zombies.map.action.round;

import com.github.steanky.element.core.annotation.Cache;
import com.github.steanky.element.core.annotation.DataObject;
import com.github.steanky.element.core.annotation.FactoryMethod;
import com.github.steanky.element.core.annotation.Model;
import net.kyori.adventure.sound.Sound;
import org.jetbrains.annotations.NotNull;
import org.phantazm.commons.InjectionStore;
import org.phantazm.commons.LazyComponent;
import org.phantazm.zombies.map.Round;
import org.phantazm.zombies.map.action.Action;
import org.phantazm.zombies.player.ZombiesPlayer;
import org.phantazm.zombies.scene2.ZombiesScene;

import java.util.function.Supplier;

@Model("zombies.map.round.action.play_sound")
@Cache
public class PlaySoundAction implements LazyComponent<ZombiesScene, Action<Round>> {
    private final Data data;

    @FactoryMethod
    public PlaySoundAction(@NotNull Data data) {
        this.data = data;
    }

    @Override
    public @NotNull Action<Round> apply(@NotNull InjectionStore injectionStore,
        @NotNull Supplier<@NotNull ZombiesScene> sceneSupplier) {
        return new Impl(data, sceneSupplier);
    }

    @DataObject
    public record Data(@NotNull Sound sound) {
    }

    private record Impl(Data data,
        Supplier<ZombiesScene> zombiesScene) implements Action<Round> {

        @Override
        public void perform(@NotNull Round round) {
            ZombiesScene zombiesScene = this.zombiesScene.get();
            for (ZombiesPlayer zombiesPlayer : zombiesScene.managedPlayers().values()) {
                if (zombiesPlayer.hasQuit()) {
                    continue;
                }

                zombiesPlayer.getPlayer().ifPresent(player -> player.playSound(Sound.sound(data.sound)
                    .seed(zombiesScene.map().objects().module().random().nextLong()).build()));
            }
        }
    }
}
