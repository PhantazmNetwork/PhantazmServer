package org.phantazm.zombies.player.upgrade.effect;

import com.github.steanky.element.core.annotation.*;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.tag.Tag;
import org.jetbrains.annotations.NotNull;
import org.phantazm.commons.InjectionStore;
import org.phantazm.core.TagUtils;
import org.phantazm.zombies.event.trait.AttributeEvent;
import org.phantazm.zombies.player.ZombiesPlayer;
import org.phantazm.zombies.player.upgrade.PlayerUpgrade;
import org.phantazm.zombies.player.upgrade.selector.Selector;
import org.phantazm.zombies.player.upgrade.selector.SelectorComponent;
import org.phantazm.zombies.player.upgrade.trigger.TriggerData;

import java.util.Objects;
import java.util.UUID;

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

        private Internal(Data data, Selector selector) {
            super(AttributeEvent.class);
            this.selector = selector;
            this.tag = Tag.Boolean(data.tag).defaultValue(false);
            this.uuidTag = Tag.UUID(TagUtils.uniqueTagName());
        }

        @Override
        public void applyEvent(@NotNull PlayerUpgrade upgrade, @NotNull ZombiesPlayer zombiesPlayer,
            @NotNull TriggerData triggerData, @NotNull AttributeEvent attributeEvent) {
            selector.select(upgrade, zombiesPlayer, triggerData).forType(LivingEntity.class, livingEntity -> {
                if (!attributeEvent.isRemove()) {
                    livingEntity.setTag(tag, true);
                    livingEntity.setTag(uuidTag, attributeEvent.attributeUuid());
                    return;
                }

                if (Objects.equals(livingEntity.getTag(uuidTag), attributeEvent.attributeUuid())) {
                    livingEntity.removeTag(tag);
                    livingEntity.removeTag(uuidTag);
                }
            });
        }
    }

    @DataObject
    public record Data(@NotNull String tag) {

    }
}
