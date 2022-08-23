package com.github.phantazmnetwork.zombies.game.map.shop.interactor;

import com.github.phantazmnetwork.commons.AdventureConfigProcessors;
import com.github.phantazmnetwork.commons.Prioritized;
import com.github.phantazmnetwork.commons.config.PrioritizedProcessor;
import com.github.phantazmnetwork.zombies.game.map.ZombiesMap;
import com.github.phantazmnetwork.zombies.game.map.shop.PlayerInteraction;
import com.github.steanky.element.core.annotation.*;
import com.github.steanky.ethylene.core.collection.ConfigNode;
import com.github.steanky.ethylene.core.collection.LinkedConfigNode;
import com.github.steanky.ethylene.core.processor.ConfigProcessException;
import com.github.steanky.ethylene.core.processor.ConfigProcessor;
import net.kyori.adventure.sound.Sound;
import org.jetbrains.annotations.NotNull;

@Model("zombies.map.shop.interactor.play_sound")
public class PlaySoundInteractor extends InteractorBase<PlaySoundInteractor.Data> {
    private static final ConfigProcessor<Data> PROCESSOR = new PrioritizedProcessor<>() {
        private static final ConfigProcessor<Sound> SOUND_PROCESSOR = AdventureConfigProcessors.sound();

        @Override
        public @NotNull Data finishData(@NotNull ConfigNode node, int priority) throws ConfigProcessException {
            Sound sound = SOUND_PROCESSOR.dataFromElement(node.getElementOrThrow("sound"));
            boolean broadcast = node.getBooleanOrThrow("broadcast");
            return new Data(priority, sound, broadcast);
        }

        @Override
        public @NotNull ConfigNode finishNode(@NotNull Data data) throws ConfigProcessException {
            ConfigNode node = new LinkedConfigNode(2);
            node.put("sound", SOUND_PROCESSOR.elementFromData(data.sound));
            node.putBoolean("broadcast", data.broadcast);
            return node;
        }
    };

    @FactoryMethod
    public PlaySoundInteractor(@NotNull Data data, @NotNull @Dependency("zombies.dependency.map") ZombiesMap map) {
        super(data, map);
    }

    @ProcessorMethod
    public static @NotNull ConfigProcessor<PlaySoundInteractor.Data> processor() {
        return PROCESSOR;
    }

    @Override
    public void handleInteraction(@NotNull PlayerInteraction interaction) {
        if (data.broadcast) {
            map.getInstance().playSound(data.sound, Sound.Emitter.self());
        }
        else {
            interaction.getPlayer().getPlayerView().getPlayer()
                    .ifPresent(player -> player.playSound(data.sound, Sound.Emitter.self()));
        }
    }

    @DataObject
    public record Data(int priority, @NotNull Sound sound, boolean broadcast) implements Prioritized {
    }
}
