package com.github.phantazmnetwork.zombies.game.map.shop.gui;

import com.github.phantazmnetwork.core.gui.Gui;
import com.github.phantazmnetwork.core.gui.GuiItem;
import com.github.phantazmnetwork.zombies.game.player.ZombiesPlayer;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.UUID;
import java.util.function.Function;

public abstract class ClickHandlerBase<TData> implements GuiItem {
    protected final TData data;
    protected final Function<? super UUID, ? extends ZombiesPlayer> playerFunction;

    public ClickHandlerBase(@NotNull TData data,
            @NotNull Function<? super UUID, ? extends ZombiesPlayer> playerFunction) {
        this.data = Objects.requireNonNull(data, "data");
        this.playerFunction = Objects.requireNonNull(playerFunction, "playerFunction");
    }

    @Override
    public void onRemove(@NotNull Gui owner, int slot) {

    }

    @Override
    public void onReplace(@NotNull Gui owner, @NotNull GuiItem newItem, int slot) {

    }
}
