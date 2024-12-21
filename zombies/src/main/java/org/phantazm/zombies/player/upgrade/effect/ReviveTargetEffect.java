package org.phantazm.zombies.player.upgrade.effect;

import com.github.steanky.element.core.annotation.*;
import net.kyori.adventure.key.Key;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.phantazm.commons.InjectionStore;
import org.phantazm.core.Target;
import org.phantazm.core.VecUtils;
import org.phantazm.zombies.map.MapSettingsInfo;
import org.phantazm.zombies.player.ZombiesPlayer;
import org.phantazm.zombies.player.state.ZombiesPlayerState;
import org.phantazm.zombies.player.state.ZombiesPlayerStateKeys;
import org.phantazm.zombies.player.state.context.AlivePlayerStateContext;
import org.phantazm.zombies.player.state.revive.KnockedPlayerState;
import org.phantazm.zombies.player.upgrade.PlayerUpgrade;
import org.phantazm.zombies.player.upgrade.selector.Selector;
import org.phantazm.zombies.player.upgrade.selector.SelectorComponent;
import org.phantazm.zombies.player.upgrade.trigger.TriggerData;
import org.phantazm.zombies.scene2.ZombiesScene;

import java.util.Set;
import java.util.function.Function;

@Model("zombies.upgrade.effect.switch_state")
@Cache
public class ReviveTargetEffect implements UpgradeEffectComponent {
    private final SelectorComponent selector;


    @FactoryMethod
    public ReviveTargetEffect(@NotNull @Child("selector") SelectorComponent selector) {
        this.selector = selector;
    }

    @Override
    public @NotNull UpgradeEffect apply(@NotNull InjectionStore injectionStore, @NotNull ZombiesPlayer zombiesPlayer) {
        return new Internal(selector.apply(injectionStore, zombiesPlayer));
    }

    private static final class Internal implements UpgradeEffect {
        private final Selector selector;

        private static final Set<Key> VALID_KEYS = Set.of(ZombiesPlayerStateKeys.DEAD.key(),
            ZombiesPlayerStateKeys.KNOCKED.key());


        private Internal(Selector selector) {
            this.selector = selector;
        }

        @Override
        public void apply(@NotNull PlayerUpgrade upgrade, @NotNull ZombiesPlayer zombiesPlayer,
            @NotNull TriggerData triggerData) {
            ZombiesScene scene = zombiesPlayer.getScene();
            Target targets = selector.select(upgrade, zombiesPlayer, triggerData);

            scene.getAcquirable().sync(ignored -> {
                targets.forType(Player.class, player -> {
                    ZombiesPlayer scenePlayer = scene.getPlayer(player.getUuid());

                    ZombiesPlayerState currentState = scenePlayer.module().getStateSwitcher().getState();

                    if (currentState == null || !VALID_KEYS.contains(currentState.key()))
                        return;

                    Point revivePoint;
                    if (currentState instanceof KnockedPlayerState knockedPlayerState) {
                        revivePoint = knockedPlayerState.getReviveHandler().context().getKnockLocation();
                    } else {
                        MapSettingsInfo mapSettingsInfo = scene.mapSettingsInfo();
                        revivePoint =
                            new Pos(VecUtils.toPoint(mapSettingsInfo.origin().add(mapSettingsInfo.spawn())), mapSettingsInfo.yaw(),
                                mapSettingsInfo.pitch()).add(0.5, 0, 0.5);

                        player.teleport(Pos.fromPoint(revivePoint));
                    }

                    Function<?, ? extends ZombiesPlayerState> stateFunction = scenePlayer.module().getStateFunctions()
                        .get(ZombiesPlayerStateKeys.ALIVE);
                    if (stateFunction == null) return;

                    scenePlayer.setState(ZombiesPlayerStateKeys.ALIVE, AlivePlayerStateContext.revive(zombiesPlayer
                        .module().getPlayerView().getDisplayNameIfCached().orElse(null), revivePoint));
                });
            });
        }
    }

    @DataObject
    public record Data() {
    }
}
