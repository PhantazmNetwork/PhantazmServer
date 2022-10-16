package com.github.phantazmnetwork.zombies.game.map.shop.interactor;

import com.github.phantazmnetwork.zombies.game.map.shop.PlayerInteraction;
import com.github.steanky.element.core.annotation.DataObject;
import com.github.steanky.element.core.annotation.Dependency;
import com.github.steanky.element.core.annotation.FactoryMethod;
import com.github.steanky.element.core.annotation.Model;
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

    @Override
    public void handleInteraction(@NotNull PlayerInteraction interaction) {
        if (data.broadcast) {
            instance.playSound(data.sound, Sound.Emitter.self());
        }
        else {
            interaction.player().getModule().getPlayerView().getPlayer()
                    .ifPresent(player -> player.playSound(data.sound, Sound.Emitter.self()));
        }
    }

    @DataObject
    public record Data(@NotNull Sound sound, boolean broadcast) {
    }
}
