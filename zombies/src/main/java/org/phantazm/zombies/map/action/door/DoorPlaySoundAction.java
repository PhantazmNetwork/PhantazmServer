package org.phantazm.zombies.map.action.door;

import com.github.steanky.element.core.annotation.Cache;
import com.github.steanky.element.core.annotation.DataObject;
import com.github.steanky.element.core.annotation.FactoryMethod;
import com.github.steanky.element.core.annotation.Model;
import net.kyori.adventure.sound.Sound;
import net.minestom.server.coordinate.Point;
import org.jetbrains.annotations.NotNull;
import org.phantazm.commons.InjectionStore;
import org.phantazm.commons.LazyComponent;
import org.phantazm.zombies.map.Door;
import org.phantazm.zombies.map.action.Action;
import org.phantazm.zombies.scene2.ZombiesScene;

import java.util.function.Supplier;

@Model("zombies.map.door.action.play_sound")
@Cache
public class DoorPlaySoundAction implements LazyComponent<ZombiesScene, Action<Door>> {
    private final Data data;

    @FactoryMethod
    public DoorPlaySoundAction(@NotNull Data data) {
        this.data = data;
    }

    @Override
    public @NotNull Action<Door> apply(@NotNull InjectionStore injectionStore,
        @NotNull Supplier<@NotNull ZombiesScene> zombiesScene) {
        return new Impl(data, zombiesScene);
    }

    private record Impl(Data data,
        Supplier<ZombiesScene> zombiesScene) implements Action<Door> {

        @Override
        public void perform(@NotNull Door door) {
            Point pos = door.center();

            if (data.atInteractor) {
                door.lastInteractor().ifPresent(zombiesPlayer -> zombiesPlayer.getPlayer()
                    .ifPresent(player -> zombiesPlayer.playSound(data.sound, pos.x(), pos.y(), pos.z())));
                return;
            }

            zombiesScene.get().playSound(data.sound, pos.x(), pos.y(), pos.z());
        }
    }

    @DataObject
    public record Data(boolean atInteractor,
        @NotNull Sound sound) {
    }
}
