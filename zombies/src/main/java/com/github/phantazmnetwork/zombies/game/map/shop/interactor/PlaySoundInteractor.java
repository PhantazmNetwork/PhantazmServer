package com.github.phantazmnetwork.zombies.game.map.shop.interactor;

import com.github.phantazmnetwork.commons.ConfigProcessors;
import com.github.phantazmnetwork.zombies.game.map.shop.PlayerInteraction;
import com.github.steanky.element.core.annotation.*;
import com.github.steanky.ethylene.core.ConfigElement;
import com.github.steanky.ethylene.core.collection.ConfigNode;
import com.github.steanky.ethylene.core.processor.ConfigProcessException;
import com.github.steanky.ethylene.core.processor.ConfigProcessor;
import net.kyori.adventure.sound.Sound;
import net.minestom.server.instance.Instance;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

@Model("zombies.map.shop.interactor.play_sound")
public class PlaySoundInteractor extends InteractorBase<PlaySoundInteractor.Data> {
    private final Instance instance;

    @FactoryMethod
    public PlaySoundInteractor(@NotNull Data data,
            @NotNull @Dependency("zombies.dependency.map_object.instance") Instance instance) {
        super(data);
        this.instance = Objects.requireNonNull(instance, "instance");
    }

    @ProcessorMethod
    public static @NotNull ConfigProcessor<PlaySoundInteractor.Data> processor() {
        return new ConfigProcessor<>() {
            private static final ConfigProcessor<Sound> SOUND_PROCESSOR = ConfigProcessors.sound();

            @Override
            public @NotNull Data dataFromElement(@NotNull ConfigElement element) throws ConfigProcessException {
                Sound sound = SOUND_PROCESSOR.dataFromElement(element.getElementOrThrow("sound"));
                boolean broadcast = element.getBooleanOrThrow("broadcast");
                return new Data(sound, broadcast);
            }

            @Override
            public @NotNull ConfigElement elementFromData(@NotNull Data data) throws ConfigProcessException {
                return ConfigNode.of("sound", SOUND_PROCESSOR.elementFromData(data.sound), "broadcast", data.broadcast);
            }
        };
    }

    @Override
    public void handleInteraction(@NotNull PlayerInteraction interaction) {
        if (data.broadcast) {
            instance.playSound(data.sound, Sound.Emitter.self());
        }
        else {
            interaction.player().getPlayerView().getPlayer()
                    .ifPresent(player -> player.playSound(data.sound, Sound.Emitter.self()));
        }
    }

    @DataObject
    public record Data(@NotNull Sound sound, boolean broadcast) {
    }
}
