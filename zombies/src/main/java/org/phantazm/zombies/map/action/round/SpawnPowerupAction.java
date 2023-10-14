package org.phantazm.zombies.map.action.round;

import com.github.steanky.element.core.annotation.Cache;
import com.github.steanky.element.core.annotation.DataObject;
import com.github.steanky.element.core.annotation.FactoryMethod;
import com.github.steanky.element.core.annotation.Model;
import com.github.steanky.vector.Vec3D;
import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.NotNull;
import org.phantazm.commons.InjectionStore;
import org.phantazm.commons.LazyComponent;
import org.phantazm.core.VecUtils;
import org.phantazm.zombies.map.Round;
import org.phantazm.zombies.map.action.Action;
import org.phantazm.zombies.scene2.ZombiesScene;

import java.util.Objects;
import java.util.function.Supplier;

@Model("zombies.map.room.action.spawn_powerup")
@Cache
public class SpawnPowerupAction implements LazyComponent<ZombiesScene, Action<Round>> {
    private final Data data;

    @FactoryMethod
    public SpawnPowerupAction(@NotNull Data data) {
        this.data = Objects.requireNonNull(data);
    }

    @Override
    public @NotNull Action<Round> apply(@NotNull InjectionStore injectionStore,
        @NotNull Supplier<@NotNull ZombiesScene> sceneSupplier) {
        return new Impl(data, sceneSupplier);
    }

    @DataObject
    public record Data(@NotNull Key powerup,
        @NotNull Vec3D spawnPosition) {
    }

    private record Impl(Data data,
        Supplier<ZombiesScene> zombiesScene) implements Action<Round> {

        @Override
        public void perform(@NotNull Round round) {
            zombiesScene.get().map().powerupHandler().spawnIfExists(data.powerup, VecUtils.toPoint(data.spawnPosition));
        }
    }
}
