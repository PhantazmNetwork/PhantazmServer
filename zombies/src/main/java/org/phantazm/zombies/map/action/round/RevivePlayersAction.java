package org.phantazm.zombies.map.action.round;

import com.github.steanky.element.core.annotation.Cache;
import com.github.steanky.element.core.annotation.FactoryMethod;
import com.github.steanky.element.core.annotation.Model;
import org.jetbrains.annotations.NotNull;
import org.phantazm.commons.InjectionStore;
import org.phantazm.commons.LazyComponent;
import org.phantazm.zombies.map.Round;
import org.phantazm.zombies.map.action.Action;
import org.phantazm.zombies.player.ZombiesPlayer;
import org.phantazm.zombies.player.state.ZombiesPlayerStateKeys;
import org.phantazm.zombies.player.state.context.AlivePlayerStateContext;
import org.phantazm.zombies.scene2.ZombiesScene;

import java.util.function.Supplier;

@Model("zombies.map.round.action.revive_players")
@Cache
public class RevivePlayersAction implements LazyComponent<ZombiesScene, Action<Round>> {
    @FactoryMethod
    public RevivePlayersAction() {

    }

    @Override
    public @NotNull Action<Round> apply(@NotNull InjectionStore injectionStore,
        @NotNull Supplier<@NotNull ZombiesScene> sceneSupplier) {
        return new Impl(sceneSupplier);
    }

    private record Impl(Supplier<ZombiesScene> zombiesScene) implements Action<Round> {
        @Override
        public void perform(@NotNull Round round) {
            ZombiesScene zombiesScene = this.zombiesScene.get();
            for (ZombiesPlayer zombiesPlayer : zombiesScene.managedPlayers().values()) {
                boolean dead = zombiesPlayer.isDead();
                if (dead || zombiesPlayer.isKnocked()) {
                    if (dead) {
                        zombiesPlayer.getPlayer().ifPresent(player -> player
                            .teleport(zombiesScene.map().objects().module().respawnPos()));
                    }

                    zombiesPlayer.setState(ZombiesPlayerStateKeys.ALIVE, AlivePlayerStateContext.regular());
                }
            }
        }
    }
}
