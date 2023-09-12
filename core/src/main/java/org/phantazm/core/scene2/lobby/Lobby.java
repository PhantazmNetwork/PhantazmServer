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
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.UnmodifiableView;
import org.phantazm.core.CoreStages;
import org.phantazm.core.npc.NPCHandler;
import org.phantazm.core.player.PlayerView;
import org.phantazm.core.scene2.IdentifiableScene;
import org.phantazm.core.scene2.InstanceScene;
import org.phantazm.core.scene2.JoinToggleable;
import org.phantazm.core.scene2.TablistLocalScene;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

public class Lobby extends InstanceScene implements IdentifiableScene, JoinToggleable, TablistLocalScene {
    private final Set<PlayerView> players;
    private final Set<PlayerView> playersView;
    private final UUID identity;

    private final Pos spawnPoint;
    private final String lobbyJoinMessageFormat;
    private final NPCHandler npcHandler;
    private final Collection<ItemStack> defaultItems;
    private final Function<? super Player, ? extends CompletableFuture<?>> displayNameStyler;

    private final EventNode<InstanceEvent> lobbyNode;

    private boolean joinable;

    public Lobby(@NotNull Instance instance, @NotNull Pos spawnPoint, @NotNull String lobbyJoinMessageFormat,
        @NotNull NPCHandler npcHandler, @NotNull Collection<ItemStack> defaultItems,
        @NotNull Function<? super @NotNull Player, ? extends @NotNull CompletableFuture<?>> displayNameStyler,
        int timeout) {
        super(instance, timeout);
        this.players = new HashSet<>();
        this.playersView = Collections.unmodifiableSet(this.players);
        this.identity = UUID.randomUUID();

        this.spawnPoint = Objects.requireNonNull(spawnPoint);
        this.lobbyJoinMessageFormat = Objects.requireNonNull(lobbyJoinMessageFormat);
        this.npcHandler = Objects.requireNonNull(npcHandler);
        this.defaultItems = List.copyOf(defaultItems);
        this.displayNameStyler = Objects.requireNonNull(displayNameStyler);
        this.lobbyNode = buildNode(instance);

        this.joinable = true;

        MinecraftServer.getGlobalEventHandler().addChild(this.lobbyNode);
        npcHandler.spawnAll();
    }

    private EventNode<InstanceEvent> buildNode(Instance instance) {
        UUID uuid = instance.getUniqueId();

        EventNode<InstanceEvent> node = EventNode.type("lobby_node_" + uuid, EventFilter.INSTANCE,
            (e, v) -> v.getUniqueId().equals(uuid));

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

        return node;
    }

    @Override
    public @NotNull @UnmodifiableView Set<@NotNull PlayerView> playersView() {
        return playersView;
    }

    @Override
    public int playerCount() {
        return players.size();
    }

    @Override
    public boolean preventsServerShutdown() {
        return false;
    }

    void postLogin(@NotNull Set<@NotNull PlayerView> players) {
        for (PlayerView playerView : players) {
            if (!this.players.contains(playerView)) {
                continue;
            }

            playerView.getPlayer().ifPresent(this::onSpawn);
        }
    }

    void join(@NotNull Set<@NotNull PlayerView> players, boolean login) {
        for (PlayerView joiningPlayer : players) {
            if (!this.players.add(joiningPlayer)) {
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

            if (login) {
                return;
            }

            onSpawn(player);
        }
    }

    private void onSpawn(Player player) {
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
        player.teleport(spawnPoint);
    }

    @Override
    public @NotNull Set<@NotNull PlayerView> leave(@NotNull Set<? extends @NotNull PlayerView> players) {
        Set<PlayerView> leftPlayers = new HashSet<>(players.size());
        for (PlayerView leavingPlayer : players) {
            if (!this.players.remove(leavingPlayer)) {
                continue;
            }

            leavingPlayer.getPlayer().ifPresent(player -> player.stateHolder().removeStage(CoreStages.LOBBY));
            leftPlayers.add(leavingPlayer);
        }

        return leftPlayers;
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
        super.tick(time);
        this.npcHandler.tick(time);
    }

    @Override
    public void shutdown() {
        MinecraftServer.getGlobalEventHandler().removeChild(lobbyNode);

        super.shutdown();
        this.npcHandler.end();
    }
}