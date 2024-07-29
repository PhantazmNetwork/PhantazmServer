package org.phantazm.zombies.player.upgrade.effect;

import com.github.steanky.element.core.annotation.*;
import net.minestom.server.entity.Entity;
import net.minestom.server.tag.Tag;
import org.jetbrains.annotations.NotNull;
import org.phantazm.commons.InjectionStore;
import org.phantazm.zombies.player.ZombiesPlayer;
import org.phantazm.zombies.player.upgrade.PlayerUpgrade;
import org.phantazm.zombies.player.upgrade.selector.Selector;
import org.phantazm.zombies.player.upgrade.selector.SelectorComponent;
import org.phantazm.zombies.player.upgrade.trigger.TriggerData;

@Model("zombies.upgrade.effect.adjust_integer_tag")
@Cache
public class AdjustIntegerTagEffect implements UpgradeEffectComponent {
    private final Data data;
    private final SelectorComponent selector;

    @FactoryMethod
    public AdjustIntegerTagEffect(@NotNull Data data, @NotNull @Child("selector") SelectorComponent selector) {
        this.data = data;
        this.selector = selector;
    }

    @Override
    public @NotNull UpgradeEffect apply(@NotNull InjectionStore injectionStore, @NotNull ZombiesPlayer zombiesPlayer) {
        return new Internal(data, selector.apply(injectionStore, zombiesPlayer));
    }

    private static final class Internal implements UpgradeEffect {
        private final Data data;
        private final Tag<Integer> tag;
        private final Selector selector;

        private Internal(Data data, Selector selector) {
            this.data = data;
            this.tag = Tag.Integer(data.tag);
            this.selector = selector;
        }

        @Override
        public void apply(@NotNull PlayerUpgrade upgrade, @NotNull ZombiesPlayer zombiesPlayer,
            @NotNull TriggerData triggerData) {
            selector.select(upgrade, zombiesPlayer, triggerData).forType(Entity.class, target -> {
                target.tagHandler().updateTag(tag, currentValue -> {
                    int next = currentValue + data.increment;
                    return data.increment < 0 ? Math.max(next, data.limit) : Math.min(next, data.limit);
                });
            });
        }
    }

    @DataObject
    public record Data(@NotNull String tag,
        int increment,
        int limit) {
    }
}
