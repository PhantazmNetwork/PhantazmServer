package org.phantazm.zombies.powerup;

import com.github.steanky.element.core.context.ContextManager;
import com.github.steanky.element.core.dependency.DependencyProvider;
import com.github.steanky.element.core.path.ElementPath;
import com.github.steanky.ethylene.core.collection.ConfigList;
import com.github.steanky.ethylene.core.collection.ConfigNode;
import net.kyori.adventure.key.Key;
import net.minestom.server.instance.Instance;
import org.jetbrains.annotations.NotNull;
import org.phantazm.zombies.player.ZombiesPlayer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.function.Supplier;

public class BasicPowerupHandlerSource implements PowerupHandler.Source {
    private static final Logger LOGGER = LoggerFactory.getLogger(BasicPowerupHandler.class);

    private final Map<Key, PowerupInfo> powerups;
    private final ContextManager contextManager;
    private final double powerupPickupRadius;

    public BasicPowerupHandlerSource(@NotNull Map<Key, PowerupInfo> powerups, @NotNull ContextManager contextManager,
            double powerupPickupRadius) {
        this.powerups = Map.copyOf(powerups);
        this.contextManager = Objects.requireNonNull(contextManager, "contextManager");
        this.powerupPickupRadius = powerupPickupRadius;
    }

    @Override
    public @NotNull PowerupHandler make(@NotNull Instance instance,
            @NotNull Map<? super UUID, ? extends ZombiesPlayer> playerMap,
            @NotNull DependencyProvider mapDependencyProvider) {
        Map<Key, PowerupComponents> powerupMap = new HashMap<>(powerups.size());

        for (Map.Entry<Key, PowerupInfo> dataEntry : powerups.entrySet()) {
            PowerupInfo data = dataEntry.getValue();

            Collection<Supplier<PowerupVisual>> visuals = contextManager.makeContext(data.visuals())
                    .provideCollection(ElementPath.EMPTY, mapDependencyProvider);

            Collection<Supplier<PowerupAction>> actions = contextManager.makeContext(data.actions())
                    .provideCollection(ElementPath.EMPTY, mapDependencyProvider);

            Supplier<DeactivationPredicate> deactivationPredicateSupplier =
                    contextManager.makeContext(data.deactivationPredicate()).provide(mapDependencyProvider);
            
            powerupMap.put(dataEntry.getKey(), new PowerupComponents(visuals, actions, deactivationPredicateSupplier));
        }

        return new BasicPowerupHandler(instance, powerupMap, playerMap, powerupPickupRadius);
    }
}
