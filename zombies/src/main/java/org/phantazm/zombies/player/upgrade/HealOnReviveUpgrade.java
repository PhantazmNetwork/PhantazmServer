package org.phantazm.zombies.player.upgrade;

import com.github.steanky.element.core.annotation.Cache;
import com.github.steanky.element.core.annotation.DataObject;
import com.github.steanky.element.core.annotation.FactoryMethod;
import com.github.steanky.element.core.annotation.Model;
import com.github.steanky.ethylene.mapper.annotation.Default;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.Player;
import net.minestom.server.event.EventListener;
import net.minestom.server.instance.EntityTracker;
import net.minestom.server.instance.Instance;
import org.jetbrains.annotations.NotNull;
import org.phantazm.commons.InjectionStore;
import org.phantazm.core.player.PlayerView;
import org.phantazm.core.tick.Activable;
import org.phantazm.zombies.event.player.ZombiesPlayerReviveEvent;
import org.phantazm.zombies.player.ZombiesPlayer;
import org.phantazm.zombies.player.state.ZombiesPlayerStateKeys;

import java.util.HashSet;
import java.util.Optional;
import java.util.UUID;

@Model("zombies.upgrade.heal_on_revive")
@Cache
public class HealOnReviveUpgrade implements PlayerUpgradeComponent {
    private final Data data;

    @FactoryMethod
    public HealOnReviveUpgrade(@NotNull Data data) {
        this.data = data;
    }

    @Default("""
        {
          maxPlayersInVicinity=-1,
          radius=0.0,
          cooldown=0
        }
        """)

    @Override
    public @NotNull PlayerUpgrade apply(@NotNull InjectionStore injectionStore, @NotNull ZombiesPlayer zombiesPlayer) {
        return new Internal(zombiesPlayer, data);
    }

    private static class Internal extends GuardedPlayerUpgrade {
        private Internal(ZombiesPlayer zombiesPlayer, Data data) {
            super(Activable.threadsafeWrapper(new Activable() {
                private final EventListener<ZombiesPlayerReviveEvent> event = EventListener.builder(ZombiesPlayerReviveEvent.class)
                    .handler(this::handleRevive).build();
                private final HashSet<PastPlayer> pastPlayers = new HashSet<PastPlayer>();

                @Override
                public void start() {
                    zombiesPlayer.getScene().sceneNode().addListener(event);
                }

                @Override
                public void tick(long time) {
                    pastPlayers.removeIf(player -> player.duration-- == 0);
                }

                @Override
                public void end() {
                    zombiesPlayer.getScene().sceneNode().removeListener(event);
                }

                private void handleRevive(ZombiesPlayerReviveEvent event) {
                    if(event.zombiesPlayer() != zombiesPlayer) {
                        return;
                    }

                    Entity reviver = event.getPlayer();
                    Instance instance = reviver.getInstance();
                    if(instance == null ) {
                        return;
                    }

                    if(data.maxPlayersInVicinity >= 0) {
                        int[] nearbyPlayers = new int[1];
                        event.zombiesPlayer().getScene().getAcquirable().sync(scene -> {
                            instance.getEntityTracker().nearbyEntities(reviver.getPosition(), data.radius,
                                EntityTracker.Target.PLAYERS, candidatePlayer -> {
                                    if(candidatePlayer == reviver) {
                                        return;
                                    }

                                    ZombiesPlayer otherZombiesPlayer = event.zombiesPlayer().getScene().managedPlayers()
                                        .get(PlayerView.lookup(candidatePlayer.getUuid()));
                                    if (otherZombiesPlayer == null) {
                                        return;
                                    }

                                    if (!otherZombiesPlayer.isState(ZombiesPlayerStateKeys.ALIVE)) {
                                        return;
                                    }

                                    nearbyPlayers[0]++;
                                });
                        });

                        if(nearbyPlayers[0] > data.maxPlayersInVicinity) {
                            return;
                        }
                    }

                    if(data.restrictSamePlayerRevival && data.cooldown != 0) {
                        Optional<Player> revivee = event.reviveTarget().getPlayer();
                        if(revivee.isEmpty()) {
                            return;
                        }

                        UUID reviveeUuid = revivee.get().getUuid();
                        for(PastPlayer player : pastPlayers) {
                            if(reviveeUuid.equals(player.uuid)) {
                                return;
                            }
                        }
                        pastPlayers.add(new PastPlayer(reviveeUuid));
                    }

                    event.getPlayer().getAcquirable().sync(revivingPlayer -> {
                        Player player = (Player) revivingPlayer;
                        player.setHealth((float) (player.getHealth() + data.healAmount));
                    });
                }

                private class PastPlayer {
                    public final UUID uuid;
                    public int duration;

                    public PastPlayer(UUID uuid) {
                        this.uuid = uuid;
                        this.duration = data.cooldown;
                    }
                }
            }), zombiesPlayer);
        }

        @Override
        public boolean needsTicking() {
            return true;
        }

    }
    @DataObject
    public record Data(
       double healAmount,
       int maxPlayersInVicinity,
       double radius,
       boolean restrictSamePlayerRevival,
       int cooldown
    ) {}
}
