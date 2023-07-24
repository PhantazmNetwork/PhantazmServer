package org.phantazm.core.game.scene.lobby;

import com.github.steanky.element.core.ElementException;
import com.github.steanky.element.core.context.ContextManager;
import com.github.steanky.element.core.context.ElementContext;
import com.github.steanky.ethylene.core.ConfigElement;
import com.github.steanky.ethylene.core.collection.ConfigList;
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
import org.jetbrains.annotations.NotNull;
import org.phantazm.core.ElementUtils;
import org.phantazm.core.config.InstanceConfig;
import org.phantazm.core.game.scene.SceneProviderAbstract;
import org.phantazm.core.game.scene.fallback.SceneFallback;
import org.phantazm.core.instance.InstanceLoader;
import org.phantazm.core.npc.NPC;
import org.phantazm.core.npc.NPCHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Consumer;

/**
 * Basic implementation of a {@link LobbyProviderAbstract}.
 */
public class BasicLobbyProvider extends LobbyProviderAbstract {
    private static final Logger LOGGER = LoggerFactory.getLogger(BasicLobbyProvider.class);
    private static final Consumer<? super ElementException> HANDLER = ElementUtils.logging(LOGGER, "npc");

    private final InstanceLoader instanceLoader;
    private final List<String> lobbyPaths;
    private final SceneFallback fallback;
    private final InstanceConfig instanceConfig;
    private final List<ElementContext> npcContexts;
    private final boolean quittable;
    private final EventNode<Event> rootNode;

    /**
     * Creates a basic implementation of a {@link SceneProviderAbstract}.
     *
     * @param newLobbyThreshold The weighting threshold for {@link Lobby}s. If no {@link Lobby}s are above
     *                          this threshold, a new lobby will be created.
     * @param maximumLobbies    The maximum {@link Lobby}s in the provider.
     * @param instanceLoader    A {@link InstanceLoader} used to load {@link Instance}s
     * @param lobbyPaths        The paths that identify the {@link Lobby} for the {@link InstanceLoader}
     * @param fallback          A {@link SceneFallback} for the created {@link Lobby}s
     * @param instanceConfig    The {@link InstanceConfig} for the {@link Lobby}s
     */
    public BasicLobbyProvider(@NotNull Executor executor, int maximumLobbies, int newLobbyThreshold,
            @NotNull InstanceLoader instanceLoader, @NotNull List<String> lobbyPaths, @NotNull SceneFallback fallback,
            @NotNull InstanceConfig instanceConfig, @NotNull ContextManager contextManager,
            @NotNull ConfigList npcConfigs, boolean quittable, @NotNull EventNode<Event> rootNode) {
        super(executor, maximumLobbies, newLobbyThreshold);

        this.instanceLoader = Objects.requireNonNull(instanceLoader, "instanceLoader");
        this.lobbyPaths = List.copyOf(Objects.requireNonNull(lobbyPaths, "lobbyPaths"));
        this.fallback = Objects.requireNonNull(fallback, "fallback");
        this.instanceConfig = Objects.requireNonNull(instanceConfig, "instanceConfig");


        List<ElementContext> npcContexts = new ArrayList<>(npcConfigs.size());
        for (ConfigElement element : npcConfigs) {
            if (element.isNode()) {
                npcContexts.add(contextManager.makeContext(element.asNode()));
            }
        }

        this.npcContexts = List.copyOf(npcContexts);
        this.quittable = quittable;
        this.rootNode = rootNode;
    }

    @Override
    protected @NotNull CompletableFuture<Lobby> createScene(@NotNull LobbyJoinRequest request) {
        return instanceLoader.loadInstance(lobbyPaths).thenApply(instance -> {
            instance.setTime(instanceConfig.time());
            instance.setTimeRate(instanceConfig.timeRate());

            EventNode<InstanceEvent> instanceNode =
                    EventNode.type("instance_npc_node_" + instance.getUniqueId(), EventFilter.INSTANCE);

            instanceNode.addListener(PlayerSwapItemEvent.class, event -> event.setCancelled(true));
            instanceNode.addListener(ItemDropEvent.class, event -> event.setCancelled(true));
            instanceNode.addListener(InventoryPreClickEvent.class, event -> event.setCancelled(true));
            instanceNode.addListener(PlayerPreEatEvent.class, event -> event.setCancelled(true));
            instanceNode.addListener(PickupItemEvent.class, event -> event.setCancelled(true));
            instanceNode.addListener(PickupExperienceEvent.class, event -> event.setCancelled(true));
            instanceNode.addListener(PrePlayerStartDiggingEvent.class, event -> event.setCancelled(true));
            instanceNode.addListener(PlayerBlockPlaceEvent.class, event -> event.setCancelled(true));
            instanceNode.addListener(PlayerBlockInteractEvent.class, event -> {
                event.setCancelled(true);
                event.setBlockingItemUse(true);
            });
            instanceNode.addListener(PlayerBlockBreakEvent.class, event -> event.setCancelled(true));

            List<NPC> npcs = new ArrayList<>(npcContexts.size());
            for (ElementContext context : npcContexts) {
                NPC npc = context.provide(HANDLER, () -> null);
                if (npc != null) {
                    npcs.add(npc);
                }
            }

            Lobby lobby = new Lobby(UUID.randomUUID(), instance, instanceConfig, fallback,
                    new NPCHandler(List.copyOf(npcs), instance, instanceNode), quittable);
            instanceNode.addListener(PlayerDisconnectEvent.class,
                    event -> lobby.leave(Collections.singleton(event.getPlayer().getUuid())).executor()
                            .ifPresent(Runnable::run));
            rootNode.addChild(instanceNode);
            return lobby;
        });
    }

    @Override
    protected void cleanupScene(@NotNull Lobby scene) {
        NPCHandler handler = scene.handler();
        handler.end();
        rootNode.removeChild(handler.instanceNode());
    }
}
