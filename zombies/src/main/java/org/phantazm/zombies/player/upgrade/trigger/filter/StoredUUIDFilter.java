package org.phantazm.zombies.player.upgrade.trigger.filter;

import com.github.steanky.element.core.annotation.*;
import com.github.steanky.ethylene.mapper.annotation.Default;
import net.minestom.server.entity.Entity;
import net.minestom.server.tag.Tag;
import org.jetbrains.annotations.NotNull;
import org.phantazm.commons.InjectionStore;
import org.phantazm.zombies.ZombiesTagUtils;
import org.phantazm.zombies.player.ZombiesPlayer;
import org.phantazm.zombies.player.upgrade.PlayerUpgrade;
import org.phantazm.zombies.player.upgrade.selector.Selector;
import org.phantazm.zombies.player.upgrade.selector.SelectorComponent;
import org.phantazm.zombies.player.upgrade.trigger.TriggerData;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Model("zombies.upgrade.filter.stored_uuid")
@Cache
public class StoredUUIDFilter implements TriggerFilterComponent {
    private final Data data;
    private final SelectorComponent targetComponent;

    @FactoryMethod
    public StoredUUIDFilter(@NotNull Data data, @NotNull @Child("target") SelectorComponent targetComponent) {
        this.data = data;
        this.targetComponent = targetComponent;
    }

    @Override
    public @NotNull TriggerFilter apply(@NotNull InjectionStore injectionStore, @NotNull ZombiesPlayer zombiesPlayer) {
        return new Internal(data, targetComponent.apply(injectionStore, zombiesPlayer), Tag.UUID(data.tag).list()
            .defaultValue(List.of()));
    }

    private record Internal(Data data,
        Selector targetSelector,
        Tag<List<UUID>> tag) implements TriggerFilter {
        public boolean test(@NotNull PlayerUpgrade upgrade, @NotNull ZombiesPlayer zombiesPlayer,
            @NotNull TriggerData triggerData) {
            Collection<? extends Entity> targets = targetSelector.select(upgrade, zombiesPlayer, triggerData).targets();
            if (targets.isEmpty()) {
                // whitelist with no targets is always false
                // blacklist with no targets is always true
                return !data.whitelist;
            }

            List<UUID> tags = ZombiesTagUtils.sceneLocalTags(zombiesPlayer).getTag(tag);
            if (tags.isEmpty()) {
                // empty tags list means tags.contains is always false
                return !data.whitelist;
            }

            boolean anyMatch = false;
            for (Entity entity : targets) {
                boolean match = tags.contains(entity.getUuid()) == data.whitelist;

                if (!match && data.allMustMatch) return false;
                else if (match) anyMatch = true;
            }

            return anyMatch;
        }
    }

    @Default("""
        {
          whitelist=true,
          allMustMatch=true
        }
        """)
    @DataObject
    public record Data(@NotNull String tag,
        boolean whitelist,
        boolean allMustMatch) {
    }
}
