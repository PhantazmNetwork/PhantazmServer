package org.phantazm.zombies.map.action.door;

import com.github.steanky.element.core.annotation.Cache;
import com.github.steanky.element.core.annotation.DataObject;
import com.github.steanky.element.core.annotation.FactoryMethod;
import com.github.steanky.element.core.annotation.Model;
import net.kyori.adventure.sound.Sound;
import net.minestom.server.coordinate.Point;
import net.minestom.server.instance.Instance;
import org.jetbrains.annotations.NotNull;
import org.phantazm.zombies.map.Door;
import org.phantazm.zombies.map.action.Action;

@Model("zombies.map.door.action.play_sound")
@Cache(false)
public class DoorPlaySoundAction implements Action<Door> {
    private final Data data;
    private final Instance instance;

    @FactoryMethod
    public DoorPlaySoundAction(@NotNull Data data, @NotNull Instance instance) {
        this.data = data;
        this.instance = instance;
    }

    @Override
    public void perform(@NotNull Door door) {
        Point pos = door.center();

        if (data.atInteractor) {
            door.lastInteractor().ifPresent(zombiesPlayer -> zombiesPlayer.getPlayer()
                    .ifPresent(player -> zombiesPlayer.playSound(data.sound, pos.x(), pos.y(), pos.z())));
        }
        else {
            instance.playSound(data.sound, pos.x(), pos.y(), pos.z());
        }
    }

    @DataObject
    public record Data(boolean atInteractor, @NotNull Sound sound) {
    }
}
