package com.github.phantazmnetwork.zombies.map.shop.interactor;

import com.github.phantazmnetwork.zombies.map.FlagAction;
import com.github.phantazmnetwork.zombies.map.Flaggable;
import com.github.phantazmnetwork.zombies.map.shop.PlayerInteraction;
import com.github.steanky.element.core.annotation.DataObject;
import com.github.steanky.element.core.annotation.Dependency;
import com.github.steanky.element.core.annotation.FactoryMethod;
import com.github.steanky.element.core.annotation.Model;
import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

@Model("zombies.map.shop.interactor.map_flagging")
public class MapFlaggingInteractor extends InteractorBase<MapFlaggingInteractor.Data> {
    private final Flaggable flaggable;

    @FactoryMethod
    public MapFlaggingInteractor(@NotNull Data data,
            @NotNull @Dependency("zombies.dependency.map_object.flaggable") Flaggable flaggable) {
        super(data);
        this.flaggable = Objects.requireNonNull(flaggable, "flaggable");
    }

    @Override
    public void handleInteraction(@NotNull PlayerInteraction interaction) {
        switch (data.action) {
            case SET -> flaggable.setFlag(data.flag);
            case CLEAR -> flaggable.clearFlag(data.flag);
            case TOGGLE -> flaggable.toggleFlag(data.flag);
        }
    }

    @DataObject
    public record Data(@NotNull Key flag, @NotNull FlagAction action) {
    }
}
