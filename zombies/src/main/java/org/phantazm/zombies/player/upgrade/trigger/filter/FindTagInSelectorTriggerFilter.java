package org.phantazm.zombies.player.upgrade.trigger.filter;

import com.github.steanky.element.core.annotation.*;
import com.github.steanky.ethylene.mapper.annotation.Default;
import net.minestom.server.entity.Entity;
import net.minestom.server.tag.Tag;
import org.jetbrains.annotations.NotNull;
import org.phantazm.commons.InjectionStore;
import org.phantazm.core.CompareCondition;
import org.phantazm.zombies.ZombiesTagUtils;
import org.phantazm.zombies.player.ZombiesPlayer;
import org.phantazm.zombies.player.upgrade.PlayerUpgrade;
import org.phantazm.zombies.player.upgrade.selector.Selector;
import org.phantazm.zombies.player.upgrade.selector.SelectorComponent;
import org.phantazm.zombies.player.upgrade.trigger.TriggerData;

import java.util.Collection;

@Model("zombies.upgrade.filter.find_tag_in_selector")
@Cache
public class FindTagInSelectorTriggerFilter implements TriggerFilterComponent {
    private final Data data;
    private final SelectorComponent selectorComponent;

    @FactoryMethod
    public FindTagInSelectorTriggerFilter(@NotNull Data data, @NotNull @Child("selector") SelectorComponent selectorComponent) {
        this.data = data;
        this.selectorComponent = selectorComponent;
    }

    @Override
    public @NotNull TriggerFilter apply(@NotNull InjectionStore injectionStore, @NotNull ZombiesPlayer zombiesPlayer) {
        return new Internal(data, selectorComponent.apply(injectionStore, zombiesPlayer), Tag.Integer(data.tag).defaultValue(data.defaultIfTagNotPresent));
    }

    private record Internal(Data data,
        Selector selector,
        Tag<Integer> tag) implements TriggerFilter {
        public boolean test(@NotNull PlayerUpgrade upgrade, @NotNull ZombiesPlayer zombiesPlayer, @NotNull TriggerData triggerData) {
            Collection<? extends Entity> targets = selector.select(upgrade, zombiesPlayer, triggerData).targets();

            if (targets.isEmpty()) {
                return false;
            }

            if (data.allTargetsMustMatch) {
                for (Entity target : targets) {
                    int value = ZombiesTagUtils.sceneLocalTags(zombiesPlayer.getScene(), target).getTag(tag);
                    if (!data.condition.compare(data.value, value)) {
                        return false;
                    }
                }

                return true;
            }
            for (Entity target : targets) {
                if (data.condition.compare(data.value, ZombiesTagUtils.sceneLocalTags(zombiesPlayer.getScene(), target).getTag(tag))) {
                    return true;
                }
            }

            return false;
        }
    }

    @Default("""
        {
          allTargetsMustMatch=true,
          condition='EQUAL_TO',
          defaultIfTagNotPresent=0,
          selector={type='zombies.upgrade.selector.self'}
        }
        """)
    @DataObject
    public record Data(@NotNull String tag,
        boolean allTargetsMustMatch,
        @NotNull CompareCondition condition,
        int value,
        int defaultIfTagNotPresent) {
    }
}
