package org.phantazm.zombies.player.upgrade.effect;

import com.github.steanky.element.core.annotation.*;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.tag.Tag;
import net.minestom.server.tag.TagHandler;
import org.jetbrains.annotations.NotNull;
import org.phantazm.commons.InjectionStore;
import org.phantazm.core.Interval;
import org.phantazm.core.TagUtils;
import org.phantazm.zombies.ZombiesTagUtils;
import org.phantazm.zombies.event.trait.AttributeEvent;
import org.phantazm.zombies.player.ZombiesPlayer;
import org.phantazm.zombies.player.upgrade.PlayerUpgrade;
import org.phantazm.zombies.player.upgrade.selector.Selector;
import org.phantazm.zombies.player.upgrade.selector.SelectorComponent;
import org.phantazm.zombies.player.upgrade.trigger.TriggerData;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Model("zombies.upgrade.effect.link_attribute_to_tag")
@Cache
public class LinkAttributeToTagEffect implements UpgradeEffectComponent {
    private final Data data;
    private final SelectorComponent selectorComponent;

    @FactoryMethod
    public LinkAttributeToTagEffect(@NotNull Data data, @NotNull @Child("selector") SelectorComponent selectorComponent) {
        this.data = data;
        this.selectorComponent = selectorComponent;
    }

    @Override
    public @NotNull UpgradeEffect apply(@NotNull InjectionStore injectionStore, @NotNull ZombiesPlayer zombiesPlayer) {
        return new Internal(data, selectorComponent.apply(injectionStore, zombiesPlayer));
    }

    private static final class Internal extends SingleEventEffect<AttributeEvent> {
        private final Selector selector;
        private final Tag<Boolean> tag;
        private final Tag<UUID> uuidTag;

        private final Map<UUID, Reference<LivingEntity>> targets;
        private final Interval interval;

        private Internal(Data data, Selector selector) {
            super(AttributeEvent.class);
            this.selector = selector;
            this.tag = Tag.Boolean(data.tag).defaultValue(false);
            this.uuidTag = Tag.UUID(TagUtils.uniqueTagName());

            this.targets = new ConcurrentHashMap<>();
            this.interval = Interval.of(20);
        }

        @Override
        public void applyEvent(@NotNull PlayerUpgrade upgrade, @NotNull ZombiesPlayer zombiesPlayer,
            @NotNull TriggerData triggerData, @NotNull AttributeEvent attributeEvent) {
            selector.select(upgrade, zombiesPlayer, triggerData).forType(LivingEntity.class, livingEntity -> {
                TagHandler handler = ZombiesTagUtils.sceneLocalTags(zombiesPlayer.getScene(), livingEntity);

                if (!attributeEvent.isRemove()) {
                    handler.setTag(tag, true);
                    handler.setTag(uuidTag, attributeEvent.attributeUuid());

                    targets.putIfAbsent(livingEntity.getUuid(), new WeakReference<>(livingEntity));
                    return;
                }

                if (Objects.equals(handler.getTag(uuidTag), attributeEvent.attributeUuid())) {
                    handler.removeTag(tag);
                    handler.removeTag(uuidTag);
                }
            });
        }

        @Override
        public boolean needsTicking() {
            return true;
        }

        @Override
        public void tick() {
            if (interval.advance()) {
                targets.values().removeIf(playerReference -> playerReference.refersTo(null));
            }
        }

        @Override
        public void clear(@NotNull PlayerUpgrade upgrade, @NotNull ZombiesPlayer zombiesPlayer) {
            targets.values().removeIf(playerReference -> {
                LivingEntity entity = playerReference.get();
                if (entity == null) {
                    return true;
                }

                entity.removeTag(tag);
                entity.removeTag(uuidTag);
                return true;
            });
        }
    }

    @DataObject
    public record Data(@NotNull String tag) {

    }
}
