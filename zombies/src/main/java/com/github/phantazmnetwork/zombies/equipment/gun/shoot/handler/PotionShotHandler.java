package com.github.phantazmnetwork.zombies.equipment.gun.shoot.handler;

import com.github.phantazmnetwork.commons.Namespaces;
import com.github.phantazmnetwork.core.config.processor.MinestomConfigProcessors;
import com.github.phantazmnetwork.zombies.equipment.gun.GunState;
import com.github.phantazmnetwork.zombies.equipment.gun.shoot.GunHit;
import com.github.phantazmnetwork.zombies.equipment.gun.shoot.GunShot;
import com.github.steanky.ethylene.core.ConfigElement;
import com.github.steanky.ethylene.core.collection.ConfigNode;
import com.github.steanky.ethylene.core.collection.LinkedConfigNode;
import com.github.steanky.ethylene.core.processor.ConfigProcessException;
import com.github.steanky.ethylene.core.processor.ConfigProcessor;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.key.Keyed;
import net.minestom.server.entity.Entity;
import net.minestom.server.potion.Potion;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Objects;
import java.util.UUID;

/**
 * A {@link ShotHandler} that applies a {@link Potion} to {@link Entity} targets.
 */
public class PotionShotHandler implements ShotHandler {

    private final Data data;

    /**
     * Creates a {@link PotionShotHandler}.
     *
     * @param data The {@link PotionShotHandler}'s {@link Data}
     */
    public PotionShotHandler(@NotNull Data data) {
        this.data = Objects.requireNonNull(data, "data");
    }

    /**
     * Creates a {@link ConfigProcessor} for {@link Data}s.
     *
     * @return A {@link ConfigProcessor} for {@link Data}s
     */
    public static @NotNull ConfigProcessor<Data> processor() {
        ConfigProcessor<Potion> potionProcessor = MinestomConfigProcessors.potion();

        return new ConfigProcessor<>() {

            @Override
            public @NotNull Data dataFromElement(@NotNull ConfigElement element) throws ConfigProcessException {
                Potion potion = potionProcessor.dataFromElement(element.getElementOrThrow("potion"));
                Potion headshotPotion = potionProcessor.dataFromElement(element.getElementOrThrow("headshotPotion"));
                return new Data(potion, headshotPotion);
            }

            @Override
            public @NotNull ConfigElement elementFromData(@NotNull Data data) throws ConfigProcessException {
                ConfigNode node = new LinkedConfigNode(2);
                node.put("potion", potionProcessor.elementFromData(data.potion));
                node.put("headshotPotion", potionProcessor.elementFromData(data.headshotPotion));

                return node;
            }
        };
    }

    @Override
    public void handle(@NotNull GunState state, @NotNull Entity attacker, @NotNull Collection<UUID> previousHits,
                       @NotNull GunShot shot) {
        for (GunHit target : shot.regularTargets()) {
            target.entity().addEffect(data.potion());
        }
        for (GunHit target : shot.headshotTargets()) {
            target.entity().addEffect(data.headshotPotion());
        }
    }

    @Override
    public void tick(@NotNull GunState state, long time) {

    }

    /**
     * Data for a {@link PotionShotHandler}.
     *
     * @param potion         The {@link Potion} to apply to regular {@link Entity} targets
     * @param headshotPotion The {@link Potion} to apply to headshot {@link Entity} targets
     */
    public record Data(@NotNull Potion potion, @NotNull Potion headshotPotion) implements Keyed {

        /**
         * The serial {@link Key} of this {@link Data}.
         */
        public static final Key SERIAL_KEY = Key.key(Namespaces.PHANTAZM, "gun.shot_handler.potion");

        @Override
        public @NotNull Key key() {
            return SERIAL_KEY;
        }
    }

}
