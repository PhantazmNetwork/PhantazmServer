package org.phantazm.zombies.powerup.action;

import com.github.steanky.element.core.annotation.*;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.text.Component;
import net.minestom.server.MinecraftServer;
import net.minestom.server.instance.Instance;
import org.jetbrains.annotations.NotNull;
import org.phantazm.commons.CancellableState;
import org.phantazm.commons.MathUtils;
import org.phantazm.zombies.player.ZombiesPlayer;
import org.phantazm.zombies.powerup.Powerup;
import org.phantazm.zombies.powerup.predicate.DeactivationPredicate;

import java.util.Map;
import java.util.UUID;
import java.util.function.Supplier;

@Model("zombies.powerup.action.boss_bar_timer")
@Cache(false)
public class BossBarTimerAction implements Supplier<PowerupAction> {
    private final Data data;
    private final Instance instance;
    private final Map<? super UUID, ? extends ZombiesPlayer> playerMap;

    @FactoryMethod
    public BossBarTimerAction(@NotNull Data data, @NotNull Instance instance,
            @NotNull Map<? super UUID, ? extends ZombiesPlayer> playerMap) {
        this.data = data;
        this.instance = instance;
        this.playerMap = playerMap;
    }

    @Override
    public PowerupAction get() {
        return new Action(data, instance, playerMap);
    }

    private static class Action implements PowerupAction {
        private final Data data;
        private final Instance instance;
        private final DeactivationPredicate predicate;
        private final Map<? super UUID, ? extends ZombiesPlayer> playerMap;
        private final UUID id;

        private long startTime = -1;
        private BossBar bossBar;

        private Action(Data data, Instance instance, Map<? super UUID, ? extends ZombiesPlayer> playerMap) {
            this.data = data;
            this.instance = instance;
            this.predicate = new DeactivationPredicate() {
                @Override
                public void activate(long time) {

                }

                @Override
                public boolean shouldDeactivate(long time) {
                    return (time - startTime) / MinecraftServer.TICK_MS >= data.duration;
                }
            };
            this.playerMap = playerMap;
            this.id = UUID.randomUUID();
        }

        @Override
        public void tick(long time) {
            BossBar bossBar = this.bossBar;
            if (startTime < 0 || bossBar == null) {
                return;
            }

            long elapsedTime = (time - startTime) / MinecraftServer.TICK_MS;
            bossBar.progress((float)MathUtils.clamp(1D - ((float)elapsedTime / (float)data.duration), 0, 1));
        }

        @Override
        public void activate(@NotNull Powerup powerup, @NotNull ZombiesPlayer player, long time) {
            this.startTime = System.currentTimeMillis();

            BossBar bossBar = BossBar.bossBar(data.name, 1.0F, data.color, data.overlay);
            instance.showBossBar(bossBar);

            this.bossBar = bossBar;

            for (ZombiesPlayer zombiesPlayer : playerMap.values()) {
                zombiesPlayer.registerCancellable(CancellableState.named(id, () -> {
                }, () -> zombiesPlayer.getPlayer().ifPresent(actualPlayer -> actualPlayer.hideBossBar(bossBar))));
            }
        }

        @Override
        public void deactivate(@NotNull ZombiesPlayer player) {
            BossBar bossBar = this.bossBar;
            if (bossBar != null) {
                for (ZombiesPlayer zombiesPlayer : playerMap.values()) {
                    zombiesPlayer.removeCancellable(id);
                }

                this.bossBar = null;
            }
        }

        @Override
        public @NotNull DeactivationPredicate deactivationPredicate() {
            return predicate;
        }
    }

    @DataObject
    public record Data(long duration,
                       @NotNull Component name,
                       @NotNull BossBar.Color color,
                       @NotNull BossBar.Overlay overlay) {
    }
}
