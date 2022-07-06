package com.github.phantazmnetwork.zombies.equipment.gun.data;

import com.github.phantazmnetwork.commons.AdventureConfigProcessors;
import com.github.steanky.ethylene.core.ConfigElement;
import com.github.steanky.ethylene.core.collection.ConfigNode;
import com.github.steanky.ethylene.core.collection.LinkedConfigNode;
import com.github.steanky.ethylene.core.processor.ConfigProcessException;
import com.github.steanky.ethylene.core.processor.ConfigProcessor;
import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public record GunData(@NotNull Key name) {

    public static @NotNull ConfigProcessor<GunData> processor() {
        ConfigProcessor<Key> keyProcessor = AdventureConfigProcessors.key();
        return new ConfigProcessor<>() {
            @Override
            public @NotNull GunData dataFromElement(@NotNull ConfigElement element) throws ConfigProcessException {
                Key name = keyProcessor.dataFromElement(element.getElementOrThrow("name"));
                return new GunData(name);
            }

            @Override
            public @NotNull ConfigElement elementFromData(@NotNull GunData gunData) throws ConfigProcessException {
                ConfigNode node = new LinkedConfigNode(1);
                node.put("name", keyProcessor.elementFromData(gunData.name()));

                return node;
            }
        };
    }

    public GunData {
        Objects.requireNonNull(name, "name");
    }

}
