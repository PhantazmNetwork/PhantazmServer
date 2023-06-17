package org.phantazm.zombies.map.shop.predicate;

import com.github.steanky.element.core.annotation.DataObject;
import com.github.steanky.element.core.annotation.FactoryMethod;
import com.github.steanky.element.core.annotation.Model;
import org.jetbrains.annotations.NotNull;
import org.phantazm.zombies.map.shop.PlayerInteraction;

@Model("zombies.map.shop.predicate.in_game")
public class PlayerInGamePredicate extends PredicateBase<PlayerInGamePredicate.Data> {
    @FactoryMethod
    public PlayerInGamePredicate(@NotNull Data data) {
        super(data);
    }

    @Override
    public boolean canInteract(@NotNull PlayerInteraction interaction) {
        return interaction.player().module().getMeta().isInGame();
    }

    @DataObject
    public record Data() {
    }
}
