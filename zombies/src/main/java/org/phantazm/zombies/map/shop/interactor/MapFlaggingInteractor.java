package org.phantazm.zombies.map.shop.interactor;

import com.github.steanky.element.core.annotation.Cache;
import com.github.steanky.element.core.annotation.DataObject;
import com.github.steanky.element.core.annotation.FactoryMethod;
import com.github.steanky.element.core.annotation.Model;
import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.NotNull;
import org.phantazm.zombies.map.FlagAction;
import org.phantazm.zombies.map.Flaggable;
import org.phantazm.zombies.map.shop.PlayerInteraction;

import java.util.Objects;

@Model("zombies.map.shop.interactor.map_flagging")
@Cache(false)
public class MapFlaggingInteractor extends InteractorBase<MapFlaggingInteractor.Data> {
    private final Flaggable flaggable;

    @FactoryMethod
    public MapFlaggingInteractor(@NotNull Data data, @NotNull Flaggable flaggable) {
        super(data);
        this.flaggable = Objects.requireNonNull(flaggable, "flaggable");
    }

    @Override
    public boolean handleInteraction(@NotNull PlayerInteraction interaction) {
        switch (data.action) {
            case SET -> flaggable.setFlag(data.flag);
            case CLEAR -> flaggable.clearFlag(data.flag);
            case TOGGLE -> flaggable.toggleFlag(data.flag);
        }

        return true;
    }

    @DataObject
    public record Data(@NotNull Key flag, @NotNull FlagAction action) {
    }
}
