package org.phantazm.zombies.player.upgrade.effect;

import com.github.steanky.element.core.annotation.*;
import net.minestom.server.attribute.Attribute;
import net.minestom.server.attribute.AttributeModifier;
import net.minestom.server.attribute.AttributeOperation;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.entity.Player;
import net.minestom.server.event.trait.CancellableEvent;
import org.jetbrains.annotations.NotNull;
import org.phantazm.commons.InjectionStore;
import org.phantazm.zombies.Attributes;
import org.phantazm.zombies.event.player.ZombiesPlayerModifyAttributeEvent;
import org.phantazm.zombies.player.ZombiesPlayer;
import org.phantazm.zombies.player.upgrade.PlayerUpgrade;
import org.phantazm.zombies.player.upgrade.selector.Selector;
import org.phantazm.zombies.player.upgrade.selector.SelectorComponent;
import org.phantazm.zombies.player.upgrade.trigger.TriggerData;

import java.util.UUID;

@Model("zombies.upgrade.effect.apply_attribute")
@Cache
public class ApplyAttributeEffect implements UpgradeEffectComponent {
    private final Data data;
    private final SelectorComponent selectorComponent;

    @FactoryMethod
    public ApplyAttributeEffect(@NotNull Data data,
        @NotNull @Child("selector") SelectorComponent selectorComponent) {
        this.data = data;
        this.selectorComponent = selectorComponent;
    }

    @Override
    public @NotNull UpgradeEffect apply(@NotNull InjectionStore injectionStore, @NotNull ZombiesPlayer zombiesPlayer) {
        return new Internal(data, selectorComponent.apply(injectionStore, zombiesPlayer));
    }

    private static final class Internal implements UpgradeEffect {
        private final Data data;
        private final Selector selector;
        private final Attribute attribute;
        private final UUID uuid;
        private final String uuidString;

        private Internal(Data data, Selector selector) {
            this.data = data;
            this.selector = selector;
            this.attribute = Attributes.get(data.attribute);
            this.uuid = UUID.randomUUID();
            this.uuidString = this.uuid.toString();
        }

        @Override
        public void apply(@NotNull PlayerUpgrade upgrade, @NotNull ZombiesPlayer zombiesPlayer,
            @NotNull TriggerData triggerData) {
            Player player = zombiesPlayer.getPlayer().orElse(null);
            selector.select(upgrade, zombiesPlayer, triggerData).forType(LivingEntity.class, entity -> {
                if (player != null) {
                    CancellableEvent event = new ZombiesPlayerModifyAttributeEvent(player, zombiesPlayer, entity,
                        this.attribute, uuid, data.amount);
                    zombiesPlayer.getScene().broadcastEvent(event);
                    if (event.isCancelled()) {
                        return;
                    }
                }

                entity.getAttribute(attribute).addModifier(new AttributeModifier(uuid, uuidString, data.amount,
                    data.operation));
            });
        }
    }

    @DataObject
    public record Data(@NotNull String attribute,
        double amount,
        @NotNull AttributeOperation operation) {

    }
}
