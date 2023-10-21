package org.phantazm.zombies.listener;

import net.minestom.server.event.trait.EntityInstanceEvent;
import net.minestom.server.instance.Instance;
import org.jetbrains.annotations.NotNull;
import org.phantazm.core.player.PlayerView;
import org.phantazm.zombies.player.ZombiesPlayer;
import org.phantazm.zombies.scene2.ZombiesScene;

import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Supplier;

public abstract class ZombiesPlayerEventListener<TEvent extends EntityInstanceEvent> implements Consumer<TEvent> {
    protected final Instance instance;
    protected final Map<PlayerView, ZombiesPlayer> zombiesPlayers;

    private final Supplier<ZombiesScene> scene;

    public ZombiesPlayerEventListener(@NotNull Instance instance,
        @NotNull Map<PlayerView, ZombiesPlayer> zombiesPlayers, @NotNull Supplier<ZombiesScene> scene) {
        this.instance = Objects.requireNonNull(instance);
        this.zombiesPlayers = Objects.requireNonNull(zombiesPlayers);
        this.scene = Objects.requireNonNull(scene);
    }

    @Override
    public void accept(TEvent event) {
        if (event.getInstance() != instance) {
            return;
        }

        ZombiesScene zombiesScene = scene.get();
        zombiesScene.getAcquirable().sync(self -> {
            ZombiesPlayer zombiesPlayer = zombiesPlayers.get(PlayerView.lookup(event.getEntity().getUuid()));
            if (zombiesPlayer != null) {
                accept(self, zombiesPlayer, event);
            }
        });
    }

    protected abstract void accept(@NotNull ZombiesScene scene, @NotNull ZombiesPlayer zombiesPlayer, @NotNull TEvent event);

}
