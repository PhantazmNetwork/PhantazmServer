package com.github.phantazmnetwork.zombies.powerup;

import com.github.phantazmnetwork.zombies.player.ZombiesPlayer;
import com.github.phantazmnetwork.zombies.util.ElementUtils;
import com.github.steanky.element.core.context.ContextManager;
import com.github.steanky.element.core.dependency.DependencyProvider;
import com.github.steanky.ethylene.core.collection.ConfigList;
import com.github.steanky.ethylene.core.collection.ConfigNode;
import net.kyori.adventure.key.Key;
import net.minestom.server.instance.Instance;
import org.jetbrains.annotations.NotNull;
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

            ConfigList visualData = data.visuals();
            ConfigList actionData = data.actions();
            ConfigNode deactivationPredicateData = data.deactivationPredicate();

            Collection<Supplier<PowerupVisual>> visuals = new ArrayList<>(visualData.size());
            Collection<Supplier<PowerupAction>> actions = new ArrayList<>(actionData.size());

            ElementUtils.createElements(contextManager, visualData, visuals, "powerup visual", mapDependencyProvider,
                    LOGGER);

            ElementUtils.createElements(contextManager, actionData, actionData, "powerup action", mapDependencyProvider,
                    LOGGER);

            Supplier<DeactivationPredicate> deactivationPredicateSupplier =
                    ElementUtils.createElement(contextManager, deactivationPredicateData,
                            "powerup deactivation predicate", mapDependencyProvider, LOGGER);

            if (deactivationPredicateSupplier == null) {
                deactivationPredicateSupplier = () -> ImmediateDeactivationPredicate.INSTANCE;
            }

            powerupMap.put(dataEntry.getKey(), new PowerupComponents(visuals, actions, deactivationPredicateSupplier));
        }

        return new BasicPowerupHandler(instance, powerupMap, playerMap, powerupPickupRadius);
    }
}
