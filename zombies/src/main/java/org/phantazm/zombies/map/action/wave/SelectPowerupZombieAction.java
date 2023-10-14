package org.phantazm.zombies.map.action.wave;

import com.github.steanky.element.core.annotation.Cache;
import com.github.steanky.element.core.annotation.DataObject;
import com.github.steanky.element.core.annotation.FactoryMethod;
import com.github.steanky.element.core.annotation.Model;
import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.NotNull;
import org.phantazm.commons.InjectionStore;
import org.phantazm.commons.LazyComponent;
import org.phantazm.mob2.Mob;
import org.phantazm.zombies.map.action.Action;
import org.phantazm.zombies.scene2.ZombiesScene;

import java.util.*;
import java.util.function.Supplier;

@Model("zombies.map.wave.action.select_powerup")
@Cache
public class SelectPowerupZombieAction implements LazyComponent<ZombiesScene, Action<List<Mob>>> {
    private final Data data;

    @FactoryMethod
    public SelectPowerupZombieAction(@NotNull Data data) {
        this.data = Objects.requireNonNull(data);
    }

    @Override
    public @NotNull Action<List<Mob>> apply(@NotNull InjectionStore injectionStore, @NotNull Supplier<@NotNull ZombiesScene> sceneSupplier) {
        return new Impl(data, sceneSupplier);
    }

    @DataObject
    public record Data(@NotNull List<Key> powerups) {
    }

    private record Impl(Data data,
        Supplier<ZombiesScene> zombiesScene) implements Action<List<Mob>> {

        @Override
        public void perform(@NotNull List<Mob> mobs) {
            List<Mob> shuffledList = new ArrayList<>(mobs);
            if (shuffledList.isEmpty()) {
                return;
            }

            ZombiesScene zombiesScene = this.zombiesScene.get();
            Collections.shuffle(shuffledList, zombiesScene.map().objects().module().random());

            Iterator<Mob> mobIterator = shuffledList.iterator();
            for (Key powerup : data.powerups) {
                if (!mobIterator.hasNext()) {
                    mobIterator = shuffledList.iterator();
                }

                Mob mob = mobIterator.next();
                zombiesScene.map().powerupHandler().assignPowerup(mob, powerup);
            }
        }
    }
}
