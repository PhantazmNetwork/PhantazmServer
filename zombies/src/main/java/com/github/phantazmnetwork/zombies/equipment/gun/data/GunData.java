package com.github.phantazmnetwork.zombies.equipment.gun.data;

import com.github.phantazmnetwork.commons.ConfigProcessors;
import com.github.steanky.ethylene.core.ConfigElement;
import com.github.steanky.ethylene.core.collection.ConfigNode;
import com.github.steanky.ethylene.core.collection.LinkedConfigNode;
import com.github.steanky.ethylene.core.processor.ConfigProcessException;
import com.github.steanky.ethylene.core.processor.ConfigProcessor;
import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * General data for a gun.
 *
 * @param name The unique {@link Key} name of the gun
 */
public record GunData(@NotNull Key name) {

    /**
     * Creates a {@link GunData}.
     *
     * @param name The unique {@link Key} name of the gun
     */
    public GunData {
        Objects.requireNonNull(name, "name");
    }

    /**
     * Creates a {@link ConfigProcessor} for {@link GunData}.
     *
     * @return A {@link ConfigProcessor} for {@link GunData}
     */
    public static @NotNull ConfigProcessor<GunData> processor() {
        ConfigProcessor<Key> keyProcessor = ConfigProcessors.key();
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

}
