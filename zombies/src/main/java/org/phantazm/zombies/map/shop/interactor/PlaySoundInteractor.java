package org.phantazm.zombies.map.shop.interactor;

import com.github.steanky.element.core.annotation.Cache;
import com.github.steanky.element.core.annotation.DataObject;
import com.github.steanky.element.core.annotation.FactoryMethod;
import com.github.steanky.element.core.annotation.Model;
import com.github.steanky.element.core.annotation.document.Description;
import net.kyori.adventure.sound.Sound;
import net.minestom.server.instance.Instance;
import org.jetbrains.annotations.NotNull;
import org.phantazm.zombies.map.shop.PlayerInteraction;

import java.util.Objects;

@Description("""
    Plays a Minecraft sound. Can either be specific to the player activating this shop, or global (instance-wide).
    """)
@Model("zombies.map.shop.interactor.play_sound")
@Cache(false)
public class PlaySoundInteractor extends InteractorBase<PlaySoundInteractor.Data> {
    private final Instance instance;

    @FactoryMethod
    public PlaySoundInteractor(@NotNull Data data, @NotNull Instance instance) {
        super(data);
        this.instance = Objects.requireNonNull(instance);
    }

    @Override
    public boolean handleInteraction(@NotNull PlayerInteraction interaction) {
        if (data.broadcast) {
            instance.playSound(data.sound);
        } else {
            interaction.player().module().getPlayerView().getPlayer().ifPresent(player -> player.playSound(data.sound));
        }

        return true;
    }

    @DataObject
    public record Data(@NotNull Sound sound, boolean broadcast) {
    }
}
