package com.github.phantazmnetwork.commons;

import com.github.steanky.ethylene.core.ConfigElement;
import com.github.steanky.ethylene.core.ConfigPrimitive;
import com.github.steanky.ethylene.core.collection.ConfigNode;
import com.github.steanky.ethylene.core.collection.LinkedConfigNode;
import com.github.steanky.ethylene.core.processor.ConfigProcessException;
import com.github.steanky.ethylene.core.processor.ConfigProcessor;
import net.kyori.adventure.key.InvalidKeyException;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.util.RGBLike;
import org.jetbrains.annotations.NotNull;

/**
 * Contains static {@link ConfigProcessor} implementations used to serialize/deserialize certain objects from the
 * Adventure api ({@code net.kyori.adventure}).
 */
public final class AdventureConfigProcessors {
    private AdventureConfigProcessors() {
        throw new UnsupportedOperationException();
    }

    private static final ConfigProcessor<Key> key = new ConfigProcessor<>() {
        @SuppressWarnings("PatternValidation")
        @Override
        public Key dataFromElement(@NotNull ConfigElement element) throws ConfigProcessException {
            try {
                String string = ConfigProcessor.STRING.dataFromElement(element);
                if (string.contains(":")) {
                    return Key.key(string);
                } else {
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
                return new ConfigPrimitive(key.value());
            }
            return new ConfigPrimitive(key.asString());
        }
    };

    private static final ConfigProcessor<Component> component = new ConfigProcessor<>() {
        @Override
        public Component dataFromElement(@NotNull ConfigElement element) throws ConfigProcessException {
            return MiniMessage.miniMessage().deserialize(ConfigProcessor.STRING.dataFromElement(element));
        }

        @Override
        public @NotNull ConfigElement elementFromData(@NotNull Component component) {
            return new ConfigPrimitive(MiniMessage.miniMessage().serialize(component));
        }
    };

    private static final ConfigProcessor<Sound> sound = new ConfigProcessor<>() {
        @Override
        public Sound dataFromElement(@NotNull ConfigElement element) throws ConfigProcessException {
            Key name = key.dataFromElement(element.getElementOrThrow("name"));
            Sound.Source source = soundSource.dataFromElement(element.getElementOrThrow("source"));
            float volume = element.getNumberOrThrow("volume").floatValue();
            float pitch = element.getNumberOrThrow("pitch").floatValue();
            return Sound.sound(name, source, volume, pitch);
        }

        @Override
        public @NotNull ConfigElement elementFromData(Sound sound) throws ConfigProcessException {
            ConfigNode node = new LinkedConfigNode(4);
            node.put("name", key.elementFromData(sound.name()));
            node.put("source", soundSource.elementFromData(sound.source()));
            node.putNumber("volume", sound.volume());
            node.putNumber("pitch", sound.pitch());
            return node;
        }
    };

    private static final ConfigProcessor<RGBLike> rgbLike = new ConfigProcessor<>() {
        @Override
        public @NotNull RGBLike dataFromElement(@NotNull ConfigElement element) throws ConfigProcessException {
            String hex = ConfigProcessor.STRING.dataFromElement(element);
            TextColor color = TextColor.fromHexString(hex);
            if (color == null) {
                throw new ConfigProcessException("Invalid hex: " + hex);
            }

            return color;
        }

        @Override
        public @NotNull ConfigElement elementFromData(@NotNull RGBLike rgbLike) {
            return new ConfigPrimitive(TextColor.color(rgbLike).asHexString());
        }
    };

    private static final ConfigProcessor<Sound.Source> soundSource = ConfigProcessor.enumProcessor(Sound.Source.class);

    /**
     * Returns the {@link ConfigProcessor} implementation used to serialize/deserialize {@link Key} objects.
     * @return the ConfigProcessor used to serialize/deserialize Key instances
     */
    public static @NotNull ConfigProcessor<Key> key() {
        return key;
    }

    /**
     * Returns the {@link ConfigProcessor} implementation used to serialize/deserialize {@link Component} objects.
     * @return the ConfigProcessor used to serialize/deserialize Component instances
     */
    public static @NotNull ConfigProcessor<Component> component() {
        return component;
    }

    /**
     * Returns the {@link ConfigProcessor} implementation used to serialize/deserialize {@link Sound} objects.
     * @return the ConfigProcessor used to serialize/deserialize Sound instances
     */
    public static @NotNull ConfigProcessor<Sound> sound() {
        return sound;
    }

    /**
     * Returns the {@link ConfigProcessor} implementation used to serialize/deserialize {@link Sound.Source} objects.
     * @return the ConfigProcessor used to serialize/deserialize Sound.Source instances
     */
    public static @NotNull ConfigProcessor<Sound.Source> soundSource() {
        return soundSource;
    }

    public static @NotNull ConfigProcessor<RGBLike> rgbLike() {
        return rgbLike;
    }

}
