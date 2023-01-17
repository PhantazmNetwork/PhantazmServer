package org.phantazm.zombies.map.action.round;

import com.github.steanky.element.core.annotation.DataObject;
import com.github.steanky.element.core.annotation.FactoryMethod;
import com.github.steanky.element.core.annotation.Model;
import com.github.steanky.vector.Vec3D;
import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.NotNull;
import org.phantazm.core.VecUtils;
import org.phantazm.zombies.map.Round;
import org.phantazm.zombies.map.action.Action;
import org.phantazm.zombies.powerup.PowerupHandler;

import java.util.Objects;
import java.util.function.Supplier;

@Model("zombies.map.room.action.spawn_powerup")
public class SpawnPowerupAction implements Action<Round> {
    private final Data data;
    private final Supplier<? extends PowerupHandler> powerupHandler;

    @FactoryMethod
    public SpawnPowerupAction(@NotNull Data data, @NotNull Supplier<? extends PowerupHandler> powerupHandler) {
        this.data = Objects.requireNonNull(data);
        this.powerupHandler = Objects.requireNonNull(powerupHandler);
    }

    @Override
    public void perform(@NotNull Round round) {
        powerupHandler.get().spawn(data.powerup, VecUtils.toPoint(data.spawnPosition));
    }

    @DataObject
    public record Data(Key powerup, Vec3D spawnPosition) {
    }
}
