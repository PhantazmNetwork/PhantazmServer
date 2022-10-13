package com.github.phantazmnetwork.server;

import com.github.phantazmnetwork.commons.vector.Vec3D;
import com.github.phantazmnetwork.commons.vector.Vec3F;
import com.github.phantazmnetwork.commons.vector.Vec3I;
import com.github.steanky.ethylene.core.ConfigElement;
import com.github.steanky.ethylene.core.ConfigPrimitive;
import com.github.steanky.ethylene.core.ElementType;
import com.github.steanky.ethylene.mapper.MappingProcessorSource;
import com.github.steanky.ethylene.mapper.PrioritizedBase;
import com.github.steanky.ethylene.mapper.signature.ScalarSignature;
import com.github.steanky.ethylene.mapper.signature.Signature;
import com.github.steanky.ethylene.mapper.type.Token;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.intellij.lang.annotations.Subst;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Initializes features related to Ethylene.
 */
public final class Ethylene {
    private static final Logger LOGGER = LoggerFactory.getLogger(Mob.class);

    private static MappingProcessorSource mappingProcessorSource;

    static void initialize() {
        LOGGER.info("Initializing Ethylene...");

        mappingProcessorSource =
                MappingProcessorSource.builder().withCustomSignature(vec3I()).withCustomSignature(vec3F())
                        .withCustomSignature(sound()).withCustomSignature(vec3D())
                        .withScalarSignature(new KeySignature()).withScalarSignature(new UUIDSignature())
                        .withScalarSignature(new ComponentSignature())
                        .withTypeImplementation(Object2IntOpenHashMap.class, Object2IntMap.class)
                        .withStandardSignatures().withStandardTypeImplementations().build();

        LOGGER.info("Ethylene initialized.");
    }

    private static Signature<Vec3I> vec3I() {
        return Signature.builder(Token.ofClass(Vec3I.class),
                (ignored, args) -> Vec3I.of((int)args[0], (int)args[1], (int)args[2]),
                vec -> List.of(new Signature.TypedObject("x", Token.PRIMITIVE_INT, vec.getX()),
                        new Signature.TypedObject("y", Token.PRIMITIVE_INT, vec.getY()),
                        new Signature.TypedObject("z", Token.PRIMITIVE_INT, vec.getZ())),
                Map.entry("x", Token.PRIMITIVE_INT), Map.entry("y", Token.PRIMITIVE_INT),
                Map.entry("z", Token.PRIMITIVE_INT)).matchingNames().matchingTypeHints().build();
    }

    private static Signature<Vec3F> vec3F() {
        return Signature.builder(Token.ofClass(Vec3F.class),
                (ignored, args) -> Vec3F.of((float)args[0], (float)args[1], (float)args[2]),
                vec -> List.of(new Signature.TypedObject("x", Token.PRIMITIVE_FLOAT, vec.getX()),
                        new Signature.TypedObject("y", Token.PRIMITIVE_FLOAT, vec.getY()),
                        new Signature.TypedObject("z", Token.PRIMITIVE_FLOAT, vec.getZ())),
                Map.entry("x", Token.PRIMITIVE_FLOAT), Map.entry("y", Token.PRIMITIVE_FLOAT),
                Map.entry("z", Token.PRIMITIVE_FLOAT)).matchingNames().matchingTypeHints().build();
    }

    private static Signature<Vec3D> vec3D() {
        return Signature.builder(Token.ofClass(Vec3D.class),
                (ignored, args) -> Vec3D.of((double)args[0], (double)args[1], (double)args[2]),
                vec -> List.of(new Signature.TypedObject("x", Token.PRIMITIVE_DOUBLE, vec.getX()),
                        new Signature.TypedObject("y", Token.PRIMITIVE_DOUBLE, vec.getY()),
                        new Signature.TypedObject("z", Token.PRIMITIVE_DOUBLE, vec.getZ())),
                Map.entry("x", Token.PRIMITIVE_DOUBLE), Map.entry("y", Token.PRIMITIVE_DOUBLE),
                Map.entry("z", Token.PRIMITIVE_DOUBLE)).matchingNames().matchingTypeHints().build();
    }

    private static Signature<Sound> sound() {
        return Signature.builder(Token.ofClass(Sound.class),
                        (ignored, args) -> Sound.sound((Key)args[0], (Sound.Source)args[1], (float)args[2], (float)args[3]),
                        sound -> List.of(new Signature.TypedObject("name", Token.ofClass(Key.class), sound.name()),
                                new Signature.TypedObject("source", Token.ofClass(Sound.Source.class), sound.source()),
                                new Signature.TypedObject("volume", Token.PRIMITIVE_FLOAT, sound.volume()),
                                new Signature.TypedObject("pitch", Token.PRIMITIVE_FLOAT, sound.pitch())),
                        Map.entry("name", Token.ofClass(Key.class)), Map.entry("source", Token.ofClass(Sound.Source.class)),
                        Map.entry("volume", Token.PRIMITIVE_FLOAT), Map.entry("pitch", Token.PRIMITIVE_FLOAT)).matchingNames()
                .matchingTypeHints().build();
    }

    public static @NotNull MappingProcessorSource getMappingProcessorSource() {
        return FeatureUtils.check(mappingProcessorSource);
    }

    private static class KeySignature extends PrioritizedBase implements ScalarSignature<Key> {
        private static final Token<Key> OBJECT_TYPE = Token.ofClass(Key.class);

        private KeySignature() {
            super(0);
        }

        @Override
        public @NotNull Token<Key> objectType() {
            return OBJECT_TYPE;
        }

        @Override
        public @NotNull ElementType elementType() {
            return ElementType.SCALAR;
        }

        @Override
        public @Nullable Key createScalar(@NotNull ConfigElement element) {
            @Subst("a")
            String keyString = element.asString();
            return Key.key(keyString);
        }

        @Override
        public @NotNull ConfigElement createElement(@Nullable Key key) {
            if (key == null) {
                return ConfigPrimitive.NULL;
            }

            return ConfigPrimitive.of(key.asString());
        }
    }

    private static class ComponentSignature extends PrioritizedBase implements ScalarSignature<Component> {
        private static final Token<Component> OBJECT_TYPE = Token.ofClass(Component.class);

        private ComponentSignature() {
            super(0);
        }

        @Override
        public @NotNull Token<Component> objectType() {
            return OBJECT_TYPE;
        }

        @Override
        public @NotNull ElementType elementType() {
            return ElementType.SCALAR;
        }

        @Override
        public @Nullable Component createScalar(@NotNull ConfigElement element) {
            return MiniMessage.miniMessage().deserialize(element.asString());
        }

        @Override
        public @NotNull ConfigElement createElement(@Nullable Component component) {
            if (component == null) {
                return ConfigPrimitive.NULL;
            }

            return ConfigPrimitive.of(MiniMessage.miniMessage().serialize(component));
        }
    }

    private static class UUIDSignature extends PrioritizedBase implements ScalarSignature<UUID> {
        private static final Token<UUID> OBJECT_TYPE = Token.ofClass(UUID.class);

        private UUIDSignature() {
            super(0);
        }

        @Override
        public @NotNull Token<UUID> objectType() {
            return OBJECT_TYPE;
        }

        @Override
        public @NotNull ElementType elementType() {
            return ElementType.SCALAR;
        }

        @Override
        public @Nullable UUID createScalar(@NotNull ConfigElement element) {
            return UUID.fromString(element.asString());
        }

        @Override
        public @NotNull ConfigElement createElement(@Nullable UUID uuid) {
            if (uuid == null) {
                return ConfigPrimitive.NULL;
            }

            return ConfigPrimitive.of(uuid.toString());
        }
    }
}
