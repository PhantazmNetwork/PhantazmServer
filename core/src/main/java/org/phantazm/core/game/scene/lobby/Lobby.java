package org.phantazm.core.game.scene.lobby;

import it.unimi.dsi.fastutil.Pair;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.minestom.server.entity.Player;
import net.minestom.server.event.EventDispatcher;
import net.minestom.server.instance.Instance;
import net.minestom.server.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;
import org.phantazm.core.config.InstanceConfig;
import org.phantazm.core.event.PlayerJoinLobbyEvent;
import org.phantazm.core.game.scene.InstanceScene;
import org.phantazm.core.game.scene.TransferResult;
import org.phantazm.core.game.scene.fallback.SceneFallback;
import org.phantazm.core.npc.NPCHandler;
import org.phantazm.core.player.PlayerView;
import org.phantazm.core.player.PlayerViewProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.CompletableFuture;

/**
 * Represents a lobby. Most basic scene which contains {@link Player}s.
 */
public class Lobby extends InstanceScene<LobbyJoinRequest> {
    private static final Logger LOGGER = LoggerFactory.getLogger(Lobby.class);

    private final InstanceConfig instanceConfig;
    private final Map<UUID, PlayerView> players;
    private final NPCHandler npcHandler;
    private final Collection<ItemStack> defaultItems;
    private final MiniMessage miniMessage;
    private final String lobbyJoinFormat;
    private final boolean quittable;

    private boolean joinable = true;

    /**
     * Creates a lobby.
     *
     * @param instance       The {@link Instance} that the lobby's players are sent to
     * @param instanceConfig The {@link InstanceConfig} used for the lobby's {@link Instance}
     * @param fallback       A fallback for the lobby
     */
    public Lobby(@NotNull UUID uuid, @NotNull Instance instance, @NotNull InstanceConfig instanceConfig,
            @NotNull SceneFallback fallback, @NotNull NPCHandler npcHandler,
            @NotNull Collection<ItemStack> defaultItems, @NotNull MiniMessage miniMessage,
            @NotNull String lobbyJoinFormat, boolean quittable, @NotNull PlayerViewProvider playerViewProvider) {
        super(uuid, instance, fallback, instanceConfig.spawnPoint(), playerViewProvider);
        this.instanceConfig = Objects.requireNonNull(instanceConfig, "instanceConfig");
        this.players = new HashMap<>();
        this.npcHandler = Objects.requireNonNull(npcHandler, "npcHandler");
        this.npcHandler.spawnAll();
        this.defaultItems = Objects.requireNonNull(defaultItems, "defaultItems");
        this.miniMessage = Objects.requireNonNull(miniMessage, "miniMessage");
        this.lobbyJoinFormat = Objects.requireNonNull(lobbyJoinFormat, "lobbyJoinFormat");
        this.quittable = quittable;
    }

    @Override
    public @NotNull TransferResult join(@NotNull LobbyJoinRequest joinRequest) {
        if (isShutdown()) {
            return TransferResult.failure(Component.text("Lobby is shutdown."));
        }
        if (!isJoinable()) {
            return TransferResult.failure(Component.text("Lobby is not joinable."));
        }

        Collection<PlayerView> playerViews = joinRequest.getPlayers();
        Collection<Pair<PlayerView, Player>> joiners = new ArrayList<>(playerViews.size());
        for (PlayerView playerView : playerViews) {
            playerView.getPlayer().ifPresent(player -> {
                if (player.getInstance() != instance) {
                    joiners.add(Pair.of(playerView, player));
                }
            });
        }

        if (joiners.isEmpty()) {
            return TransferResult.failure(Component.text("Everybody is already in the lobby."));
        }

        return TransferResult.success(() -> {
            for (Pair<PlayerView, Player> player : joiners) {
                players.put(player.first().getUUID(), player.first());
            }

            joinRequest.handleJoin(this, instance, instanceConfig);
            for (Pair<PlayerView, Player> player : joiners) {
                player.left().getDisplayName().thenAccept(joiner -> {
                    TagResolver joinerPlaceholder = Placeholder.component("joiner", joiner);
                    Component message = miniMessage.deserialize(lobbyJoinFormat, joinerPlaceholder);
                    instance.sendMessage(message);
                });

                for (ItemStack stack : defaultItems) {
                    player.right().getInventory().addItemStack(stack);
                }

                EventDispatcher.call(new PlayerJoinLobbyEvent(player.right()));
            }
        });
    }

    @Override
    public @NotNull TransferResult leave(@NotNull Iterable<UUID> leavers) {
        boolean anyInside = false;
        for (UUID uuid : leavers) {
            if (players.containsKey(uuid)) {
                anyInside = true;
                break;
            }
        }

        if (!anyInside) {
            return TransferResult.failure(Component.text("None of the players are in the lobby."));
        }

        return TransferResult.success(() -> {
            for (UUID uuid : leavers) {
                players.remove(uuid);
            }
        });
    }

    @Override
    public @Unmodifiable @NotNull Map<UUID, PlayerView> getPlayers() {
        return Map.copyOf(players);
    }

    @Override
    public boolean isJoinable() {
        return joinable;
    }

    @Override
    public void setJoinable(boolean joinable) {
        this.joinable = joinable;
    }

    @Override
    public boolean isQuittable() {
        return quittable;
    }

    @Override
    public void shutdown() {
        this.shutdown = true;

        List<CompletableFuture<Boolean>> fallbackFutures = new ArrayList<>(players.size());
        for (PlayerView player : players.values()) {
            fallbackFutures.add(fallback.fallback(player).whenComplete((fallbackResult, throwable) -> {
                if (throwable != null) {
                    LOGGER.warn("Failed to fallback {}", player.getUUID(), throwable);
                }
            }));
        }

        CompletableFuture.allOf(fallbackFutures.toArray(CompletableFuture[]::new))
                .whenComplete((ignored, error) -> super.shutdown());
    }

    @Override
    public void tick(long time) {
        this.npcHandler.tick(time);
    }

    public @NotNull NPCHandler handler() {
        return this.npcHandler;
    }
}
