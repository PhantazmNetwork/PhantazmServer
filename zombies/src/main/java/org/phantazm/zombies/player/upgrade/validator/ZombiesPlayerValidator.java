package org.phantazm.zombies.player.upgrade.validator;

import com.github.steanky.element.core.annotation.Cache;
import com.github.steanky.element.core.annotation.DataObject;
import com.github.steanky.element.core.annotation.FactoryMethod;
import com.github.steanky.element.core.annotation.Model;
import net.kyori.adventure.key.Key;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.phantazm.commons.InjectionStore;
import org.phantazm.core.player.PlayerView;
import org.phantazm.zombies.player.ZombiesPlayer;
import org.phantazm.zombies.player.state.ZombiesPlayerState;

import java.util.Objects;
import java.util.Set;

@Model("zombies.upgrade.validator.zombies_player")
@Cache
public class ZombiesPlayerValidator implements ValidatorComponent {
    private final Data data;

    @FactoryMethod
    public ZombiesPlayerValidator(@NotNull Data data) {
        this.data = Objects.requireNonNull(data);
    }

    @Override
    public @NotNull Validator apply(@NotNull InjectionStore injectionStore, @NotNull ZombiesPlayer zombiesPlayer) {
        return new Internal(data, zombiesPlayer);
    }

    private record Internal(Data data,
        ZombiesPlayer zombiesPlayer) implements Validator {
        @Override
        public boolean test(Entity entity) {
            if (!(entity instanceof Player)) {
                return false;
            }

            ZombiesPlayer player = zombiesPlayer.getScene().map().objects().module().playerMap()
                .get(PlayerView.lookup(entity.getUuid()));
            if (player == null) {
                return false;
            }

            ZombiesPlayerState state = player.module().getStateSwitcher().getState();
            if (state == null) {
                return false;
            }

            Key currentState = state.key();
            if (data.blacklist) {
                return !data.states.contains(currentState);
            }

            return data.states.contains(currentState);
        }
    }

    @DataObject
    public record Data(@NotNull Set<Key> states,
        boolean blacklist) {
    }
}
