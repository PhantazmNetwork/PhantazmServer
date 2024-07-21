package org.phantazm.zombies.player.upgrade.trigger;

import com.github.steanky.element.core.annotation.Cache;
import com.github.steanky.element.core.annotation.DataObject;
import com.github.steanky.element.core.annotation.FactoryMethod;
import com.github.steanky.element.core.annotation.Model;
import net.minestom.server.event.Event;
import net.minestom.server.event.EventListener;
import org.jetbrains.annotations.NotNull;
import org.phantazm.commons.InjectionStore;
import org.phantazm.zombies.player.ZombiesPlayer;
import org.phantazm.zombies.player.upgrade.PlayerUpgrade;
import org.phantazm.zombies.player.upgrade.effect.UpgradeEffect;
import org.phantazm.zombies.player.upgrade.trigger.filter.EventFilter;
import org.phantazm.zombies.player.upgrade.trigger.filter.EventFilterComponent;

import java.util.concurrent.atomic.AtomicReference;

@Model("zombies.upgrade.trigger.event")
@Cache
public class EventTrigger implements UpgradeTriggerComponent {
    private final Class<? extends Event> eventClass;
    private final EventFilterComponent eventFilterComponent;

    @FactoryMethod
    public EventTrigger(@NotNull Data data, @NotNull EventFilterComponent eventFilterComponent) {
        this.eventClass = resolveEventClass(data.eventClass);
        this.eventFilterComponent = eventFilterComponent;
    }

    @SuppressWarnings("unchecked")
    private static Class<? extends Event> resolveEventClass(String name) {
        try {
            Class<?> cls = Class.forName(name);
            if (Event.class.isAssignableFrom(cls)) {
                return (Class<? extends Event>) cls;
            }
        } catch (ClassNotFoundException ignored) {
        }

        return null;
    }

    @Override
    public @NotNull UpgradeTrigger apply(@NotNull InjectionStore injectionStore, @NotNull ZombiesPlayer zombiesPlayer) {
        return eventClass == null ? UpgradeTrigger.NONE : new Internal(zombiesPlayer, eventClass,
            eventFilterComponent.apply(injectionStore, zombiesPlayer));
    }

    private static final class Internal implements UpgradeTrigger {
        private final ZombiesPlayer zombiesPlayer;
        private final EventListener<? extends Event> listener;
        private final EventFilter eventFilter;

        private final AtomicReference<ArmData> target;

        private record ArmData(PlayerUpgrade upgrade,
            UpgradeEffect effect) {
        }

        private Internal(ZombiesPlayer zombiesPlayer, Class<? extends Event> cls, EventFilter eventFilter) {
            this.zombiesPlayer = zombiesPlayer;
            this.listener = EventListener.builder(cls).handler(this::handle).filter(this::filter).build();
            this.eventFilter = eventFilter;

            this.target = new AtomicReference<>();
        }

        @Override
        public void arm(@NotNull PlayerUpgrade upgrade, @NotNull UpgradeEffect effect) {
            target.compareAndSet(null, new ArmData(upgrade, effect));
            zombiesPlayer.getScene().sceneNode().addListener(listener);
        }

        @Override
        public void disarm() {
            zombiesPlayer.getScene().sceneNode().removeListener(listener);
            target.set(null);
        }

        private boolean filter(Event event) {
            return eventFilter.test(event);
        }

        private void handle(Event event) {
            ArmData armData = target.get();
            if (armData == null) {
                return;
            }

            armData.effect.apply(armData.upgrade, zombiesPlayer);
        }
    }

    @DataObject
    public record Data(@NotNull String eventClass) {
    }
}
