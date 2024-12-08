package org.phantazm.zombies.player.upgrade.effect;

import com.github.steanky.element.core.annotation.*;
import org.jetbrains.annotations.NotNull;
import org.phantazm.commons.InjectionStore;
import org.phantazm.zombies.player.ZombiesPlayer;
import org.phantazm.zombies.player.upgrade.PlayerUpgrade;
import org.phantazm.zombies.player.upgrade.trigger.TriggerData;
import org.phantazm.zombies.scene2.ZombiesScene;
import org.phantazm.zombies.stage.Stage;
import org.phantazm.zombies.stage.StageKeys;

@Model("zombies.upgrade.effect.prevent_game_end")
@Cache
public class PreventGameEndEffect implements UpgradeEffectComponent {
    @FactoryMethod
    public PreventGameEndEffect() {
    }

    @Override
    public @NotNull UpgradeEffect apply(@NotNull InjectionStore injectionStore, @NotNull ZombiesPlayer zombiesPlayer) {
        return new Internal();
    }

    private static final class Internal implements UpgradeEffect {

        private Internal() {
        }

        private boolean isInvalid(ZombiesScene scene, ZombiesPlayer zombiesPlayer) {
            // sanity check: technically possible under edge cases
            if (!scene.hasPlayer(zombiesPlayer.getUUID()) || !zombiesPlayer.isInGame()) return true;

            Stage currentStage = scene.currentStage();

            // can't activate if we're not IN_GAME
            return currentStage == null || !currentStage.key().equals(StageKeys.IN_GAME);
        }

        @Override
        public void apply(@NotNull PlayerUpgrade upgrade, @NotNull ZombiesPlayer zombiesPlayer,
            @NotNull TriggerData triggerData) {
            ZombiesScene scene = zombiesPlayer.getScene();
            scene.getAcquirable().sync(ignored -> {
                if (isInvalid(scene, zombiesPlayer)) return;

                // set the keep alive meta
                // this will be cleared if the player quits
                zombiesPlayer.module().getMeta().setKeepGameAlive(true);
            });
        }

        @Override
        public void clear(@NotNull PlayerUpgrade upgrade, @NotNull ZombiesPlayer zombiesPlayer) {
            ZombiesScene scene = zombiesPlayer.getScene();
            scene.getAcquirable().sync(ignored -> {
                if (isInvalid(scene, zombiesPlayer)) return;

                // clear the keep alive meta
                zombiesPlayer.module().getMeta().setKeepGameAlive(false);
            });
        }
    }
}
