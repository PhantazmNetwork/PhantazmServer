package org.phantazm.zombies.powerup.action.component;

import com.github.steanky.element.core.annotation.*;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.state.CancellableState;
import net.minestom.server.instance.Instance;
import org.jetbrains.annotations.NotNull;
import org.phantazm.commons.MathUtils;
import org.phantazm.core.player.PlayerView;
import org.phantazm.core.time.TickFormatter;
import org.phantazm.zombies.Stages;
import org.phantazm.zombies.player.ZombiesPlayer;
import org.phantazm.zombies.powerup.Powerup;
import org.phantazm.zombies.powerup.action.PowerupAction;
import org.phantazm.zombies.powerup.predicate.DeactivationPredicate;
import org.phantazm.zombies.scene2.ZombiesScene;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Model("zombies.powerup.action.boss_bar_timer")
@Cache(false)
public class BossBarTimerAction implements PowerupActionComponent {
    private final Data data;
    private final TickFormatter tickFormatter;

    @FactoryMethod
    public BossBarTimerAction(@NotNull Data data, @NotNull @Child("tickFormatter") TickFormatter tickFormatter) {
        this.data = data;
        this.tickFormatter = tickFormatter;
    }

    @Override
    public @NotNull PowerupAction apply(@NotNull ZombiesScene scene) {
        return new Action(data, scene.instance(), scene.managedPlayers(), tickFormatter);
    }

    private static class Action implements PowerupAction {
        private final Data data;
        private final Instance instance;
        private final DeactivationPredicate predicate;
        private final Map<PlayerView, ZombiesPlayer> playerMap;
        private final TickFormatter tickFormatter;

        private final List<CancellableState<Entity>> states;

        private long startTicks = -1;
        private BossBar bossBar;

        private Action(Data data, Instance instance, Map<PlayerView, ZombiesPlayer> playerMap,
            TickFormatter tickFormatter) {
            this.data = data;
            this.instance = instance;
            this.predicate = new DeactivationPredicate() {
                @Override
                public void activate(long time) {

                }

                @Override
                public boolean shouldDeactivate(long time) {
                    return startTicks >= data.duration;
                }
            };
            this.playerMap = playerMap;
            this.tickFormatter = tickFormatter;
            this.states = new ArrayList<>();
        }

        @Override
        public void tick(long time) {
            BossBar bossBar = this.bossBar;
            if (startTicks < 0 || bossBar == null) {
                return;
            }

            ++startTicks;
            bossBar.name(createBossBarName());
            bossBar.progress((float) MathUtils.clamp(1D - ((float) startTicks / (float) data.duration), 0, 1));
        }

        @Override
        public void activate(@NotNull Powerup powerup, @NotNull ZombiesPlayer player, long time) {
            this.startTicks = 0;

            BossBar bossBar = BossBar.bossBar(createBossBarName(), 1.0F, data.color, data.overlay);
            instance.showBossBar(bossBar);

            this.bossBar = bossBar;

            for (ZombiesPlayer zombiesPlayer : playerMap.values()) {
                zombiesPlayer.getPlayer().ifPresent(actualPlayer -> {
                    CancellableState<Entity> state = CancellableState.builder((Entity) actualPlayer).end(entity -> {
                        ((Player) entity).hideBossBar(bossBar);
                    }).build();

                    states.add(state);
                    actualPlayer.stateHolder().registerState(Stages.ZOMBIES_GAME, state);
                });
            }
        }

        private Component createBossBarName() {
            long remainingTicks = data.duration - startTicks;

            TagResolver timePlaceholder = Placeholder.unparsed("time", tickFormatter.format(remainingTicks));
            return MiniMessage.miniMessage().deserialize(data.format, timePlaceholder);
        }

        @Override
        public void deactivate(@NotNull ZombiesPlayer player) {
            BossBar bossBar = this.bossBar;
            if (bossBar == null) {
                return;
            }

            for (CancellableState<Entity> state : states) {
                state.self().stateHolder().removeState(Stages.ZOMBIES_GAME, state);
            }

            MinecraftServer.getBossBarManager().destroyBossBar(bossBar);
            this.bossBar = null;
            states.clear();
        }

        @Override
        public @NotNull DeactivationPredicate deactivationPredicate() {
            return predicate;
        }
    }

    @DataObject
    public record Data(
        long duration,
        @NotNull String format,
        @NotNull BossBar.Color color,
        @NotNull BossBar.Overlay overlay) {
    }
}
