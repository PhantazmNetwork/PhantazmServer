package org.phantazm.zombies.player.upgrade.effect;

import com.github.steanky.element.core.annotation.*;
import com.github.steanky.ethylene.mapper.annotation.Default;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.Player;
import net.minestom.server.tag.Tag;
import net.minestom.server.tag.TagHandler;
import org.jetbrains.annotations.NotNull;
import org.phantazm.commons.InjectionStore;
import org.phantazm.core.Interval;
import org.phantazm.zombies.ZombiesTagUtils;
import org.phantazm.zombies.player.ZombiesPlayer;
import org.phantazm.zombies.player.upgrade.PlayerUpgrade;
import org.phantazm.zombies.player.upgrade.selector.Selector;
import org.phantazm.zombies.player.upgrade.selector.SelectorComponent;
import org.phantazm.zombies.player.upgrade.trigger.TriggerData;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Model("zombies.upgrade.effect.set_boolean_tag")
@Cache
public class SetBooleanTagEffect implements UpgradeEffectComponent {
    private final Data data;
    private final SelectorComponent selector;

    @FactoryMethod
    public SetBooleanTagEffect(@NotNull Data data, @NotNull @Child("selector") SelectorComponent selector) {
        this.data = data;
        this.selector = selector;
    }

    @Override
    public @NotNull UpgradeEffect apply(@NotNull InjectionStore injectionStore, @NotNull ZombiesPlayer zombiesPlayer) {
        return new Internal(data, selector.apply(injectionStore, zombiesPlayer));
    }

    private static final class Internal implements UpgradeEffect {
        private final Tag<Boolean> tag;
        private final Data data;
        private final Selector selector;

        private final Map<UUID, Reference<Entity>> map;
        private final Interval interval;

        private Internal(Data data, Selector selector) {
            this.tag = Tag.Boolean(data.tag);
            this.data = data;
            this.selector = selector;

            this.map = new ConcurrentHashMap<>();
            this.interval = Interval.of(20);
        }

        @Override
        public void apply(@NotNull PlayerUpgrade upgrade, @NotNull ZombiesPlayer zombiesPlayer,
            @NotNull TriggerData triggerData) {
            selector.select(upgrade, zombiesPlayer, triggerData).forType(Entity.class, target -> {
                TagHandler handler = ZombiesTagUtils.sceneLocalTags(zombiesPlayer.getScene(), target);

                handler.setTag(tag, data.value);
                map.putIfAbsent(target.getUuid(), new WeakReference<>(target));
            });
        }

        @Override
        public void clear(@NotNull PlayerUpgrade upgrade, @NotNull ZombiesPlayer zombiesPlayer) {
            map.values().removeIf(reference -> {
                Entity entity = reference.get();
                if (entity == null || entity.isRemoved()) {
                    return true;
                }

                TagHandler handler = ZombiesTagUtils.sceneLocalTags(zombiesPlayer.getScene(), entity);
                if (data.ephemeral && entity instanceof Player) {
                    handler.removeTag(tag);
                    return true;
                }

                return false;
            });
        }

        @Override
        public boolean needsTicking() {
            return true;
        }

        @Override
        public void tick() {
            if (interval.advance()) {
                map.values().removeIf(reference -> reference.refersTo(null));
            }
        }
    }

    @Default("""
        {
          selector={type='zombies.upgrade.selector.self'},
          value=true,
          ephemeral=false
        }
        """)
    @DataObject
    public record Data(@NotNull String tag,
        boolean value,
        boolean ephemeral) {
    }
}
