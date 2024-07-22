package org.phantazm.zombies.player.upgrade;

import com.github.steanky.element.core.annotation.Cache;
import com.github.steanky.element.core.annotation.DataObject;
import com.github.steanky.element.core.annotation.FactoryMethod;
import com.github.steanky.element.core.annotation.Model;
import net.kyori.adventure.key.Key;
import net.minestom.server.attribute.Attribute;
import net.minestom.server.attribute.AttributeModifier;
import net.minestom.server.attribute.AttributeOperation;
import net.minestom.server.event.EventListener;
import org.jetbrains.annotations.NotNull;
import org.phantazm.commons.InjectionStore;
import org.phantazm.core.tick.Activable;
import org.phantazm.zombies.Attributes;
import org.phantazm.zombies.event.player.ZombiesPlayerKillMobEvent;
import org.phantazm.zombies.player.ZombiesPlayer;

import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

@Model("zombies.upgrade.apply_attribute_on_kill")
@Cache
public class ApplyAttributeOnKill implements PlayerUpgradeComponent {
    private final Data data;

    @FactoryMethod
    public ApplyAttributeOnKill(@NotNull Data data) {
        this.data = Objects.requireNonNull(data);
    }


    @Override
    public @NotNull PlayerUpgrade apply(@NotNull InjectionStore injectionStore, @NotNull ZombiesPlayer zombiesPlayer) {
        return new Internal(zombiesPlayer, data);
    }

    private static final class Internal extends GuardedPlayerUpgrade {
        private Internal(ZombiesPlayer zombiesPlayer, Data data) {
            super(Activable.threadsafeWrapper(new Activable() {
                private final EventListener<ZombiesPlayerKillMobEvent> listener = EventListener
                    .builder(ZombiesPlayerKillMobEvent.class).handler(this::onKillMob).build();

                private final Attribute attribute = Attributes.get(data.attribute);
                private final UUID uuid = UUID.randomUUID();
                private final AttributeModifier modifier = new AttributeModifier(uuid, uuid.toString(), data.value, data.operation);

                private final AtomicBoolean active = new AtomicBoolean();
                private final AtomicInteger timer = new AtomicInteger();

                @Override
                public void start() {
                    zombiesPlayer.getScene().sceneNode().addListener(listener);
                }

                @Override
                public void tick(long time) {
                    if (!active.get() || timer.getAndUpdate(current -> Math.max(0, current - 1)) != 1) {
                        return;
                    }

                    if (active.compareAndSet(true, false)) {
                        zombiesPlayer.getPlayer().ifPresent(player -> player.getAttribute(attribute).removeModifier(modifier));
                    }
                }

                @Override
                public void end() {
                    zombiesPlayer.getScene().sceneNode().removeListener(listener);
                }

                private void onKillMob(ZombiesPlayerKillMobEvent event) {
                    if (event.zombiesPlayer() != zombiesPlayer || !event.target().data().tags().contains(data.requiredTag)) {
                        return;
                    }

                    if (active.compareAndSet(false, true)) {
                        event.getPlayer().getAttribute(attribute).addModifier(modifier);
                        timer.set(data.attributeDuration);
                    }
                }
            }), zombiesPlayer);
        }

        @Override
        public boolean needsTicking() {
            return true;
        }
    }

    @DataObject
    public record Data(int attributeDuration,
        @NotNull Key requiredTag,
        @NotNull String attribute,
        double value,
        @NotNull AttributeOperation operation) {
    }
}
