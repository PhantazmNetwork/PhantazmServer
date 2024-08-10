package org.phantazm.zombies.player.upgrade.effect.scaling;

import com.github.steanky.element.core.annotation.*;
import com.github.steanky.ethylene.mapper.annotation.Default;
import net.minestom.server.entity.Entity;
import net.minestom.server.tag.Tag;
import org.jetbrains.annotations.NotNull;
import org.phantazm.commons.InjectionStore;
import org.phantazm.zombies.player.ZombiesPlayer;
import org.phantazm.zombies.player.upgrade.PlayerUpgrade;
import org.phantazm.zombies.player.upgrade.selector.Selector;
import org.phantazm.zombies.player.upgrade.selector.SelectorComponent;
import org.phantazm.zombies.player.upgrade.trigger.TriggerData;

@Model("zombies.upgrade.effect.scaling.tag")
@Cache
public class TagScaling implements ScalingComponent {
    private final Data data;
    private final SelectorComponent selectorComponent;

    @FactoryMethod
    public TagScaling(@NotNull Data data, @NotNull @Child("selector") SelectorComponent selectorComponent) {
        this.data = data;
        this.selectorComponent = selectorComponent;
    }

    @Override
    public @NotNull Scaling apply(@NotNull InjectionStore injectionStore, @NotNull ZombiesPlayer zombiesPlayer) {
        return new Internal(data, selectorComponent.apply(injectionStore, zombiesPlayer));
    }

    private static final class Internal implements Scaling {
        private final Data data;
        private final Selector selector;
        private final Tag<Integer> levelTag;

        private Internal(Data data, Selector selector) {
            this.data = data;
            this.selector = selector;
            this.levelTag = Tag.Integer(data.tag).defaultValue(data.defaultValue);
        }

        @Override
        public double getMultiplier(@NotNull PlayerUpgrade upgrade, @NotNull ZombiesPlayer zombiesPlayer,
            @NotNull TriggerData triggerData) {
            return selector.select(upgrade, zombiesPlayer, triggerData)
                .forType(Entity.class)
                .map(entity -> entity.getTag(levelTag))
                .orElse(data.defaultValue);
        }
    }

    @Default("""
        {
          defaultValue=0,
          selector={type='zombies.upgrade.selector.self'}
        }
        """)
    @DataObject
    public record Data(@NotNull String tag,
        int defaultValue) {
    }
}
