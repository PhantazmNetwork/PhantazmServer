package org.phantazm.zombies.map.shop;

import it.unimi.dsi.fastutil.objects.Object2BooleanMap;
import it.unimi.dsi.fastutil.objects.Object2BooleanOpenHashMap;
import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;
import org.phantazm.zombies.map.shop.interactor.SelectionGroupInteractor;

import java.util.*;

public class BasicInteractorGroupHandler implements InteractorGroupHandler {
    private final Map<Key, List<SelectionGroupInteractor>> interactorGroups;
    private final Object2BooleanMap<Key> chosenMap;

    public BasicInteractorGroupHandler() {
        this.interactorGroups = new HashMap<>();
        this.chosenMap = new Object2BooleanOpenHashMap<>();
    }

    @Override
    public void subscribe(@NotNull Key group, @NotNull SelectionGroupInteractor interactor) {
        Objects.requireNonNull(group);
        Objects.requireNonNull(interactor);

        interactorGroups.computeIfAbsent(group, ignored -> new ArrayList<>()).add(interactor);
    }

    @Override
    public @NotNull
    @Unmodifiable List<SelectionGroupInteractor> interactors(@NotNull Key group) {
        Objects.requireNonNull(group);
        return interactorGroups.getOrDefault(group, List.of());
    }

    @Override
    public void markChosen(@NotNull Key group) {
        Objects.requireNonNull(group);
        chosenMap.put(group, true);
    }

    @Override
    public boolean isChosen(@NotNull Key group) {
        Objects.requireNonNull(group);
        return chosenMap.getOrDefault(group, false);
    }
}
