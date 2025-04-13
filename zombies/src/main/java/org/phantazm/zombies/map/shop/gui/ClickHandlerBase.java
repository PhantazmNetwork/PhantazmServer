package org.phantazm.zombies.map.shop.gui;

import org.jetbrains.annotations.NotNull;
import org.phantazm.core.gui.Gui;
import org.phantazm.core.gui.GuiItem;
import org.phantazm.core.player.PlayerView;
import org.phantazm.zombies.map.shop.PlayerInteraction;
import org.phantazm.zombies.map.shop.Shop;
import org.phantazm.zombies.map.shop.interactor.ShopInteractor;
import org.phantazm.zombies.player.ZombiesPlayer;

import java.util.Map;
import java.util.Objects;

public abstract class ClickHandlerBase<TData> implements GuiItem, ShopInteractor {
    public enum SwitchDirection {
        NONE,
        NEXT,
        PREV
    }

    protected final TData data;
    protected final Map<PlayerView, ZombiesPlayer> playerMap;

    protected Shop shop;

    public ClickHandlerBase(@NotNull TData data, @NotNull Map<PlayerView, ZombiesPlayer> playerMap) {
        this.data = Objects.requireNonNull(data);
        this.playerMap = Objects.requireNonNull(playerMap);
    }

    public final TData data() {
        return data;
    }

    public final @NotNull Map<PlayerView, ZombiesPlayer> playerMap() {
        return playerMap;
    }

    @Override
    public void initialize(@NotNull Shop shop) {
        this.shop = shop;
    }

    @Override
    public boolean handleInteraction(@NotNull PlayerInteraction interaction) {
        return false;
    }

    @Override
    public void onRemove(@NotNull Gui owner, int slot) {

    }

    @Override
    public void onReplace(@NotNull Gui owner, @NotNull GuiItem newItem, int slot) {

    }

    public abstract int fixedSlot();

    public abstract int page();

    public @NotNull SwitchDirection pageDirection() {
        return SwitchDirection.NONE;
    }
}
