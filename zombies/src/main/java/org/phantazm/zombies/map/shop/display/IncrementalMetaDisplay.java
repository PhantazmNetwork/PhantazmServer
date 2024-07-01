package org.phantazm.zombies.map.shop.display;

import com.github.steanky.element.core.annotation.*;
import org.jetbrains.annotations.NotNull;
import org.phantazm.zombies.map.shop.PlayerInteraction;
import org.phantazm.zombies.map.shop.Shop;

import java.util.List;
import java.util.Objects;

@Model("zombies.map.shop.display.incremental_meta")
@Cache(false)
public class IncrementalMetaDisplay implements ShopDisplay {
    private final Data data;
    private final List<ShopDisplay> displays;

    private ShopDisplay currentDisplay;
    private int displayIndex;

    @FactoryMethod
    public IncrementalMetaDisplay(@NotNull Data data, @NotNull @Child("displays") List<ShopDisplay> displays) {
        this.data = Objects.requireNonNull(data);
        this.displays = Objects.requireNonNull(displays);
        this.displayIndex = 0;
    }

    @Override
    public void initialize(@NotNull Shop shop) {
        if (!displays.isEmpty() && currentDisplay == null) {
            currentDisplay = displays.get(0);
            currentDisplay.initialize(shop);
        }
    }

    @Override
    public void destroy(@NotNull Shop shop) {
        if (currentDisplay != null) {
            currentDisplay.destroy(shop);
        }

        currentDisplay = null;
        displayIndex = 0;
    }

    @Override
    public void update(@NotNull Shop shop, @NotNull PlayerInteraction interaction, boolean interacted) {
        if (interacted && currentDisplay != null) {
            ShopDisplay oldDisplay = currentDisplay;

            int nextDisplayIndex = displayIndex + 1;
            if (nextDisplayIndex < displays.size()) {
                displayIndex = nextDisplayIndex;
            } else if (data.cycle) {
                displayIndex = 0;
            }

            if (!displays.isEmpty()) {
                ShopDisplay newDisplay = displays.get(displayIndex);
                currentDisplay = newDisplay;

                if (newDisplay != oldDisplay) {
                    oldDisplay.destroy(shop);
                    newDisplay.initialize(shop);
                }
            }
        }
    }

    @Override
    public void tick(long time) {
        if (currentDisplay != null) {
            currentDisplay.tick(time);
        }
    }

    @DataObject
    public record Data(boolean cycle) {
    }
}
