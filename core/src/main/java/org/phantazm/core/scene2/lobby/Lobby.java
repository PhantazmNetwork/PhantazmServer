package org.phantazm.core.scene2.lobby;

import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.GameMode;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.state.CancellableState;
import net.minestom.server.event.Event;
import net.minestom.server.event.EventFilter;
import net.minestom.server.event.EventNode;
import net.minestom.server.event.inventory.InventoryPreClickEvent;
import net.minestom.server.event.item.ItemDropEvent;
import net.minestom.server.event.item.PickupExperienceEvent;
import net.minestom.server.event.item.PickupItemEvent;
import net.minestom.server.event.player.*;
import net.minestom.server.event.trait.InstanceEvent;
import net.minestom.server.instance.Instance;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.network.packet.server.play.OpenBookPacket;
import net.minestom.server.thread.Acquirable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.phantazm.commons.FutureUtils;
import org.phantazm.core.CoreStages;
import org.phantazm.core.npc.NPCHandler;
import org.phantazm.core.player.PlayerView;
import org.phantazm.core.scene2.EventScene;
import org.phantazm.core.scene2.InstanceScene;
import org.phantazm.core.scene2.TablistLocalScene;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.function.Predicate;

public class Lobby extends InstanceScene implements EventScene {
    private final Pos spawnPoint;
    private final String lobbyJoinMessageFormat;
    private final NPCHandler npcHandler;
    private final Collection<ItemStack> defaultItems;
    private final Function<? super Player, ? extends CompletableFuture<?>> displayNameStyler;

    private final EventNode<Event> lobbyNode;

    public Lobby(@NotNull Instance instance, @NotNull Pos spawnPoint, @NotNull String lobbyJoinMessageFormat,
        @NotNull NPCHandler npcHandler, @NotNull Collection<ItemStack> defaultItems,
        @NotNull Function<? super @NotNull Player, ? extends @NotNull CompletableFuture<?>> displayNameStyler,
        int timeout) {
        super(instance, timeout);

        this.spawnPoint = Objects.requireNonNull(spawnPoint);
        this.lobbyJoinMessageFormat = Objects.requireNonNull(lobbyJoinMessageFormat);
        this.npcHandler = Objects.requireNonNull(npcHandler);
        this.defaultItems = List.copyOf(defaultItems);
        this.displayNameStyler = Objects.requireNonNull(displayNameStyler);
        this.lobbyNode = buildNode(instance);

        MinecraftServer.getGlobalEventHandler().addChild(this.lobbyNode);
        npcHandler.spawnAll();
    }

    private EventNode<Event> buildNode(Instance instance) {
        UUID uuid = instance.getUniqueId();

        EventNode<Event> node = EventNode.event("lobby_node_" + uuid, EventFilter.ALL, event ->
            event instanceof InstanceEvent instanceEvent && instanceEvent.getInstance().getUniqueId().equals(uuid));

        node.addListener(PlayerSwapItemEvent.class, event -> event.setCancelled(true));
        node.addListener(PlayerEntityInteractEvent.class, npcHandler::handleInteract);
        node.addListener(ItemDropEvent.class, event -> event.setCancelled(true));
        node.addListener(InventoryPreClickEvent.class, event -> event.setCancelled(true));
        node.addListener(PlayerPreEatEvent.class, event -> event.setCancelled(true));
        node.addListener(PickupItemEvent.class, event -> event.setCancelled(true));
        node.addListener(PickupExperienceEvent.class, event -> event.setCancelled(true));
        node.addListener(PrePlayerStartDiggingEvent.class, event -> event.setCancelled(true));
        node.addListener(PlayerBlockPlaceEvent.class, event -> event.setCancelled(true));
        node.addListener(PlayerBlockInteractEvent.class, event -> {
            event.setCancelled(true);
            event.setBlockingItemUse(true);
        });
        node.addListener(PlayerBlockBreakEvent.class, event -> event.setCancelled(true));
        node.addListener(PlayerUseItemEvent.class, event -> {
            if (event.getPlayer().getItemInMainHand().material().equals(Material.WRITTEN_BOOK)) {
                event.getPlayer().sendPacket(new OpenBookPacket(Player.Hand.MAIN));
            }
        });
        node.addListener(PlayerRespawnEvent.class, event -> event.setRespawnPosition(spawnPoint));

        return node;
    }

    @Override
    public boolean preventsServerShutdown() {
        return false;
    }

    @Override
    public boolean isGame() {
        return false;
    }

    void postLogin(@NotNull Set<@NotNull PlayerView> players) {
        CompletableFuture<?>[] futures = new CompletableFuture[players.size()];

        int i = 0;
        for (PlayerView playerView : players) {
            if (!this.scenePlayers.contains(playerView)) {
                futures[i++] = FutureUtils.nullCompletedFuture();
                continue;
            }

            Optional<Player> playerOptional = playerView.getPlayer();
            if (playerOptional.isEmpty()) {
                futures[i++] = FutureUtils.nullCompletedFuture();
                continue;
            }

            futures[i++] = onSpawn(playerOptional.get());
        }

        CompletableFuture.allOf(futures).join();
    }

    void join(@NotNull Set<@NotNull PlayerView> players, boolean login) {
        CompletableFuture<?>[] futures = new CompletableFuture[players.size()];

        int i = 0;
        for (PlayerView joiningPlayer : players) {
            if (!this.scenePlayers.add(joiningPlayer)) {
                futures[i++] = FutureUtils.nullCompletedFuture();
                continue;
            }

            Optional<Player> playerOptional = joiningPlayer.getPlayer();
            if (playerOptional.isEmpty()) {
                futures[i++] = FutureUtils.nullCompletedFuture();
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

            if (login) {
                futures[i++] = FutureUtils.nullCompletedFuture();
                continue;
            }

            futures[i++] = onSpawn(player);
        }

        CompletableFuture.allOf(futures).join();
    }

    private CompletableFuture<?> onSpawn(Player player) {
        player.heal();

        CancellableState.Holder<Entity> holder = player.stateHolder();
        holder.registerState(CoreStages.LOBBY, CancellableState.state(player, self -> {
            for (ItemStack stack : defaultItems) {
                ((Player) self).getInventory().addItemStack(stack);
            }

            ((Player) self).setGameMode(GameMode.ADVENTURE);
        }, self -> {
            ((Player) self).getInventory().clear();
            ((Player) self).setGameMode(GameMode.SURVIVAL);
        }));

        holder.setStage(CoreStages.LOBBY);
        return teleportOrSetInstance(player, spawnPoint);
    }

    @Override
    public @NotNull Set<@NotNull PlayerView> leave(@NotNull Set<? extends @NotNull PlayerView> players) {
        Set<PlayerView> leftPlayers = super.leave(players);
        for (PlayerView left : leftPlayers) {
            left.getPlayer().ifPresent(player -> player.stateHolder().removeStage(CoreStages.LOBBY));
        }

        return leftPlayers;
    }

    @Override
    public void tick(long time) {
        super.tick(time);
        this.npcHandler.tick(time);
    }

    @Override
    public void shutdown() {
        EventNode<? super Event> parent = lobbyNode.getParent();
        if (parent != null) {
            parent.removeChild(lobbyNode);
        }

        this.npcHandler.end();
        super.shutdown();
    }

    @SuppressWarnings("unchecked")
    @Override
    public @NotNull Acquirable<? extends Lobby> getAcquirable() {
        return (Acquirable<? extends Lobby>) super.getAcquirable();
    }

    @Override
    public @NotNull EventNode<? super Event> sceneNode() {
        return lobbyNode;
    }
}