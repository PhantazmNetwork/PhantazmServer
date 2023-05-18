package org.phantazm.zombies.powerup;

import com.github.steanky.element.core.ElementException;
import com.github.steanky.element.core.context.ContextManager;
import com.github.steanky.element.core.dependency.DependencyProvider;
import com.github.steanky.element.core.path.ElementPath;
import net.kyori.adventure.key.Key;
import net.minestom.server.instance.Instance;
import org.jetbrains.annotations.NotNull;
import org.phantazm.core.ElementUtils;
import org.phantazm.zombies.player.ZombiesPlayer;
import org.phantazm.zombies.powerup.action.PowerupAction;
import org.phantazm.zombies.powerup.predicate.DeactivationPredicate;
import org.phantazm.zombies.powerup.predicate.ImmediateDeactivationPredicate;
import org.phantazm.zombies.powerup.visual.PowerupVisual;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class BasicPowerupHandlerSource implements PowerupHandler.Source {
    private static final Logger LOGGER = LoggerFactory.getLogger(BasicPowerupHandler.class);
    private static final Consumer<? super ElementException> HANDLER = ElementUtils.logging(LOGGER, "powerup");

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
                    .provideCollection(ElementPath.EMPTY, mapDependencyProvider, HANDLER);

            Collection<Supplier<PowerupAction>> actions = contextManager.makeContext(data.actions())
                    .provideCollection(ElementPath.EMPTY, mapDependencyProvider, HANDLER);

            Supplier<DeactivationPredicate> deactivationPredicateSupplier;
            try {
                deactivationPredicateSupplier =
                        contextManager.makeContext(data.deactivationPredicate()).provide(mapDependencyProvider);
            }
            catch (ElementException e) {
                HANDLER.accept(e);
                deactivationPredicateSupplier = () -> ImmediateDeactivationPredicate.INSTANCE;
            }

            powerupMap.put(dataEntry.getKey(), new PowerupComponents(visuals, actions, deactivationPredicateSupplier));
        }

        return new BasicPowerupHandler(instance, powerupMap, playerMap, powerupPickupRadius);
    }
}
