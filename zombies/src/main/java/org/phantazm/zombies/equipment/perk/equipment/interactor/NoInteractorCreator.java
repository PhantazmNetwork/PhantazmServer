package org.phantazm.zombies.equipment.perk.equipment.interactor;

import com.github.steanky.element.core.annotation.Cache;
import com.github.steanky.element.core.annotation.FactoryMethod;
import com.github.steanky.element.core.annotation.Model;
import com.github.steanky.element.core.annotation.document.Description;
import org.jetbrains.annotations.NotNull;
import org.phantazm.zombies.player.ZombiesPlayer;

@Description("Interactor that does nothing.")
@Model("zombies.perk.interactor.none")
@Cache
public class NoInteractorCreator implements PerkInteractorCreator {
    private static final PerkInteractor INTERACTOR = new PerkInteractor() {
        @Override
        public void setSelected(boolean selected) {

        }

        @Override
        public void leftClick() {

        }

        @Override
        public void rightClick() {

        }
    };

    @FactoryMethod
    public NoInteractorCreator() {
    }

    @Override
    public @NotNull PerkInteractor forPlayer(@NotNull ZombiesPlayer zombiesPlayer) {
        return INTERACTOR;
    }
}
