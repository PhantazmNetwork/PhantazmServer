package com.github.phantazmnetwork.zombies.game.map.shop;

import com.github.phantazmnetwork.commons.ConfigProcessors;
import com.github.steanky.element.core.annotation.DataObject;
import com.github.steanky.element.core.annotation.FactoryMethod;
import com.github.steanky.element.core.annotation.Model;
import com.github.steanky.element.core.annotation.ProcessorMethod;
import com.github.steanky.ethylene.core.ConfigElement;
import com.github.steanky.ethylene.core.collection.ConfigNode;
import com.github.steanky.ethylene.core.processor.ConfigProcessException;
import com.github.steanky.ethylene.core.processor.ConfigProcessor;
import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Model("zombies.map.shop.upgrade_path.linear")
public class LinearUpgradePath implements UpgradePath {
    private final Data data;

    @ProcessorMethod
    public static @NotNull ConfigProcessor<Data> processor() {
        return new ConfigProcessor<>() {
            private static final ConfigProcessor<Key> KEY_PROCESSOR = ConfigProcessors.key();
            private static final ConfigProcessor<Map<Key, Key>> KEY_KEY_PROCESSOR =
                    ConfigProcessor.mapProcessor(KEY_PROCESSOR, KEY_PROCESSOR, HashMap::new);

            @Override
            public @NotNull Data dataFromElement(@NotNull ConfigElement element) throws ConfigProcessException {
                Map<Key, Key> upgrades = KEY_KEY_PROCESSOR.dataFromElement(element.getElementOrThrow("upgrades"));
                return new Data(upgrades);
            }

            @Override
            public @NotNull ConfigElement elementFromData(@NotNull Data data) throws ConfigProcessException {
                return ConfigNode.of("upgrades", KEY_KEY_PROCESSOR.elementFromData(data.upgrades));
            }
        };
    }

    @FactoryMethod
    public LinearUpgradePath(@NotNull Data data) {
        this.data = Objects.requireNonNull(data, "data");
    }

    @Override
    public Key nextUpgrade(@NotNull Key key) {
        return data.upgrades.get(key);
    }

    @DataObject
    public record Data(@NotNull Map<Key, Key> upgrades) {

    }
}
