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
import java.util.Iterator;
import java.util.Map;
import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
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
        @NotNull UUID uuid) {
    }

    private static final class Internal implements UpgradeEffect {
        private final Tag<Queue<QueueEntry>> queueTag;

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
                Wrapper<Queue<QueueEntry>> queue = Wrapper.ofNull();

                int time = this.time.get();
                target.select(upgrade, zombiesPlayer, triggerData).forType(Entity.class, target -> {
                    if (queue.get() == null) {
                        queue.set(entity.tagHandler().updateAndGetTag(queueTag,
                            current -> current == null ? new ConcurrentLinkedQueue<>() : current));
                        targets.put(entity.getUuid(), new WeakReference<>(entity));
                    }

                    queue.get().add(new QueueEntry(time, target.getUuid()));
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

                Queue<QueueEntry> queue = entity.getTag(queueTag);
                if (queue == null || queue.isEmpty()) {
                    return true;
                }

                Iterator<QueueEntry> entryIterator = queue.iterator();
                while (entryIterator.hasNext()) {
                    if (time - entryIterator.next().time >= data.duration) {
                        entryIterator.remove();
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
