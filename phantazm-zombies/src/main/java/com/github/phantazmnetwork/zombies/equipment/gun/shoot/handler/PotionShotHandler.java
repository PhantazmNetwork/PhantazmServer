package com.github.phantazmnetwork.zombies.equipment.gun.shoot.handler;

import com.github.phantazmnetwork.api.config.processor.MinestomConfigProcessors;
import com.github.phantazmnetwork.commons.Namespaces;
import com.github.phantazmnetwork.mob.PhantazmMob;
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
import net.minestom.server.entity.Player;
import net.minestom.server.potion.Potion;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Objects;

public class PotionShotHandler implements ShotHandler {

    public record Data(@NotNull Potion potion, @NotNull Potion headshotPotion) implements Keyed {

        public static final Key SERIAL_KEY = Key.key(Namespaces.PHANTAZM, "gun.hit_handler.potion");

        @Override
        public @NotNull Key key() {
            return SERIAL_KEY;
        }
    }

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

    private final Data data;

    public PotionShotHandler(@NotNull Data data) {
        this.data = Objects.requireNonNull(data, "data");
    }

    @Override
    public void handle(@NotNull GunState state, @NotNull Player attacker, @NotNull Collection<PhantazmMob> previousHits, @NotNull GunShot shot) {
        for (GunHit target : shot.regularTargets()) {
            target.mob().entity().addEffect(data.potion());
        }
        for (GunHit target : shot.headshotTargets()) {
            target.mob().entity().addEffect(data.headshotPotion());
        }
    }

    @Override
    public void tick(@NotNull GunState state, long time) {

    }

    @Override
    public @NotNull Keyed getData() {
        return data;
    }
}
