package org.phantazm.zombies.map.shop;

import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;
import org.phantazm.zombies.map.shop.interactor.SelectionGroupInteractor;

import java.util.List;

public interface InteractorGroupHandler {
    void subscribe(@NotNull Key group, @NotNull SelectionGroupInteractor interactor);

    @NotNull @Unmodifiable List<SelectionGroupInteractor> interactors(@NotNull Key group);

    void markChosen(@NotNull Key group);

    boolean isChosen(@NotNull Key group);
}
