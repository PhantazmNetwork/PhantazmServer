package org.phantazm.zombies.map.shop.predicate;

import com.github.steanky.element.core.annotation.DataObject;
import com.github.steanky.element.core.annotation.FactoryMethod;
import com.github.steanky.element.core.annotation.Model;
import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.NotNull;
import org.phantazm.zombies.map.shop.PlayerInteraction;
import org.phantazm.zombies.map.shop.Shop;

import java.util.Set;

@Model("zombies.map.shop.predicate.player_state")
public class PlayerStatePredicate extends PredicateBase<PlayerStatePredicate.Data> {
    @FactoryMethod
    public PlayerStatePredicate(@NotNull Data data) {
        super(data);
    }

    @Override
    public boolean canInteract(@NotNull PlayerInteraction interaction, @NotNull Shop shop) {
        return data.blacklist !=
                data.states.contains(interaction.player().module().getStateSwitcher().getState().key());
    }

    @DataObject
    public record Data(@NotNull Set<Key> states, boolean blacklist) {
    }
}
