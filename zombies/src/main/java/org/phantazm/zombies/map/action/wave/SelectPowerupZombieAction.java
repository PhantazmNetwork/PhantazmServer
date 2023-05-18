package org.phantazm.zombies.map.action.wave;

import com.github.steanky.element.core.annotation.DataObject;
import com.github.steanky.element.core.annotation.FactoryMethod;
import com.github.steanky.element.core.annotation.Model;
import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.NotNull;
import org.phantazm.mob.PhantazmMob;
import org.phantazm.zombies.Tags;
import org.phantazm.zombies.map.action.Action;

import java.util.*;

@Model("zombies.map.wave.action.select_powerup")
public class SelectPowerupZombieAction implements Action<List<PhantazmMob>> {
    private final Data data;
    private final Random random;

    @FactoryMethod
    public SelectPowerupZombieAction(@NotNull Data data, @NotNull Random random) {
        this.data = Objects.requireNonNull(data, "data");
        this.random = Objects.requireNonNull(random, "random");
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
            mob.entity().setTag(Tags.POWERUP_TAG, powerup.asString());
        }
    }

    @DataObject
    public record Data(@NotNull Set<Key> powerups) {
    }
}
