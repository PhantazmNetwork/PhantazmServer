package org.phantazm.core.scene2.lobby;

import net.minestom.server.entity.Player;
import net.minestom.server.instance.Instance;
import net.minestom.server.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.phantazm.commons.BasicComponent;
import org.phantazm.commons.InjectionStore;
import org.phantazm.core.config.InstanceConfig;
import org.phantazm.core.instance.InstanceLoader;
import org.phantazm.core.npc.NPC;
import org.phantazm.core.npc.NPCHandler;
import org.phantazm.core.scene2.SceneCreator;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

public class LobbyCreator implements SceneCreator<Lobby> {
    private final InstanceLoader instanceLoader;
    private final List<String> lobbyPath;
    private final InstanceConfig instanceConfig;
    private final String lobbyJoinMessageFormat;
    private final List<BasicComponent<NPC>> npcs;
    private final List<ItemStack> defaultItems;
    private final Function<? super Player, ? extends CompletableFuture<?>> displayNameStyler;

    private final int sceneCap;
    private final int playerCap;

    public LobbyCreator(@NotNull InstanceLoader instanceLoader, @NotNull List<String> lobbyPath,
        @NotNull InstanceConfig instanceConfig, @NotNull String lobbyJoinMessageFormat,
        @NotNull List<BasicComponent<NPC>> npcs, @NotNull List<ItemStack> defaultItems,
        @NotNull Function<? super @NotNull Player, ? extends @NotNull CompletableFuture<?>> displayNameStyler,
        int sceneCap, int playerCap) {
        this.instanceLoader = Objects.requireNonNull(instanceLoader);
        this.lobbyPath = List.copyOf(lobbyPath);
        this.instanceConfig = Objects.requireNonNull(instanceConfig);
        this.lobbyJoinMessageFormat = Objects.requireNonNull(lobbyJoinMessageFormat);
        this.npcs = List.copyOf(npcs);
        this.defaultItems = List.copyOf(defaultItems);
        this.displayNameStyler = Objects.requireNonNull(displayNameStyler);

        this.sceneCap = sceneCap;
        this.playerCap = playerCap;
    }

    @Override
    public @NotNull Lobby createScene() {
        Instance instance = instanceLoader.loadInstance(lobbyPath).join();
        instance.setTime(instanceConfig.time());
        instance.setTimeRate(instanceConfig.timeRate());

        InjectionStore store = InjectionStore.of();

        List<NPC> npcs = new ArrayList<>(this.npcs.size());
        for (BasicComponent<NPC> component : this.npcs) {
            npcs.add(component.apply(store));
        }

        NPCHandler handler = new NPCHandler(npcs, instance);
        return new Lobby(instance, instanceConfig.spawnPoint(), lobbyJoinMessageFormat, handler, defaultItems,
            displayNameStyler);
    }

    @Override
    public int sceneCap() {
        return sceneCap;
    }

    @Override
    public int playerCap() {
        return playerCap;
    }
}
