package org.phantazm.zombies.map.action.wave;

import com.github.steanky.element.core.annotation.Cache;
import com.github.steanky.element.core.annotation.DataObject;
import com.github.steanky.element.core.annotation.FactoryMethod;
import com.github.steanky.element.core.annotation.Model;
import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.NotNull;
import org.phantazm.mob2.Mob;
import org.phantazm.zombies.map.action.Action;
import org.phantazm.zombies.powerup.PowerupHandler;

import java.util.*;
import java.util.function.Supplier;

@Model("zombies.map.wave.action.select_powerup")
@Cache(false)
public class SelectPowerupZombieAction implements Action<List<Mob>> {
    private final Data data;
    private final Random random;
    private final Supplier<? extends PowerupHandler> powerupHandler;

    @FactoryMethod
    public SelectPowerupZombieAction(@NotNull Data data, @NotNull Random random,
        @NotNull Supplier<? extends PowerupHandler> powerupHandler) {
        this.data = Objects.requireNonNull(data);
        this.random = Objects.requireNonNull(random);
        this.powerupHandler = powerupHandler;
    }

    @Override
    public void perform(@NotNull List<Mob> mobs) {
        List<Mob> shuffledList = new ArrayList<>(mobs);
        if (shuffledList.isEmpty()) {
            return;
        }

        Collections.shuffle(shuffledList, random);

        Iterator<Mob> mobIterator = shuffledList.iterator();
        for (Key powerup : data.powerups) {
            if (!mobIterator.hasNext()) {
                mobIterator = shuffledList.iterator();
            }

            Mob mob = mobIterator.next();
            powerupHandler.get().assignPowerup(mob, powerup);
        }
    }

    @DataObject
    public record Data(@NotNull List<Key> powerups) {
    }
}
