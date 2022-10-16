package com.github.phantazmnetwork.zombies.game.map.shop.interactor;

import com.github.phantazmnetwork.zombies.game.map.Flaggable;
import com.github.phantazmnetwork.zombies.game.map.shop.PlayerInteraction;
import com.github.steanky.element.core.annotation.DataObject;
import com.github.steanky.element.core.annotation.Dependency;
import com.github.steanky.element.core.annotation.FactoryMethod;
import com.github.steanky.element.core.annotation.Model;
import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

@Model("zombies.map.shop.interactor.flag_setting")
public class FlagSettingInteractor extends InteractorBase<FlagSettingInteractor.Data> {
    private final Flaggable flaggable;

    @FactoryMethod
    public FlagSettingInteractor(@NotNull Data data,
            @NotNull @Dependency("zombies.dependency.map_object.flaggable") Flaggable flaggable) {
        super(data);
        this.flaggable = Objects.requireNonNull(flaggable, "flaggable");
    }

    @Override
    public void handleInteraction(@NotNull PlayerInteraction interaction) {
        if (data.remove) {
            flaggable.clearFlag(data.flag);
        }
        else {
            flaggable.setFlag(data.flag);
        }
    }

    @DataObject
    public record Data(@NotNull Key flag, boolean remove) {
    }
}
