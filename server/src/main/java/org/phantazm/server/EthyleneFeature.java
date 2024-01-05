package org.phantazm.server;

import com.github.steanky.element.core.key.KeyParser;
import com.github.steanky.ethylene.core.ConfigPrimitive;
import com.github.steanky.ethylene.core.collection.ConfigList;
import com.github.steanky.ethylene.mapper.MappingProcessorSource;
import com.github.steanky.ethylene.mapper.signature.ScalarSignature;
import com.github.steanky.ethylene.mapper.signature.Signature;
import com.github.steanky.ethylene.mapper.signature.SignatureParameter;
import com.github.steanky.ethylene.mapper.type.Token;
import it.unimi.dsi.fastutil.ints.*;
import it.unimi.dsi.fastutil.objects.*;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.EntityType;
import net.minestom.server.instance.block.Block;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.particle.Particle;
import net.minestom.server.permission.Permission;
import net.minestom.server.potion.PotionEffect;
import org.jetbrains.annotations.NotNull;
import org.jglrxavpok.hephaistos.nbt.NBTCompound;
import org.jglrxavpok.hephaistos.nbt.NBTException;
import org.jglrxavpok.hephaistos.parser.SNBTParser;
import org.phantazm.commons.Signatures;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.StringReader;
import java.nio.file.Path;
import java.util.*;

/**
 * Initializes features related to Ethylene.
 */
public final class EthyleneFeature {
    private static final Logger LOGGER = LoggerFactory.getLogger(MobFeature.class);

    private static MappingProcessorSource mappingProcessorSource;
    private static KeyParser keyParser;

    static void initialize(@NotNull KeyParser keyParser) {
        EthyleneFeature.keyParser = Objects.requireNonNull(keyParser);

        mappingProcessorSource = Signatures.core(MappingProcessorSource.builder()).withCustomSignature(basicItemStack())
            .withCustomSignature(pos()).withScalarSignature(key())
            .withScalarSignature(itemStack())
            .withScalarSignature(particle()).withScalarSignature(block())
            .withScalarSignature(permission())
            .withScalarSignature(entityType()).withScalarSignature(material())
            .withScalarSignature(potionEffect())
            .withScalarSignature(path())
            .withTypeImplementation(IntArrayList.class, IntList.class)
            .withTypeImplementation(Object2IntOpenHashMap.class, Object2IntMap.class)
            .withTypeImplementation(Object2FloatOpenHashMap.class, Object2FloatMap.class)
            .withTypeImplementation(IntOpenHashSet.class, IntSet.class)
            .withTypeImplementation(ObjectLinkedOpenHashSet.class, ObjectSortedSet.class)
            .withTypeImplementation(Int2IntOpenHashMap.class, Int2IntMap.class)
            .withTypeImplementation(Int2ObjectOpenHashMap.class, Int2ObjectMap.class)
            .withStandardSignatures()
            .withStandardTypeImplementations().ignoringLengths().build();

        LOGGER.info("Ethylene initialized.");
    }

    @SuppressWarnings("PatternValidation")
    private static ScalarSignature<Key> key() {
        return ScalarSignature.of(Token.ofClass(Key.class), element -> keyParser.parseKey(element.asString()),
            key -> key == null ? ConfigPrimitive.NULL : ConfigPrimitive.of(key.asString()));
    }

    @SuppressWarnings("unchecked")
    private static Signature<ItemStack> basicItemStack() {
        return Signature.builder(Token.ofClass(ItemStack.class), (ignored, args) -> {
                    ItemStack.Builder builder = ItemStack.builder(args.get(0));
                    String meta = args.get(3);
                    if (meta != null) {
                        try {
                            builder.meta((NBTCompound) new SNBTParser(new StringReader(meta)).parse());
                        } catch (NBTException exception) {
                            LOGGER.warn("NBTException when deserializing to ItemStack", exception);
                        }
                    }

                    builder.displayName(args.get(1)).lore((List<? extends Component>) args.get(2));
                    return builder.build();
                }, itemStack -> {
                    List<Object> list = new ArrayList<>(4);
                    list.add(itemStack.material());
                    list.add(itemStack.getDisplayName());
                    list.add(itemStack.getLore());
                    list.add(itemStack.meta().toSNBT());

                    return list;
                }, Map.entry("material", SignatureParameter.parameter(Token.ofClass(Material.class))), Map.entry("displayName",
                    SignatureParameter.parameter(Token.ofClass(Component.class), ConfigPrimitive.NULL)),
                Map.entry("lore", SignatureParameter.parameter(new Token<List<Component>>() {
                }, ConfigList.of())),
                Map.entry("tag", SignatureParameter.parameter(Token.STRING, ConfigPrimitive.NULL))).matchingNames()
            .matchingTypeHints().build();
    }

    private static Signature<Pos> pos() {
        return Signature.builder(Token.ofClass(Pos.class),
                (ignored, args) -> new Pos(args.get(0), args.get(1), args.get(2), args.get(3), args.get(4)),
                pos -> List.of(pos.x(), pos.y(), pos.z(), pos.yaw(), pos.pitch()),
                Map.entry("x", SignatureParameter.parameter(Token.DOUBLE)),
                Map.entry("y", SignatureParameter.parameter(Token.DOUBLE)),
                Map.entry("z", SignatureParameter.parameter(Token.DOUBLE)),
                Map.entry("yaw", SignatureParameter.parameter(Token.FLOAT)),
                Map.entry("pitch", SignatureParameter.parameter(Token.FLOAT))).matchingNames().matchingTypeHints()
            .build();
    }

    private static ScalarSignature<PotionEffect> potionEffect() {
        return ScalarSignature.of(Token.ofClass(PotionEffect.class),
            effect -> PotionEffect.fromNamespaceId(effect.asString()),
            effect -> effect == null ? ConfigPrimitive.NULL : ConfigPrimitive.of(effect.namespace().asString()));
    }

    private static ScalarSignature<Permission> permission() {
        return ScalarSignature.of(Token.ofClass(Permission.class), element -> new Permission(element.asString()),
            permission -> permission == null
                ? ConfigPrimitive.NULL
                : ConfigPrimitive.of(permission.getPermissionName()));
    }

    private static ScalarSignature<Material> material() {
        return ScalarSignature.of(Token.ofClass(Material.class),
            element -> Material.fromNamespaceId(element.asString()),
            material -> material == null ? ConfigPrimitive.NULL : ConfigPrimitive.of(material.key().toString()));
    }

    private static ScalarSignature<Particle> particle() {
        return ScalarSignature.of(Token.ofClass(Particle.class),
            element -> Particle.fromNamespaceId(element.asString()),
            particle -> particle == null ? ConfigPrimitive.NULL : ConfigPrimitive.of(particle.key().toString()));
    }

    private static ScalarSignature<ItemStack> itemStack() {
        return ScalarSignature.of(Token.ofClass(ItemStack.class), element -> {
            try {
                return ItemStack.fromItemNBT(
                    (NBTCompound) new SNBTParser(new StringReader(element.asString())).parse());
            } catch (NBTException e) {
                LOGGER.warn("Error deserializing SNBT", e);
                return ItemStack.AIR;
            }
        }, itemStack -> itemStack == null ? ConfigPrimitive.NULL : ConfigPrimitive.of(itemStack.toItemNBT().toSNBT()));
    }

    private static ScalarSignature<EntityType> entityType() {
        return ScalarSignature.of(Token.ofClass(EntityType.class), element -> {
            return EntityType.fromNamespaceId(element.asString());
        }, entityType -> entityType == null
            ? ConfigPrimitive.NULL
            : ConfigPrimitive.of(entityType.namespace().asString()));
    }

    private static ScalarSignature<Path> path() {
        return ScalarSignature.of(Token.ofClass(Path.class), element -> {
            return Path.of(element.asString());
        }, entityType -> entityType == null
            ? ConfigPrimitive.NULL
            : ConfigPrimitive.of(entityType.toString()));
    }

    private static ScalarSignature<Block> block() {
        return ScalarSignature.of(Token.ofClass(Block.class), element -> {
            return Objects.requireNonNullElse(Block.fromNamespaceId(element.asString()), Block.AIR);
        }, block -> block == null ? ConfigPrimitive.NULL : ConfigPrimitive.of(block.name()));
    }

    public static @NotNull MappingProcessorSource getMappingProcessorSource() {
        return FeatureUtils.check(mappingProcessorSource);
    }
}
