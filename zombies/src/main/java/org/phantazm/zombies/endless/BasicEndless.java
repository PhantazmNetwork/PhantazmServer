package org.phantazm.zombies.endless;

import com.github.steanky.element.core.annotation.Cache;
import com.github.steanky.element.core.annotation.FactoryMethod;
import com.github.steanky.element.core.annotation.Model;
import org.jetbrains.annotations.NotNull;
import org.phantazm.zombies.map.Round;
import org.phantazm.zombies.map.SpawnInfo;

import java.util.List;

@Model("zombies.endless.basic")
@Cache(false)
public class BasicEndless implements Endless {
    public interface Theme {
        @NotNull List<SpawnInfo> spawns();
    }

    @FactoryMethod
    public BasicEndless() {

    }

    @Override
    public @NotNull Round generateRound(int roundIndex) {
        return null;
    }
}
