package com.github.phantazmnetwork.zombies.powerup;

import com.github.phantazmnetwork.zombies.map.PowerupInfo;
import com.github.phantazmnetwork.zombies.player.ZombiesPlayer;
import com.github.phantazmnetwork.zombies.util.ElementUtils;
import com.github.steanky.element.core.context.ContextManager;
import com.github.steanky.element.core.dependency.DependencyProvider;
import com.github.steanky.ethylene.core.collection.ConfigList;
import com.github.steanky.ethylene.core.collection.ConfigNode;
import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.function.Supplier;

public class BasicPowerupHandlerSource implements PowerupHandler.Source {
    private static final Logger LOGGER = LoggerFactory.getLogger(BasicPowerupHandler.class);

    private final List<PowerupInfo> powerupData;
    private final ContextManager contextManager;
    private final double powerupPickupRadius;

    public BasicPowerupHandlerSource(@NotNull List<PowerupInfo> powerupData, @NotNull ContextManager contextManager,
            double powerupPickupRadius) {
        this.powerupData = List.copyOf(powerupData);
        this.contextManager = Objects.requireNonNull(contextManager, "contextManager");
        this.powerupPickupRadius = powerupPickupRadius;
    }

    @Override
    public @NotNull PowerupHandler make(@NotNull Map<? super UUID, ? extends ZombiesPlayer> playerMap,
            @NotNull DependencyProvider mapDependencyProvider) {
        Map<Key, PowerupComponents> powerupMap = new HashMap<>(powerupData.size());

        for (PowerupInfo data : powerupData) {
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

            powerupMap.put(data.id(), new PowerupComponents(visuals, actions, deactivationPredicateSupplier));
        }

        return new BasicPowerupHandler(powerupMap, playerMap, powerupPickupRadius);
    }
}
