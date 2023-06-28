package org.phantazm.zombies.map.shop.interactor;

import com.github.steanky.element.core.annotation.Cache;
import com.github.steanky.element.core.annotation.DataObject;
import com.github.steanky.element.core.annotation.FactoryMethod;
import com.github.steanky.element.core.annotation.Model;
import com.github.steanky.ethylene.core.ConfigElement;
import com.github.steanky.ethylene.core.ConfigPrimitive;
import com.github.steanky.ethylene.mapper.annotation.Default;
import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.NotNull;
import org.phantazm.zombies.map.shop.InteractorGroupHandler;
import org.phantazm.zombies.map.shop.PlayerInteraction;

import java.util.List;
import java.util.Random;

@Model("zombies.map.shop.interactor.change_selection_group")
@Cache(false)
public class ChangeSelectionGroupInteractor implements ShopInteractor {
    private final Data data;
    private final InteractorGroupHandler groupHandler;
    private final Random random;

    @FactoryMethod
    public ChangeSelectionGroupInteractor(@NotNull Data data, @NotNull InteractorGroupHandler groupHandler,
            @NotNull Random random) {
        this.data = data;
        this.groupHandler = groupHandler;
        this.random = random;
    }

    @Override
    public boolean handleInteraction(@NotNull PlayerInteraction interaction) {
        List<SelectionGroupInteractor> interactors = groupHandler.interactors(data.groupKey);
        if (interactors.isEmpty()) {
            return false;
        }

        boolean hasCurrentlyActiveInteractor = false;
        int currentActiveInteractorIndex = 0;
        for (SelectionGroupInteractor interactor : interactors) {
            if (interactor.isActive()) {
                hasCurrentlyActiveInteractor = true;
                break;
            }

            currentActiveInteractorIndex++;
        }

        if (!hasCurrentlyActiveInteractor) {
            interactors.get(random.nextInt(interactors.size())).setActive(true);
            return true;
        }

        if (data.excludeCurrent) {
            if (interactors.size() == 1) {
                return true;
            }

            int newIndex = random.nextInt(interactors.size() - 1);
            if (newIndex >= currentActiveInteractorIndex) {
                newIndex++;
            }

            interactors.get(currentActiveInteractorIndex).setActive(false);
            interactors.get(newIndex).setActive(true);
            return true;
        }

        SelectionGroupInteractor newInteractor = interactors.get(random.nextInt(interactors.size()));
        SelectionGroupInteractor oldInteractor = interactors.get(currentActiveInteractorIndex);
        if (newInteractor == oldInteractor) {
            return true;
        }

        oldInteractor.setActive(false);
        newInteractor.setActive(true);
        return false;
    }

    @DataObject
    public record Data(@NotNull Key groupKey, boolean excludeCurrent) {
        @Default("excludeCurrent")
        public static @NotNull ConfigElement defaultExcludeCurrent() {
            return ConfigPrimitive.of(true);
        }
    }
}
