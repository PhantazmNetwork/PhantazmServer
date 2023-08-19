package org.phantazm.zombies.map.shop.display.creator;

import com.github.steanky.element.core.annotation.*;
import org.jetbrains.annotations.NotNull;
import org.phantazm.zombies.map.shop.display.IncrementalMetaDisplay;
import org.phantazm.zombies.map.shop.display.ShopDisplay;
import org.phantazm.zombies.player.ZombiesPlayer;

import java.util.ArrayList;
import java.util.List;

@Model("zombies.map.shop.display.player.incremental")
@Cache(false)
public class IncrementalMetaPlayerDisplayCreator implements PlayerDisplayCreator {
    private final Data data;
    private final List<PlayerDisplayCreator> steps;

    @FactoryMethod
    public IncrementalMetaPlayerDisplayCreator(@NotNull Data data,
        @NotNull @Child("displays") List<PlayerDisplayCreator> steps) {
        this.data = data;
        this.steps = steps;
    }

    @Override
    public @NotNull ShopDisplay forPlayer(@NotNull ZombiesPlayer zombiesPlayer) {
        List<ShopDisplay> displays = new ArrayList<>(steps.size());
        for (PlayerDisplayCreator creator : steps) {
            displays.add(creator.forPlayer(zombiesPlayer));
        }

        return new IncrementalMetaDisplay(new IncrementalMetaDisplay.Data(data.displays, data.cycle), displays);
    }

    @DataObject
    public record Data(boolean cycle,
        @NotNull @ChildPath("displays") List<String> displays) {
    }
}
