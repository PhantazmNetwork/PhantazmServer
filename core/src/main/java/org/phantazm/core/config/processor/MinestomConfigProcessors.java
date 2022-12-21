package org.phantazm.core.config.processor;

import com.github.steanky.ethylene.core.ConfigElement;
import com.github.steanky.ethylene.core.ConfigPrimitive;
import com.github.steanky.ethylene.core.collection.ConfigNode;
import com.github.steanky.ethylene.core.collection.LinkedConfigNode;
import com.github.steanky.ethylene.core.processor.ConfigProcessException;
import com.github.steanky.ethylene.core.processor.ConfigProcessor;
import com.github.steanky.vector.Vec3D;
import net.kyori.adventure.key.Key;
import net.minestom.server.coordinate.Point;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.metadata.villager.VillagerMeta;
import net.minestom.server.particle.Particle;
import net.minestom.server.potion.Potion;
import net.minestom.server.potion.PotionEffect;
import net.minestom.server.registry.ProtocolObject;
import net.minestom.server.utils.NamespaceID;
import org.jetbrains.annotations.NotNull;
import org.jglrxavpok.hephaistos.nbt.NBT;
import org.jglrxavpok.hephaistos.nbt.NBTException;
import org.jglrxavpok.hephaistos.parser.SNBTParser;
import org.phantazm.commons.ConfigProcessors;
import org.phantazm.commons.vector.VectorConfigProcessors;
import org.phantazm.core.VecUtils;

import java.io.StringReader;
import java.util.Objects;
import java.util.function.Function;

/**
 * {@link ConfigProcessor}s for Minestom-specific objects that are serializable.
 */
public class MinestomConfigProcessors {

    private static final ConfigProcessor<EntityType> ENTITY_TYPE =
            new ProtocolObjectConfigProcessor<>(EntityType::fromNamespaceId);
    private static final ConfigProcessor<Particle> PARTICLE =
            new ProtocolObjectConfigProcessor<>(Particle::fromNamespaceId);
    private static final ConfigProcessor<PotionEffect> POTION_EFFECT =
            new ProtocolObjectConfigProcessor<>(PotionEffect::fromNamespaceId);
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
    private static final ConfigProcessor<Point> POINT = new ConfigProcessor<>() {

        private static final ConfigProcessor<Vec3D> VECTOR_PROCESSOR = VectorConfigProcessors.vec3D();

        @Override
        public @NotNull Point dataFromElement(@NotNull ConfigElement element) throws ConfigProcessException {
            return VecUtils.toPoint(VECTOR_PROCESSOR.dataFromElement(element));
        }

        @Override
        public @NotNull ConfigElement elementFromData(@NotNull Point point) throws ConfigProcessException {
            return VECTOR_PROCESSOR.elementFromData(VecUtils.toDouble(point));
        }
    };
    private static final ConfigProcessor<NBT> NBT = new ConfigProcessor<>() {
        @Override
        public @NotNull NBT dataFromElement(@NotNull ConfigElement element) throws ConfigProcessException {
            String nbtString = ConfigProcessor.STRING.dataFromElement(element);
            NBT nbt;
            try {
                nbt = new SNBTParser(new StringReader(nbtString)).parse();
            }
            catch (NBTException e) {
                throw new ConfigProcessException(e);
            }

            return nbt;
        }

        @Override
        public @NotNull ConfigElement elementFromData(@NotNull NBT compound) {
            return ConfigPrimitive.of(compound.toSNBT());
        }
    };
    private static final ConfigProcessor<VillagerMeta.VillagerData> VILLAGER_DATA = new ConfigProcessor<>() {

        private static final ConfigProcessor<VillagerMeta.Type> TYPE_PROCESSOR =
                ConfigProcessor.enumProcessor(VillagerMeta.Type.class);

        private static final ConfigProcessor<VillagerMeta.Profession> PROFESSION_PROCESSOR =
                ConfigProcessor.enumProcessor(VillagerMeta.Profession.class);

        private static final ConfigProcessor<VillagerMeta.Level> LEVEL_PROCESSOR =
                ConfigProcessor.enumProcessor(VillagerMeta.Level.class);

        @Override
        public @NotNull VillagerMeta.VillagerData dataFromElement(@NotNull ConfigElement element)
                throws ConfigProcessException {
            VillagerMeta.Type type = TYPE_PROCESSOR.dataFromElement(element.getElementOrThrow("type"));
            VillagerMeta.Profession profession =
                    PROFESSION_PROCESSOR.dataFromElement(element.getElementOrThrow("profession"));
            VillagerMeta.Level level = LEVEL_PROCESSOR.dataFromElement(element.getElementOrThrow("level"));

            return new VillagerMeta.VillagerData(type, profession, level);
        }

        @Override
        public @NotNull ConfigElement elementFromData(@NotNull VillagerMeta.VillagerData villagerData)
                throws ConfigProcessException {
            return ConfigNode.of("type", TYPE_PROCESSOR.elementFromData(villagerData.getType()), "profession",
                    PROFESSION_PROCESSOR.elementFromData(villagerData.getProfession()), "level",
                    LEVEL_PROCESSOR.elementFromData(villagerData.getLevel()));
        }
    };

    private MinestomConfigProcessors() {
        throw new UnsupportedOperationException();
    }

    /**
     * Gets a {@link ConfigProcessor} for {@link EntityType}s.
     *
     * @return A {@link ConfigProcessor} for {@link EntityType}s
     */
    public static @NotNull ConfigProcessor<EntityType> entityType() {
        return ENTITY_TYPE;
    }

    /**
     * Gets a {@link ConfigProcessor} for {@link Particle}s.
     *
     * @return A {@link ConfigProcessor} for {@link Particle}s
     */
    public static @NotNull ConfigProcessor<Particle> particle() {
        return PARTICLE;
    }

    /**
     * Gets a {@link ConfigProcessor} for {@link PotionEffect}s.
     *
     * @return A {@link ConfigProcessor} for {@link PotionEffect}s
     */
    @SuppressWarnings("unused")
    public static @NotNull ConfigProcessor<PotionEffect> potionEffect() {
        return POTION_EFFECT;
    }

    /**
     * Gets a {@link ConfigProcessor} for {@link Potion}s.
     *
     * @return A {@link ConfigProcessor} for {@link Potion}s
     */
    public static @NotNull ConfigProcessor<Potion> potion() {
        return POTION;
    }

    public static @NotNull ConfigProcessor<Point> point() {
        return POINT;
    }

    public static @NotNull ConfigProcessor<NBT> nbt() {
        return NBT;
    }

    public static @NotNull ConfigProcessor<VillagerMeta.VillagerData> villagerData() {
        return VILLAGER_DATA;
    }

    @SuppressWarnings("ClassCanBeRecord")
    private static class ProtocolObjectConfigProcessor<TObject extends ProtocolObject>
            implements ConfigProcessor<TObject> {

        private static final ConfigProcessor<Key> KEY_PROCESSOR = ConfigProcessors.key();

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

}
