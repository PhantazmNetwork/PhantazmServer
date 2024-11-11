package org.phantazm.zombies.player.upgrade.effect;

import com.github.steanky.element.core.annotation.*;
import com.github.steanky.ethylene.mapper.annotation.Default;
import com.github.steanky.toolkit.collection.Wrapper;
import net.minestom.server.entity.Entity;
import net.minestom.server.tag.Tag;
import org.jetbrains.annotations.NotNull;
import org.phantazm.commons.InjectionStore;
import org.phantazm.core.TagUtils;
import org.phantazm.zombies.player.ZombiesPlayer;
import org.phantazm.zombies.player.upgrade.PlayerUpgrade;
import org.phantazm.zombies.player.upgrade.selector.Selector;
import org.phantazm.zombies.player.upgrade.selector.SelectorComponent;
import org.phantazm.zombies.player.upgrade.trigger.TriggerData;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

@Model("zombies.upgrade.effect.record_target")
@Cache
public class RecordTargetEffect implements UpgradeEffectComponent {
    private final Data data;
    private final SelectorComponent target;
    private final SelectorComponent queue;

    @FactoryMethod
    public RecordTargetEffect(@NotNull Data data, @NotNull @Child("target") SelectorComponent target,
        @NotNull @Child("queue") SelectorComponent queue) {
        this.data = data;
        this.target = target;
        this.queue = queue;
    }

    @Override
    public @NotNull UpgradeEffect apply(@NotNull InjectionStore injectionStore, @NotNull ZombiesPlayer zombiesPlayer) {
        return new Internal(data, target.apply(injectionStore, zombiesPlayer),
            queue.apply(injectionStore, zombiesPlayer), data.queueName);
    }

    public record QueueEntry(int time,
        @NotNull UUID uuid) implements Comparable<QueueEntry> {
        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }

            return obj == this || (obj instanceof QueueEntry other && Objects.equals(other.uuid, uuid));
        }

        @Override
        public int hashCode() {
            return uuid.hashCode();
        }

        @Override
        public int compareTo(@NotNull RecordTargetEffect.QueueEntry o) {
            return Integer.compare(time, o.time);
        }
    }

    private static final class Internal implements UpgradeEffect {
        private final Tag<Map<QueueEntry, Void>> queueTag;

        private final Data data;
        private final Selector target;
        private final Selector queueSelector;

        private final AtomicInteger time;
        private final Map<UUID, Reference<Entity>> targets;

        private Internal(Data data, Selector target, Selector queue, String queueName) {
            this.queueTag = TagUtils.transientTag(queueName);

            this.data = data;
            this.target = target;
            this.queueSelector = queue;
            this.time = new AtomicInteger();
            this.targets = new ConcurrentHashMap<>();
        }

        @Override
        public void apply(@NotNull PlayerUpgrade upgrade, @NotNull ZombiesPlayer zombiesPlayer,
            @NotNull TriggerData triggerData) {
            queueSelector.select(upgrade, zombiesPlayer, triggerData).forType(Entity.class, entity -> {
                Wrapper<Map<QueueEntry, Void>> queue = Wrapper.ofNull();

                int time = this.time.get();
                target.select(upgrade, zombiesPlayer, triggerData).forType(Entity.class, target -> {
                    if (queue.get() == null) {
                        queue.set(entity.tagHandler().updateAndGetTag(queueTag, current -> current == null ?
                            new ConcurrentSkipListMap<>() : current));
                        targets.put(entity.getUuid(), new WeakReference<>(entity));
                    }

                    QueueEntry newEntry = new QueueEntry(time, target.getUuid());
                    queue.get().put(newEntry, null);
                });
            });
        }

        @Override
        public void clear(@NotNull PlayerUpgrade upgrade, @NotNull ZombiesPlayer zombiesPlayer) {
            targets.values().removeIf(entityReference -> {
                Entity entity = entityReference.get();
                if (entity == null) {
                    return true;
                }

                entity.setTag(queueTag, null);
                return true;
            });
        }

        @Override
        public boolean needsTicking() {
            return true;
        }

        @Override
        public void tick() {
            int time = this.time.getAndIncrement();

            targets.values().removeIf(entityReference -> {
                Entity entity = entityReference.get();
                if (entity == null) {
                    return true;
                }

                Map<QueueEntry, Void> queue = entity.getTag(queueTag);
                if (queue == null || queue.isEmpty()) {
                    return true;
                }

                if (data.duration < 0) {
                    return false;
                }

                Iterator<QueueEntry> keyIterator = queue.keySet().iterator();
                while (keyIterator.hasNext()) {
                    if (time - keyIterator.next().time >= data.duration) {
                        keyIterator.remove();
                        continue;
                    }

                    break;
                }

                return queue.isEmpty();
            });
        }
    }

    @Default("""
        {
          queue={type='zombies.upgrade.selector.self'}
        }
        """)
    @DataObject
    public record Data(int duration,
        @NotNull String queueName) {
    }
}
