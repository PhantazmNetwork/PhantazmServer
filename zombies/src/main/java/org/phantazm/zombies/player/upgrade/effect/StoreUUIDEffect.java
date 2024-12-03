package org.phantazm.zombies.player.upgrade.effect;

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

@Model("zombies.upgrade.effect.store_target_uuid")
@Cache
public class StoreUUIDEffect implements UpgradeEffectComponent {
    private final SelectorComponent selector;
    private final Tag<List<UUID>> tag;

    @FactoryMethod
    public StoreUUIDEffect(@NotNull Data data, @NotNull @Child("selector") SelectorComponent selector) {
        this.selector = selector;
        this.tag = Tag.UUID(data.tag).list();
    }

    @Override
    public @NotNull UpgradeEffect apply(@NotNull InjectionStore injectionStore, @NotNull ZombiesPlayer zombiesPlayer) {
        return new Internal(selector.apply(injectionStore, zombiesPlayer), tag);
    }

    private static final class Internal implements UpgradeEffect {
        private final Selector selector;
        private final Tag<List<UUID>> tag;

        private Internal(Selector selector, Tag<List<UUID>> tag) {
            this.selector = selector;
            this.tag = tag;
        }

        @Override
        public void apply(@NotNull PlayerUpgrade upgrade, @NotNull ZombiesPlayer zombiesPlayer, @NotNull TriggerData triggerData) {
            Collection<? extends Entity> targets = selector.select(upgrade, zombiesPlayer, triggerData).targets();
            if (targets.isEmpty()) return;

            ZombiesTagUtils.sceneLocalTags(zombiesPlayer).setTag(tag, targets.stream().map(Entity::getUuid).toList());
        }

        @Override
        public void clear(@NotNull PlayerUpgrade upgrade, @NotNull ZombiesPlayer zombiesPlayer) {
            ZombiesTagUtils.sceneLocalTags(zombiesPlayer).removeTag(tag);
        }
    }

    @Default("""
        {
          selector={type='zombies.upgrade.selector.event', useTarget=true}
        }
        """)
    @DataObject
    public record Data(@NotNull String tag) {
    }
}
