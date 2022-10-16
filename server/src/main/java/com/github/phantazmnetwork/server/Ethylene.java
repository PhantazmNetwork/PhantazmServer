package com.github.phantazmnetwork.server;

import com.github.phantazmnetwork.commons.vector.Vec3D;
import com.github.phantazmnetwork.commons.vector.Vec3F;
import com.github.phantazmnetwork.commons.vector.Vec3I;
import com.github.steanky.ethylene.core.ConfigPrimitive;
import com.github.steanky.ethylene.mapper.MappingProcessorSource;
import com.github.steanky.ethylene.mapper.signature.ScalarSignature;
import com.github.steanky.ethylene.mapper.signature.Signature;
import com.github.steanky.ethylene.mapper.type.Token;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.title.TitlePart;
import net.minestom.server.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jglrxavpok.hephaistos.nbt.NBTCompound;
import org.jglrxavpok.hephaistos.nbt.NBTException;
import org.jglrxavpok.hephaistos.parser.SNBTParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.StringReader;
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
                        .withCustomSignature(sound()).withCustomSignature(vec3D()).withScalarSignature(key())
                        .withScalarSignature(uuid()).withScalarSignature(component()).withScalarSignature(itemStack())
                        .withScalarSignature(titlePartComponent())
                        .withTypeImplementation(Object2IntOpenHashMap.class, Object2IntMap.class)
                        .withStandardSignatures().withStandardTypeImplementations().build();

        LOGGER.info("Ethylene initialized.");
    }

    private static Signature<Vec3I> vec3I() {
        return Signature.builder(Token.ofClass(Vec3I.class),
                        (ignored, args) -> Vec3I.of((int)args[0], (int)args[1], (int)args[2]),
                        vec -> List.of(vec.getX(), vec.getY(), vec.getZ()), Map.entry("x", Token.PRIMITIVE_INT),
                        Map.entry("y", Token.PRIMITIVE_INT), Map.entry("z", Token.PRIMITIVE_INT)).matchingNames()
                .matchingTypeHints().build();
    }

    private static Signature<Vec3F> vec3F() {
        return Signature.builder(Token.ofClass(Vec3F.class),
                        (ignored, args) -> Vec3F.of((float)args[0], (float)args[1], (float)args[2]),
                        vec -> List.of(vec.getX(), vec.getY(), vec.getZ()), Map.entry("x", Token.PRIMITIVE_FLOAT),
                        Map.entry("y", Token.PRIMITIVE_FLOAT), Map.entry("z", Token.PRIMITIVE_FLOAT)).matchingNames()
                .matchingTypeHints().build();
    }

    private static Signature<Vec3D> vec3D() {
        return Signature.builder(Token.ofClass(Vec3D.class),
                        (ignored, args) -> Vec3D.of((double)args[0], (double)args[1], (double)args[2]),
                        vec -> List.of(vec.getX(), vec.getY(), vec.getZ()), Map.entry("x", Token.PRIMITIVE_DOUBLE),
                        Map.entry("y", Token.PRIMITIVE_DOUBLE), Map.entry("z", Token.PRIMITIVE_DOUBLE)).matchingNames()
                .matchingTypeHints().build();
    }

    private static Signature<Sound> sound() {
        return Signature.builder(Token.ofClass(Sound.class),
                        (ignored, args) -> Sound.sound((Key)args[0], (Sound.Source)args[1], (float)args[2], (float)args[3]),
                        sound -> List.of(sound.name(), sound.source(), sound.volume(), sound.pitch()),
                        Map.entry("name", Token.ofClass(Key.class)), Map.entry("source", Token.ofClass(Sound.Source.class)),
                        Map.entry("volume", Token.PRIMITIVE_FLOAT), Map.entry("pitch", Token.PRIMITIVE_FLOAT)).matchingNames()
                .matchingTypeHints().build();
    }

    private static ScalarSignature<ItemStack> itemStack() {
        return ScalarSignature.of(Token.ofClass(ItemStack.class), element -> {
            try {
                return ItemStack.fromItemNBT((NBTCompound)new SNBTParser(new StringReader(element.asString())).parse());
            }
            catch (NBTException e) {
                throw new RuntimeException(e);
            }
        }, itemStack -> itemStack == null ? ConfigPrimitive.NULL : ConfigPrimitive.of(itemStack.toItemNBT().toSNBT()));
    }

    private static ScalarSignature<TitlePart<Component>> titlePartComponent() {
        return ScalarSignature.of(new Token<>() {
        }, element -> {
            String value = element.asString();
            return switch (value) {
                case "TITLE" -> TitlePart.TITLE;
                case "SUBTITLE" -> TitlePart.SUBTITLE;
                default -> throw new IllegalArgumentException("Unexpected TitlePart '" + value + "'");
            };
        }, titlePart -> titlePart == null
                        ? ConfigPrimitive.NULL
                        : titlePart == TitlePart.TITLE ? ConfigPrimitive.of("TITLE") : ConfigPrimitive.of("SUBTITLE"));
    }

    public static @NotNull MappingProcessorSource getMappingProcessorSource() {
        return FeatureUtils.check(mappingProcessorSource);
    }

    @SuppressWarnings("PatternValidation")
    private static ScalarSignature<Key> key() {
        return ScalarSignature.of(Token.ofClass(Key.class), element -> Key.key(element.asString()),
                key -> key == null ? ConfigPrimitive.NULL : ConfigPrimitive.of(key.asString()));
    }

    private static ScalarSignature<Component> component() {
        return ScalarSignature.of(Token.ofClass(Component.class),
                element -> MiniMessage.miniMessage().deserialize(element.asString()), component -> component == null
                                                                                                   ? ConfigPrimitive.NULL
                                                                                                   : ConfigPrimitive.of(
                                                                                                           MiniMessage.miniMessage()
                                                                                                                   .serialize(
                                                                                                                           component)));
    }

    private static ScalarSignature<UUID> uuid() {
        return ScalarSignature.of(Token.ofClass(UUID.class), element -> UUID.fromString(element.asString()),
                uuid -> uuid == null ? ConfigPrimitive.NULL : ConfigPrimitive.of(uuid.toString()));
    }
}
