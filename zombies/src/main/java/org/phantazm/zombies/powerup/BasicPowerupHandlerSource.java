package org.phantazm.zombies.powerup;

import com.github.steanky.element.core.ElementException;
import com.github.steanky.element.core.context.ContextManager;
import com.github.steanky.element.core.path.ElementPath;
import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.NotNull;
import org.phantazm.core.ElementUtils;
import org.phantazm.zombies.powerup.action.component.PowerupActionComponent;
import org.phantazm.zombies.powerup.predicate.AlwaysPickupPredicate;
import org.phantazm.zombies.powerup.predicate.DeactivationPredicateComponent;
import org.phantazm.zombies.powerup.predicate.ImmediateDeactivationPredicate;
import org.phantazm.zombies.powerup.predicate.PickupPredicateComponent;
import org.phantazm.zombies.powerup.visual.PowerupVisualComponent;
import org.phantazm.zombies.scene.ZombiesScene;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class BasicPowerupHandlerSource implements PowerupHandler.Source {
    private static final Logger LOGGER = LoggerFactory.getLogger(BasicPowerupHandler.class);
    private static final Consumer<? super ElementException> HANDLER = ElementUtils.logging(LOGGER, "powerup");

    private final Map<Key, PowerupComponents> powerups;

    public BasicPowerupHandlerSource(@NotNull Map<Key, PowerupData> powerupData,
            @NotNull ContextManager contextManager) {
        Map<Key, PowerupComponents> powerupMap = new HashMap<>(powerupData.size());

        for (Map.Entry<Key, PowerupData> dataEntry : powerupData.entrySet()) {
            PowerupData data = dataEntry.getValue();

            Collection<PowerupVisualComponent> visuals =
                    contextManager.makeContext(data.visuals()).provideCollection(ElementPath.EMPTY, HANDLER);

            Collection<PowerupActionComponent> actions =
                    contextManager.makeContext(data.actions()).provideCollection(ElementPath.EMPTY, HANDLER);

            DeactivationPredicateComponent deactivationPredicate;
            try {
                deactivationPredicate = contextManager.makeContext(data.deactivationPredicate()).provide();
            }
            catch (ElementException e) {
                HANDLER.accept(e);
                deactivationPredicate = (scene) -> ImmediateDeactivationPredicate.INSTANCE;
            }

            PickupPredicateComponent pickupPredicateComponent;
            if (data.pickupPredicate() == null) {
                pickupPredicateComponent = player -> AlwaysPickupPredicate.INSTANCE;
            }
            else {
                try {
                    pickupPredicateComponent = contextManager.makeContext(data.pickupPredicate()).provide();
                }
                catch (ElementException e) {
                    HANDLER.accept(e);
                    pickupPredicateComponent = player -> AlwaysPickupPredicate.INSTANCE;
                }
            }

            powerupMap.put(dataEntry.getKey(),
                    new PowerupComponents(visuals, actions, deactivationPredicate, pickupPredicateComponent));
        }

        this.powerups = Map.copyOf(powerupMap);
    }

    @Override
    public @NotNull PowerupHandler make(@NotNull Supplier<? extends @NotNull ZombiesScene> scene) {
        return new BasicPowerupHandler(scene, powerups);
    }
}
