package org.phantazm.zombies.map.action.wave;

import com.github.steanky.element.core.annotation.Cache;
import com.github.steanky.element.core.annotation.DataObject;
import com.github.steanky.element.core.annotation.FactoryMethod;
import com.github.steanky.element.core.annotation.Model;
import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.NotNull;
import org.phantazm.mob.PhantazmMob;
import org.phantazm.zombies.map.action.Action;
import org.phantazm.zombies.powerup.PowerupHandler;

import java.util.*;
import java.util.function.Supplier;

@Model("zombies.map.wave.action.select_powerup")
@Cache(false)
public class SelectPowerupZombieAction implements Action<List<PhantazmMob>> {
    private final Data data;
    private final Random random;
    private final Supplier<? extends PowerupHandler> powerupHandler;

    @FactoryMethod
    public SelectPowerupZombieAction(@NotNull Data data, @NotNull Random random,
            @NotNull Supplier<? extends PowerupHandler> powerupHandler) {
        this.data = Objects.requireNonNull(data, "data");
        this.random = Objects.requireNonNull(random, "random");
        this.powerupHandler = powerupHandler;
    }

    @Override
    public void perform(@NotNull List<PhantazmMob> mobs) {
        List<PhantazmMob> shuffledList = new ArrayList<>(mobs);
        if (shuffledList.isEmpty()) {
            return;
        }

        Collections.shuffle(shuffledList, random);

        Iterator<PhantazmMob> mobIterator = shuffledList.iterator();
        for (Key powerup : data.powerups) {
            if (!mobIterator.hasNext()) {
                mobIterator = shuffledList.iterator();
            }

            PhantazmMob mob = mobIterator.next();
            powerupHandler.get().assignPowerup(mob.entity(), powerup);
        }
    }

    @DataObject
    public record Data(@NotNull List<Key> powerups) {
    }
}
