package org.phantazm.zombies.mob2.validator;

import com.github.steanky.element.core.annotation.Cache;
import com.github.steanky.element.core.annotation.DataObject;
import com.github.steanky.element.core.annotation.FactoryMethod;
import com.github.steanky.element.core.annotation.Model;
import com.github.steanky.ethylene.mapper.annotation.Default;
import net.kyori.adventure.key.Key;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.GameMode;
import net.minestom.server.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.phantazm.core.player.PlayerView;
import org.phantazm.mob2.Mob;
import org.phantazm.mob2.validator.Validator;
import org.phantazm.mob2.validator.ValidatorComponent;
import org.phantazm.zombies.mob2.ZombiesMobSpawner;
import org.phantazm.zombies.player.ZombiesPlayer;
import org.phantazm.zombies.player.state.ZombiesPlayerState;
import org.phantazm.zombies.scene2.ZombiesScene;

import java.util.Objects;
import java.util.Set;

@Model("zombies.mob.validator.zombies_player")
@Cache
public class ZombiesPlayerValidator implements ValidatorComponent {
    private final Data data;

    @FactoryMethod
    public ZombiesPlayerValidator(@NotNull Data data) {
        this.data = Objects.requireNonNull(data);
    }

    @Override
    public @NotNull Validator get() {
        return new Internal(data);
    }

    private record Internal(Data data) implements Validator {

        @Override
        public boolean valid(@NotNull Mob mob, @NotNull Entity entity) {
            if (!(entity instanceof Player actualPlayer)) return false;

            GameMode gameMode = actualPlayer.getGameMode();
            if (!gameMode.canTakeDamage()) return false;

            ZombiesScene scene = mob.extensions().get(ZombiesMobSpawner.SCENE_KEY);
            ZombiesPlayer player = scene.map().objects().module().playerMap().get(PlayerView.lookup(entity.getUuid()));
            if (player == null) return false;

            ZombiesPlayerState state = player.module().getStateSwitcher().getState();
            if (state == null) return false;

            Key currentState = state.key();
            if (data.blacklist) return !data.states.contains(currentState);
           
            return data.states.contains(currentState);
        }
    }

    @Default("""
        {
          blacklist=false
        }
        """)
    @DataObject
    public record Data(@NotNull Set<Key> states,
        boolean blacklist) {
    }
}
