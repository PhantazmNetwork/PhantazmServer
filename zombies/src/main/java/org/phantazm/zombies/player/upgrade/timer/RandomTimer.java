package org.phantazm.zombies.player.upgrade.timer;

import com.github.steanky.element.core.annotation.Cache;
import com.github.steanky.element.core.annotation.DataObject;
import com.github.steanky.element.core.annotation.FactoryMethod;
import com.github.steanky.element.core.annotation.Model;
import org.jetbrains.annotations.NotNull;
import org.phantazm.commons.InjectionStore;
import org.phantazm.commons.MathUtils;
import org.phantazm.zombies.player.ZombiesPlayer;

@Model("zombies.upgrade.timer.random")
@Cache
public class RandomTimer implements TimerComponent {
    private final Data data;

    @FactoryMethod
    public RandomTimer(@NotNull Data data) {
        this.data = data;
    }

    @Override
    public @NotNull Timer apply(@NotNull InjectionStore injectionStore, @NotNull ZombiesPlayer zombiesPlayer) {
        return new Internal(data);
    }

    private record Internal(Data data) implements Timer {
        @Override
        public int getAsInt() {
            return MathUtils.randomInterval(data.minDuration, data.maxDuration);
        }
    }

    @DataObject
    public record Data(int minDuration,
        int maxDuration) {
    }
}
