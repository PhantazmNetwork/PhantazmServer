package org.phantazm.zombies.equipment.perk.equipment.interactor;

import com.github.steanky.element.core.annotation.Cache;
import com.github.steanky.element.core.annotation.FactoryMethod;
import com.github.steanky.element.core.annotation.Model;
import com.github.steanky.element.core.annotation.document.Description;
import net.minestom.server.entity.Entity;
import org.jetbrains.annotations.NotNull;
import org.phantazm.zombies.player.ZombiesPlayer;

@Description("""
        Interactor that does nothing. Since all perks require an interactor, this is useful as a "placeholder" of sorts.
        """)
@Model("zombies.perk.interactor.none")
@Cache
public class NoInteractorCreator implements PerkInteractorCreator {
    private static final PerkInteractor INTERACTOR = new PerkInteractor() {
        @Override
        public boolean setSelected(boolean selected) {
            return true;
        }

        @Override
        public boolean leftClick() {
            return true;
        }

        @Override
        public boolean rightClick() {
            return true;
        }

        @Override
        public boolean attack(@NotNull Entity target) {
            return true;
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
