package com.github.phantazmnetwork.api.config.processor;

import com.github.phantazmnetwork.commons.AdventureConfigProcessors;
import com.github.steanky.ethylene.core.ConfigElement;
import com.github.steanky.ethylene.core.collection.ConfigNode;
import com.github.steanky.ethylene.core.collection.LinkedConfigNode;
import com.github.steanky.ethylene.core.processor.ConfigProcessException;
import com.github.steanky.ethylene.core.processor.ConfigProcessor;
import net.kyori.adventure.key.Key;
import net.minestom.server.entity.EntityType;
import net.minestom.server.potion.Potion;
import net.minestom.server.potion.PotionEffect;
import net.minestom.server.utils.NamespaceID;
import org.jetbrains.annotations.NotNull;

public class MinestomConfigProcessors {

    private MinestomConfigProcessors() {
        throw new UnsupportedOperationException();
    }

    private static final ConfigProcessor<EntityType> ENTITY_TYPE = new ConfigProcessor<>() {

        private final ConfigProcessor<Key> keyProcessor = AdventureConfigProcessors.key();

        @Override
        public @NotNull EntityType dataFromElement(@NotNull ConfigElement element) throws ConfigProcessException {
            Key key = keyProcessor.dataFromElement(element);
            NamespaceID namespace = NamespaceID.from(key);

            return EntityType.fromNamespaceId(namespace);
        }

        @Override
        public @NotNull ConfigElement elementFromData(@NotNull EntityType entityType) throws ConfigProcessException {
            return keyProcessor.elementFromData(entityType.namespace());
        }
    };

    private static final ConfigProcessor<PotionEffect> POTION_EFFECT = new ConfigProcessor<>() {

        private final ConfigProcessor<Key> keyProcessor = AdventureConfigProcessors.key();

        @Override
        public @NotNull PotionEffect dataFromElement(@NotNull ConfigElement element) throws ConfigProcessException {
            Key key = keyProcessor.dataFromElement(element);
            NamespaceID namespace = NamespaceID.from(key);

            PotionEffect potionEffect = PotionEffect.fromNamespaceId(namespace);
            if (potionEffect == null) {
                throw new ConfigProcessException("PotionEffect not found: " + namespace);
            }

            return potionEffect;
        }

        @Override
        public @NotNull ConfigElement elementFromData(@NotNull PotionEffect potion) throws ConfigProcessException {
            return keyProcessor.elementFromData(potion.namespace());
        }
    };

    private static final ConfigProcessor<Potion> POTION = new ConfigProcessor<>() {

        @Override
        public Potion dataFromElement(@NotNull ConfigElement element) throws ConfigProcessException {
            PotionEffect effect = POTION_EFFECT.dataFromElement(element.getElementOrThrow("effect"));
            byte amplifier = element.getNumberOrThrow("amplifier").byteValue();
            int duration = element.getNumberOrThrow("duration").intValue();

            return new Potion(effect, amplifier, duration);
        }

        @Override
        public @NotNull ConfigElement elementFromData(Potion potion) throws ConfigProcessException {
            ConfigNode node = new LinkedConfigNode(3);
            node.put("effect", POTION_EFFECT.elementFromData(potion.effect()));
            node.putNumber("amplifier", potion.amplifier());
            node.putNumber("duration", potion.duration());

            return node;
        }
    };

    public static @NotNull ConfigProcessor<EntityType> entityType() {
        return ENTITY_TYPE;
    }

    public static @NotNull ConfigProcessor<PotionEffect> potionEffect() {
        return POTION_EFFECT;
    }

    public static @NotNull ConfigProcessor<Potion> potion() {
        return POTION;
    }

}
