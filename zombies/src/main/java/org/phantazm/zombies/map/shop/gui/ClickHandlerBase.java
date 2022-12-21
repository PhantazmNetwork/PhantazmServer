package org.phantazm.zombies.map.shop.gui;

import org.jetbrains.annotations.NotNull;
import org.phantazm.core.gui.Gui;
import org.phantazm.core.gui.GuiItem;
import org.phantazm.zombies.player.ZombiesPlayer;

import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public abstract class ClickHandlerBase<TData> implements GuiItem {
    protected final TData data;
    protected final Map<? super UUID, ? extends ZombiesPlayer> playerMap;

    public ClickHandlerBase(@NotNull TData data, @NotNull Map<? super UUID, ? extends ZombiesPlayer> playerMap) {
        this.data = Objects.requireNonNull(data, "data");
        this.playerMap = Objects.requireNonNull(playerMap, "playerMap");
    }

    @Override
    public void onRemove(@NotNull Gui owner, int slot) {

    }

    @Override
    public void onReplace(@NotNull Gui owner, @NotNull GuiItem newItem, int slot) {

    }
}
