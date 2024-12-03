package org.phantazm.zombies.player.upgrade.effect;

import com.github.steanky.element.core.annotation.*;
import com.github.steanky.ethylene.mapper.annotation.Default;
import net.minestom.server.entity.Entity;
import net.minestom.server.tag.Tag;
import org.jetbrains.annotations.NotNull;
import org.phantazm.commons.InjectionStore;
import org.phantazm.core.Target;
import org.phantazm.zombies.player.ZombiesPlayer;
import org.phantazm.zombies.player.upgrade.PlayerUpgrade;
import org.phantazm.zombies.player.upgrade.selector.Selector;
import org.phantazm.zombies.player.upgrade.selector.SelectorComponent;
import org.phantazm.zombies.player.upgrade.trigger.TriggerData;

import java.util.Optional;
import java.util.UUID;

@Model("zombies.upgrade.effect.store_target")
@Cache
public class StoreTargetEffect implements UpgradeEffectComponent {
    private final SelectorComponent selector;
    private final Tag<UUID> tag;

    @FactoryMethod
    public StoreTargetEffect(@NotNull Data data, @NotNull @Child("selector") SelectorComponent selector) {
        this.selector = selector;
        this.tag = Tag.UUID(data.tag);
    }

    @Override
    public @NotNull UpgradeEffect apply(@NotNull InjectionStore injectionStore, @NotNull ZombiesPlayer zombiesPlayer) {
        return new Internal(selector.apply(injectionStore, zombiesPlayer), tag);
    }

    private static final class Internal implements UpgradeEffect {
        private final Selector selector;
        private final Tag<UUID> tag;

        private Internal(Selector selector, Tag<UUID> tag) {
            this.selector = selector;
            this.tag = tag;
        }

        @Override
        public void apply(@NotNull PlayerUpgrade upgrade, @NotNull ZombiesPlayer zombiesPlayer, @NotNull TriggerData triggerData) {
            Target target = selector.select(upgrade, zombiesPlayer, triggerData);
            Optional<? extends Entity> targetOptional = target.forType(Entity.class);
            if (targetOptional.isEmpty()) return;

            Entity targetEntity = targetOptional.get();
            zombiesPlayer.getPlayer().ifPresent(player -> player.setTag(tag, targetEntity.getUuid()));
        }

        @Override
        public void clear(@NotNull PlayerUpgrade upgrade, @NotNull ZombiesPlayer zombiesPlayer) {
            zombiesPlayer.getPlayer().ifPresent(player -> player.removeTag(tag));
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
