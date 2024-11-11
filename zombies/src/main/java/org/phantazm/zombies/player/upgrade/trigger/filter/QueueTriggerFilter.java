package org.phantazm.zombies.player.upgrade.trigger.filter;

import com.github.steanky.element.core.annotation.*;
import com.github.steanky.ethylene.mapper.annotation.Default;
import net.minestom.server.entity.Entity;
import net.minestom.server.tag.Tag;
import org.jetbrains.annotations.NotNull;
import org.phantazm.commons.InjectionStore;
import org.phantazm.core.TagUtils;
import org.phantazm.zombies.player.ZombiesPlayer;
import org.phantazm.zombies.player.upgrade.PlayerUpgrade;
import org.phantazm.zombies.player.upgrade.effect.RecordTargetEffect;
import org.phantazm.zombies.player.upgrade.selector.Selector;
import org.phantazm.zombies.player.upgrade.selector.SelectorComponent;
import org.phantazm.zombies.player.upgrade.trigger.TriggerData;

import java.util.Collection;
import java.util.Map;

@Model("zombies.upgrade.filter.queue")
@Cache
public class QueueTriggerFilter implements TriggerFilterComponent {
    private final Data data;
    private final SelectorComponent queueComponent;
    private final SelectorComponent targetComponent;

    @FactoryMethod
    public QueueTriggerFilter(@NotNull Data data, @NotNull @Child("queue") SelectorComponent queueComponent,
        @NotNull @Child("target") SelectorComponent targetComponent) {
        this.data = data;
        this.queueComponent = queueComponent;
        this.targetComponent = targetComponent;
    }

    @Override
    public @NotNull TriggerFilter apply(@NotNull InjectionStore injectionStore, @NotNull ZombiesPlayer zombiesPlayer) {
        return new Internal(data, queueComponent.apply(injectionStore, zombiesPlayer),
            targetComponent.apply(injectionStore, zombiesPlayer), TagUtils.transientTag(data.queueName));
    }

    private record Internal(Data data,
        Selector queueSelector,
        Selector targetSelector,
        Tag<Map<RecordTargetEffect.QueueEntry, Void>> tag) implements TriggerFilter {
        public boolean test(@NotNull PlayerUpgrade upgrade, @NotNull ZombiesPlayer zombiesPlayer, @NotNull TriggerData triggerData) {
            Collection<? extends Entity> queueHolders = queueSelector.select(upgrade, zombiesPlayer, triggerData).targets();

            if (queueHolders.isEmpty()) {
                return !data.whitelist;
            }

            Collection<? extends Entity> targets = targetSelector.select(upgrade, zombiesPlayer, triggerData).targets();
            if (targets.isEmpty()) {
                return !data.whitelist;
            }

            for (Entity queueHolder : queueHolders) {
                Map<RecordTargetEffect.QueueEntry, Void> queue = queueHolder.getTag(tag);
                if (queue == null || queue.isEmpty()) {
                    if (data.whitelist && data.allTargetsMustMatch) {
                        // empty whitelist queue, any target will fail, but all targets must succeed
                        return false;
                    }

                    if (!data.whitelist && !data.allTargetsMustMatch) {
                        // empty blacklist queue, any target will succeed, and at least one target must succeed
                        return true;
                    }

                    continue;
                }

                for (Entity queueTarget : targets) {
                    boolean matches = queue.containsKey(new RecordTargetEffect.QueueEntry(-1,
                        queueTarget.getUuid())) == data.whitelist;
                    if (data.allTargetsMustMatch && !matches) {
                        return false;
                    }

                    if (!data.allTargetsMustMatch && matches) {
                        return true;
                    }
                }
            }

            return !data.whitelist;
        }
    }

    @Default("""
        {
          allTargetsMustMatch=true,
          whitelist=true,
          queue={type='zombies.upgrade.selector.self'}
        }
        """)
    @DataObject
    public record Data(@NotNull String queueName,
        boolean allTargetsMustMatch,
        boolean whitelist) {
    }
}
