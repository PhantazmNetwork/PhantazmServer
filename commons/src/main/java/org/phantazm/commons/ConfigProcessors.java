package org.phantazm.commons;

import com.github.steanky.ethylene.core.ConfigElement;
import com.github.steanky.ethylene.core.ConfigPrimitive;
import com.github.steanky.ethylene.core.processor.ConfigProcessException;
import com.github.steanky.ethylene.core.processor.ConfigProcessor;
import net.kyori.adventure.key.InvalidKeyException;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.intellij.lang.annotations.Subst;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

/**
 * Contains static {@link ConfigProcessor} implementations used to serialize/deserialize certain common objects.
 */
public final class ConfigProcessors {
    private static final ConfigProcessor<Key> key = new ConfigProcessor<>() {
        @Override
        public Key dataFromElement(@NotNull ConfigElement element) throws ConfigProcessException {
            try {
                @Subst("key")
                String string = ConfigProcessor.STRING.dataFromElement(element);
                if (string.contains(":")) {
                    return Key.key(string);
                }
                else {
                    return Key.key(Namespaces.PHANTAZM, string);
                }
            }
            catch (InvalidKeyException keyException) {
                throw new ConfigProcessException(keyException);
            }
        }

        @Override
        public @NotNull ConfigElement elementFromData(Key key) {
            if (key.namespace().equals(Namespaces.PHANTAZM)) {
                return ConfigPrimitive.of(key.value());
            }
            return ConfigPrimitive.of(key.asString());
        }
    };
    private static final ConfigProcessor<Component> component = new ConfigProcessor<>() {
        @Override
        public Component dataFromElement(@NotNull ConfigElement element) throws ConfigProcessException {
            return MiniMessage.miniMessage().deserialize(ConfigProcessor.STRING.dataFromElement(element));
        }

        @Override
        public @NotNull ConfigElement elementFromData(@NotNull Component component) {
            return ConfigPrimitive.of(MiniMessage.miniMessage().serialize(component));
        }
    };

    private static final ConfigProcessor<UUID> uuid = new ConfigProcessor<>() {
        @Override
        public UUID dataFromElement(@NotNull ConfigElement element) throws ConfigProcessException {
            try {
                return UUID.fromString(ConfigProcessor.STRING.dataFromElement(element));
            }
            catch (IllegalArgumentException e) {
                throw new ConfigProcessException("invalid UUID string", e);
            }
        }

        @Override
        public @NotNull ConfigElement elementFromData(UUID uuid) {
            return ConfigPrimitive.of(uuid.toString());
        }
    };

    private ConfigProcessors() {
        throw new UnsupportedOperationException();
    }

    /**
     * Returns the {@link ConfigProcessor} implementation used to serialize/deserialize {@link UUID} objects.
     *
     * @return the ConfigProcessor used to serialize/deserialize UUID instances
     */
    public static @NotNull ConfigProcessor<UUID> uuid() {
        return uuid;
    }

    /**
     * Returns the {@link ConfigProcessor} implementation used to serialize/deserialize {@link Key} objects.
     *
     * @return the ConfigProcessor used to serialize/deserialize Key instances
     */
    public static @NotNull ConfigProcessor<Key> key() {
        return key;
    }

    /**
     * Returns the {@link ConfigProcessor} implementation used to serialize/deserialize {@link Component} objects.
     *
     * @return the ConfigProcessor used to serialize/deserialize Component instances
     */
    public static @NotNull ConfigProcessor<Component> component() {
        return component;
    }
}
