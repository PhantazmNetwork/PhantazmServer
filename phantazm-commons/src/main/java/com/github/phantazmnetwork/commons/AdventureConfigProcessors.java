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
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public final class AdventureConfigProcessors {
    private AdventureConfigProcessors() {
        throw new UnsupportedOperationException();
    }

    private static final ConfigProcessor<Key> key = new ConfigProcessor<>() {
        @Override
        public Key dataFromElement(@NotNull ConfigElement element) throws ConfigProcessException {
            if(!element.isString()) {
                throw new ConfigProcessException("Element must be a string");
            }

            try {
                //noinspection PatternValidation
                return Key.key(element.asString());
            }
            catch (InvalidKeyException keyException) {
                throw new ConfigProcessException(keyException);
            }
        }

        @Override
        public @NotNull ConfigElement elementFromData(Key key) {
            return new ConfigPrimitive(key.asString());
        }
    };

    private static class ComponentConfigProcessor implements ConfigProcessor<Component> {

        private final MiniMessage miniMessage;

        private final boolean optional;

        public ComponentConfigProcessor(@NotNull MiniMessage miniMessage, boolean optional) {
            this.miniMessage = Objects.requireNonNull(miniMessage, "miniMessage");
            this.optional = optional;
        }

        @Override
        public Component dataFromElement(@NotNull ConfigElement element) throws ConfigProcessException {
            if (optional && element.isNull()) {
                return null;
            }
            if(!element.isString()) {
                throw new ConfigProcessException("Element is not a string");
            }

            return miniMessage.deserialize(element.asString());
        }

        @Override
        public @NotNull ConfigElement elementFromData(Component component) {
            String string = miniMessage.serialize(component);
            return new ConfigPrimitive(string);
        }
    };

    private static final ConfigProcessor<Component> component = component(MiniMessage.miniMessage(), false);

    private static final ConfigProcessor<Component> optionalComponent = component(MiniMessage.miniMessage(), true);

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
            node.put("volume", new ConfigPrimitive(sound.volume()));
            node.put("pitch", new ConfigPrimitive(sound.pitch()));
            return node;
        }
    };

    private static final ConfigProcessor<Sound.Source> soundSource = ConfigProcessor.enumProcessor(Sound.Source.class);

    public static @NotNull ConfigProcessor<Key> key() {
        return key;
    }

    public static @NotNull ConfigProcessor<Component> component() {
        return component;
    }

    public static @NotNull ConfigProcessor<Component> component(boolean optional) {
        return optional ? optionalComponent : component();
    }

    public static @NotNull ConfigProcessor<Component> component(@NotNull MiniMessage miniMessage, boolean optional) {
        return new ComponentConfigProcessor(miniMessage, optional);
    }

    public static @NotNull ConfigProcessor<Sound> sound() {
        return sound;
    }

    public static @NotNull ConfigProcessor<Sound.Source> soundSource() {
        return soundSource;
    }
}
