package com.github.phantazmnetwork.zombies.map.shop.interactor;

import com.github.phantazmnetwork.zombies.map.FlagAction;
import com.github.phantazmnetwork.zombies.map.Flaggable;
import com.github.phantazmnetwork.zombies.map.shop.PlayerInteraction;
import com.github.steanky.element.core.annotation.DataObject;
import com.github.steanky.element.core.annotation.FactoryMethod;
import com.github.steanky.element.core.annotation.Model;
import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.NotNull;

@Model("zombies.map.shop.interactor.player_flagging")
public class PlayerFlaggingInteractor extends InteractorBase<PlayerFlaggingInteractor.Data> {
    @FactoryMethod
    public PlayerFlaggingInteractor(@NotNull Data data) {
        super(data);
    }

    @Override
    public void handleInteraction(@NotNull PlayerInteraction interaction) {
        Flaggable flags = interaction.player().flags();
        switch (data.action) {
            case SET -> flags.setFlag(data.flag);
            case CLEAR -> flags.clearFlag(data.flag);
            case TOGGLE -> flags.toggleFlag(data.flag);
        }
    }

    @DataObject
    public record Data(@NotNull Key flag, @NotNull FlagAction action) {
    }
}
