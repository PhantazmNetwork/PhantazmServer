package com.github.phantazmnetwork.zombies.game.map.shop.display;

import com.github.phantazmnetwork.zombies.game.map.shop.PlayerInteraction;
import com.github.phantazmnetwork.zombies.game.map.shop.Shop;
import com.github.steanky.element.core.annotation.*;
import com.github.steanky.ethylene.core.ConfigElement;
import com.github.steanky.ethylene.core.collection.ConfigNode;
import com.github.steanky.ethylene.core.processor.ConfigProcessException;
import com.github.steanky.ethylene.core.processor.ConfigProcessor;
import org.jetbrains.annotations.NotNull;

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
    public IncrementalMetaDisplay(@NotNull Data data, @NotNull @DataName("displays") List<ShopDisplay> displays) {
        this.data = Objects.requireNonNull(data, "data");
        this.displays = Objects.requireNonNull(displays, "displays");
        this.displayIndex = 0;
    }

    @ProcessorMethod
    public static @NotNull ConfigProcessor<Data> processor() {
        return new ConfigProcessor<>() {
            private static final ConfigProcessor<List<String>> LIST_STRING_PROCESSOR =
                    ConfigProcessor.STRING.listProcessor();

            @Override
            public @NotNull Data dataFromElement(@NotNull ConfigElement node) throws ConfigProcessException {
                List<String> displays = LIST_STRING_PROCESSOR.dataFromElement(node.getElementOrThrow("displays"));
                boolean cycle = node.getBooleanOrThrow("cycle");
                return new Data(displays, cycle);
            }

            @Override
            public @NotNull ConfigElement elementFromData(@NotNull Data data) throws ConfigProcessException {
                return ConfigNode.of("displays", LIST_STRING_PROCESSOR.elementFromData(data.displays), "cycle",
                        data.cycle);
            }
        };
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
            oldDisplay.destroy(shop);

            int nextDisplayIndex = displayIndex + 1;
            if (nextDisplayIndex < displays.size()) {
                displayIndex = nextDisplayIndex;
            }
            else if (data.cycle) {
                displayIndex = 0;
            }
            else {
                destroy(shop);
                return;
            }

            if (!displays.isEmpty()) {
                currentDisplay = displays.get(displayIndex);
                currentDisplay.initialize(shop);
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
    public record Data(@NotNull @DataPath("displays") List<String> displays, boolean cycle) {
    }
}
