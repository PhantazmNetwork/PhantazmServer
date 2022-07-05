package com.github.phantazmnetwork.zombies.equipment.gun.shoot.handler;

import com.github.phantazmnetwork.commons.AdventureConfigProcessors;
import com.github.phantazmnetwork.commons.Namespaces;
import com.github.phantazmnetwork.mob.PhantazmMob;
import com.github.phantazmnetwork.zombies.equipment.gun.GunState;
import com.github.phantazmnetwork.zombies.equipment.gun.shoot.GunShot;
import com.github.steanky.ethylene.core.ConfigElement;
import com.github.steanky.ethylene.core.collection.ConfigNode;
import com.github.steanky.ethylene.core.collection.LinkedConfigNode;
import com.github.steanky.ethylene.core.processor.ConfigProcessException;
import com.github.steanky.ethylene.core.processor.ConfigProcessor;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.key.Keyed;
import net.kyori.adventure.sound.Sound;
import net.minestom.server.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Objects;

public class SoundShotHandler implements ShotHandler {

    public record Data(@NotNull Sound sound, @NotNull Sound headshotSound) implements Keyed {

        public static final Key SERIAL_KEY = Key.key(Namespaces.PHANTAZM, "gun.hit_handler.sound");

        public Data {
            Objects.requireNonNull(sound, "sound");
            Objects.requireNonNull(headshotSound, "headshotSound");
        }

        @Override
        public @NotNull Key key() {
            return SERIAL_KEY;
        }
    }

    public static @NotNull ConfigProcessor<Data> processor() {
        ConfigProcessor<Sound> soundProcessor = AdventureConfigProcessors.sound();

        return new ConfigProcessor<>() {

            @Override
            public @NotNull Data dataFromElement(@NotNull ConfigElement element) throws ConfigProcessException {
                Sound sound = soundProcessor.dataFromElement(element.getElementOrThrow("sound"));
                Sound headshotSound = soundProcessor.dataFromElement(element.getElementOrThrow("headshotSound"));
                return new Data(sound, headshotSound);
            }

            @Override
            public @NotNull ConfigElement elementFromData(@NotNull Data data) throws ConfigProcessException {
                ConfigNode node = new LinkedConfigNode(2);
                node.put("sound", soundProcessor.elementFromData(data.sound));
                node.put("headshotSound", soundProcessor.elementFromData(data.headshotSound));

                return node;
            }
        };
    }

    private final Data data;

    public SoundShotHandler(@NotNull Data data) {
        this.data = Objects.requireNonNull(data, "data");
    }

    @Override
    public void handle(@NotNull GunState state, @NotNull Player attacker, @NotNull Collection<PhantazmMob> previousHits, @NotNull GunShot shot) {
        if (!shot.regularTargets().isEmpty()) {
            attacker.playSound(data.sound(), Sound.Emitter.self());
        }
        else if (!shot.headshotTargets().isEmpty()) {
            attacker.playSound(data.headshotSound(), Sound.Emitter.self());
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
