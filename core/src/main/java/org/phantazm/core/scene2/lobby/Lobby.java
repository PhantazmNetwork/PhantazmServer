package org.phantazm.core.scene2.lobby;

import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.state.CancellableState;
import net.minestom.server.instance.Instance;
import net.minestom.server.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.UnmodifiableView;
import org.phantazm.core.CoreStages;
import org.phantazm.core.npc.NPCHandler;
import org.phantazm.core.player.PlayerView;
import org.phantazm.core.player.PlayerViewProvider;
import org.phantazm.core.scene2.IdentifiableScene;
import org.phantazm.core.scene2.InstanceScene;
import org.phantazm.core.scene2.JoinToggleable;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

public class Lobby extends InstanceScene implements IdentifiableScene, JoinToggleable {
    private final Set<PlayerView> players;
    private final Set<PlayerView> playersView;
    private final UUID identity;

    private final Pos spawnPoint;
    private final String lobbyJoinMessageFormat;
    private final NPCHandler npcHandler;
    private final Collection<ItemStack> defaultItems;
    private final Function<? super Player, ? extends CompletableFuture<?>> displayNameStyler;

    private boolean joinable;

    public Lobby(@NotNull Instance instance,
        @NotNull Pos spawnPoint,
        @NotNull String lobbyJoinMessageFormat,
        @NotNull NPCHandler npcHandler,
        @NotNull Collection<ItemStack> defaultItems,
        @NotNull PlayerViewProvider viewProvider,
        @NotNull Function<? super Player, ? extends CompletableFuture<?>> displayNameStyler) {
        super(instance);
        this.players = new HashSet<>();
        this.playersView = Collections.unmodifiableSet(players);
        this.identity = UUID.randomUUID();

        this.spawnPoint = Objects.requireNonNull(spawnPoint);
        this.lobbyJoinMessageFormat = Objects.requireNonNull(lobbyJoinMessageFormat);
        this.npcHandler = Objects.requireNonNull(npcHandler);
        this.defaultItems = List.copyOf(defaultItems);
        this.displayNameStyler = Objects.requireNonNull(displayNameStyler);

        this.joinable = true;
    }

    @Override
    public @NotNull @UnmodifiableView Set<PlayerView> players() {
        return playersView;
    }

    @Override
    public boolean preventsServerShutdown() {
        return false;
    }

    public void join(@NotNull Set<@NotNull PlayerView> players) {
        for (PlayerView joiningPlayer : players) {
            if (!players.add(joiningPlayer)) {
                continue;
            }

            Optional<Player> playerOptional = joiningPlayer.getPlayer();
            if (playerOptional.isEmpty()) {
                continue;
            }

            Player player = playerOptional.get();
            displayNameStyler.apply(player).whenComplete((result, error) -> {
                if (error != null) {
                    return;
                }

                TagResolver joinerTag = Placeholder.component("joiner", player.getName());
                instance().sendMessage(MiniMessage.miniMessage().deserialize(lobbyJoinMessageFormat, joinerTag));
            });

            CancellableState.Holder<Entity> holder = player.stateHolder();
            holder.registerState(CoreStages.LOBBY, CancellableState.state(player, self -> {
                for (ItemStack stack : defaultItems) {
                    ((Player) self).getInventory().addItemStack(stack);
                }
            }, self -> {
                ((Player) self).getInventory().clear();
            }));

            holder.setStage(CoreStages.LOBBY);

            player.teleport(spawnPoint);
        }
    }

    @Override
    public void leave(@NotNull Set<? extends @NotNull PlayerView> players) {
        for (PlayerView leavingPlayer : players) {
            if (!this.players.remove(leavingPlayer)) {
                continue;
            }

            leavingPlayer.getPlayer().ifPresent(player -> {
                player.stateHolder().removeStage(CoreStages.LOBBY);
            });
        }
    }

    @Override
    public @NotNull UUID identity() {
        return identity;
    }

    @Override
    public boolean joinable() {
        return super.joinable() && joinable;
    }

    @Override
    public void setJoinable(boolean joinable) {
        this.joinable = joinable;
    }

    @Override
    public void tick(long time) {
        this.npcHandler.tick(time);
    }
}
