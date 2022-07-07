package com.github.phantazmnetwork.api.config.processor;

import com.github.phantazmnetwork.commons.AdventureConfigProcessors;
import com.github.steanky.ethylene.core.ConfigElement;
import com.github.steanky.ethylene.core.collection.ConfigNode;
import com.github.steanky.ethylene.core.collection.LinkedConfigNode;
import com.github.steanky.ethylene.core.processor.ConfigProcessException;
import com.github.steanky.ethylene.core.processor.ConfigProcessor;
import net.kyori.adventure.key.Key;
import net.minestom.server.entity.EntityType;
import net.minestom.server.particle.Particle;
import net.minestom.server.potion.Potion;
import net.minestom.server.potion.PotionEffect;
import net.minestom.server.registry.ProtocolObject;
import net.minestom.server.utils.NamespaceID;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.function.Function;

public class MinestomConfigProcessors {

    private MinestomConfigProcessors() {
        throw new UnsupportedOperationException();
    }

    @SuppressWarnings("ClassCanBeRecord")
    private static class ProtocolObjectConfigProcessor<TObject extends ProtocolObject> implements ConfigProcessor<TObject> {

        private static final ConfigProcessor<Key> KEY_PROCESSOR = AdventureConfigProcessors.key();

        private final Function<NamespaceID, TObject> registryLookup;

        public ProtocolObjectConfigProcessor(@NotNull Function<NamespaceID, TObject> registryLookup) {
            this.registryLookup = Objects.requireNonNull(registryLookup, "registryLookup");
        }

        @Override
        public @NotNull TObject dataFromElement(@NotNull ConfigElement element) throws ConfigProcessException {
            Key key = KEY_PROCESSOR.dataFromElement(element);
            NamespaceID namespaceID = NamespaceID.from(key);

            TObject object = registryLookup.apply(namespaceID);
            if (object == null) {
                throw new ConfigProcessException("Unknown protocol object: " + key);
            }

            return object;
        }

        @Override
        public @NotNull ConfigElement elementFromData(@NotNull TObject object) throws ConfigProcessException {
            return KEY_PROCESSOR.elementFromData(object.namespace());
        }
    }

    private static final ConfigProcessor<EntityType> ENTITY_TYPE = new ProtocolObjectConfigProcessor<>(EntityType::fromNamespaceId);

    private static final ConfigProcessor<Particle> PARTICLE = new ProtocolObjectConfigProcessor<>(Particle::fromNamespaceId);

    private static final ConfigProcessor<PotionEffect> POTION_EFFECT = new ProtocolObjectConfigProcessor<>(PotionEffect::fromNamespaceId);

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

    public static @NotNull ConfigProcessor<Particle> particle() {
        return PARTICLE;
    }

    public static @NotNull ConfigProcessor<PotionEffect> potionEffect() {
        return POTION_EFFECT;
    }

    public static @NotNull ConfigProcessor<Potion> potion() {
        return POTION;
    }

}
