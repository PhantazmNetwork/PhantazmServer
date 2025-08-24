package org.phantazm.zombies.player.upgrade.effect;

import com.github.steanky.element.core.annotation.*;
import com.github.steanky.ethylene.mapper.annotation.Default;
import net.kyori.adventure.key.Key;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.phantazm.commons.InjectionStore;
import org.phantazm.core.Target;
import org.phantazm.core.VecUtils;
import org.phantazm.zombies.event.player.ZombiesPlayerReviveEvent;
import org.phantazm.zombies.event.player.ZombiesPlayerStartReviveEvent;
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

import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

@Model("zombies.upgrade.effect.switch_state")
@Cache
public class ReviveTargetEffect implements UpgradeEffectComponent {
    private final Data data;
    private final SelectorComponent selector;


    @FactoryMethod
    public ReviveTargetEffect(@NotNull Data data, @NotNull @Child("selector") SelectorComponent selector) {
        this.data = data;
        this.selector = selector;
    }

    @Override
    public @NotNull UpgradeEffect apply(@NotNull InjectionStore injectionStore, @NotNull ZombiesPlayer zombiesPlayer) {
        return new Internal(selector.apply(injectionStore, zombiesPlayer), data);
    }

    private static final class Internal implements UpgradeEffect {
        private final Selector selector;
        private final Data data;

        private static final Set<Key> VALID_KEYS = Set.of(ZombiesPlayerStateKeys.DEAD.key(),
            ZombiesPlayerStateKeys.KNOCKED.key());


        private Internal(Selector selector, Data data) {
            this.selector = selector;
            this.data = data;
        }

        @Override
        public void apply(@NotNull PlayerUpgrade upgrade, @NotNull ZombiesPlayer selfZombiesPlayer,
            @NotNull TriggerData triggerData) {
            ZombiesScene scene = selfZombiesPlayer.getScene();
            Target targets = selector.select(upgrade, selfZombiesPlayer, triggerData);

            Optional<Player> selfOptional = selfZombiesPlayer.getPlayer();

            scene.getAcquirable().sync(ignored -> {
                targets.forType(Player.class, targetPlayer -> {
                    ZombiesPlayer targetZombiesPlayer = scene.getPlayer(targetPlayer.getUuid());

                    ZombiesPlayerState currentState = targetZombiesPlayer.module().getStateSwitcher().getState();

                    if (currentState == null || !VALID_KEYS.contains(currentState.key()))
                        return;

                    Point revivePoint;
                    if (currentState instanceof KnockedPlayerState knockedPlayerState) {
                        revivePoint = knockedPlayerState.getReviveHandler().context().getKnockLocation();
                    } else {
                        MapSettingsInfo mapSettingsInfo = scene.mapSettingsInfo();
                        Optional<Player> playerOptional = selfZombiesPlayer.getPlayer();

                        if (playerOptional.isPresent() && data.respawnAtPlayer) {
                            revivePoint = playerOptional.get().getPosition();
                        } else {
                            revivePoint =
                                new Pos(VecUtils.toPoint(mapSettingsInfo.origin().add(mapSettingsInfo.spawn())), mapSettingsInfo.yaw(),
                                    mapSettingsInfo.pitch()).add(0.5, 0, 0.5);
                        }

                        targetPlayer.teleport(Pos.fromPoint(revivePoint));
                    }

                    Function<?, ? extends ZombiesPlayerState> stateFunction = targetZombiesPlayer.module().getStateFunctions()
                        .get(ZombiesPlayerStateKeys.ALIVE);
                    if (stateFunction == null) return;

                    if (selfOptional.isPresent()) {
                        Player selfPlayer = selfOptional.get();

                        ZombiesPlayerStartReviveEvent startReviveEvent = new ZombiesPlayerStartReviveEvent(selfPlayer,
                            selfZombiesPlayer, targetPlayer, targetZombiesPlayer);

                        ZombiesPlayerReviveEvent event = new ZombiesPlayerReviveEvent(selfPlayer, selfZombiesPlayer,
                            targetPlayer, targetZombiesPlayer);

                        scene.broadcastEvent(startReviveEvent);
                        scene.broadcastEvent(event);
                    }

                    targetZombiesPlayer.setState(ZombiesPlayerStateKeys.ALIVE, AlivePlayerStateContext.revive(selfZombiesPlayer
                        .module().getPlayerView().getDisplayNameIfCached().orElse(null), revivePoint));
                });
            });
        }
    }

    @DataObject
    @Default("""
        {
          respawnAtPlayer=false
        }
        """)
    public record Data(boolean respawnAtPlayer) {
    }
}
