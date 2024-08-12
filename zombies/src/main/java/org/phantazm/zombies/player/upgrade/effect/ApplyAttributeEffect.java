package org.phantazm.zombies.player.upgrade.effect;

import com.github.steanky.element.core.annotation.*;
import com.github.steanky.ethylene.mapper.annotation.Default;
import net.minestom.server.attribute.Attribute;
import net.minestom.server.attribute.AttributeInstance;
import net.minestom.server.attribute.AttributeModifier;
import net.minestom.server.attribute.AttributeOperation;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.phantazm.commons.InjectionStore;
import org.phantazm.core.Interval;
import org.phantazm.zombies.Attributes;
import org.phantazm.zombies.event.player.ZombiesPlayerModifyAttributeEvent;
import org.phantazm.zombies.player.ZombiesPlayer;
import org.phantazm.zombies.player.upgrade.PlayerUpgrade;
import org.phantazm.zombies.player.upgrade.effect.scaling.Scaling;
import org.phantazm.zombies.player.upgrade.effect.scaling.ScalingComponent;
import org.phantazm.zombies.player.upgrade.selector.Selector;
import org.phantazm.zombies.player.upgrade.selector.SelectorComponent;
import org.phantazm.zombies.player.upgrade.trigger.TriggerData;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Model("zombies.upgrade.effect.apply_attribute")
@Cache
public class ApplyAttributeEffect implements UpgradeEffectComponent {
    private static final Map<String, UUID> NAMED_ATTRIBUTES = new ConcurrentHashMap<>();

    private final Data data;
    private final ScalingComponent scalingComponent;
    private final SelectorComponent selectorComponent;

    private final UUID uuid;

    @FactoryMethod
    public ApplyAttributeEffect(@NotNull Data data,
        @NotNull @Child("scaling") ScalingComponent scalingComponent,
        @NotNull @Child("selector") SelectorComponent selectorComponent) {
        this.data = data;
        this.scalingComponent = scalingComponent;
        this.selectorComponent = selectorComponent;

        this.uuid = data.shareKey == null ? null : NAMED_ATTRIBUTES.computeIfAbsent(data.shareKey, ignored -> UUID.randomUUID());
    }

    @Override
    public @NotNull UpgradeEffect apply(@NotNull InjectionStore injectionStore, @NotNull ZombiesPlayer zombiesPlayer) {
        return new Internal(data, scalingComponent.apply(injectionStore, zombiesPlayer),
            selectorComponent.apply(injectionStore, zombiesPlayer), this.uuid);
    }

    private static final class Internal implements UpgradeEffect {
        private final Data data;
        private final Scaling scaling;
        private final Selector selector;
        private final Attribute attribute;
        private final UUID uuid;
        private final String uuidString;

        private final Map<UUID, Reference<LivingEntity>> targets;
        private final Interval interval;

        private volatile boolean isBroadcasting;

        private Internal(Data data, Scaling scaling, Selector selector, UUID uuid) {
            this.data = data;
            this.scaling = scaling;
            this.selector = selector;
            this.attribute = Attributes.get(data.attribute);
            this.uuid = uuid == null ? UUID.randomUUID() : uuid;
            this.uuidString = this.uuid.toString();

            this.targets = new ConcurrentHashMap<>();
            this.interval = Interval.of(20);
        }

        @Override
        public void apply(@NotNull PlayerUpgrade upgrade, @NotNull ZombiesPlayer zombiesPlayer,
            @NotNull TriggerData triggerData) {
            if (isBroadcasting) {
                return;
            }

            Player player = zombiesPlayer.getPlayer().orElse(null);

            selector.select(upgrade, zombiesPlayer, triggerData).forType(LivingEntity.class, entity -> {
                double amount = data.amount * scaling.getMultiplier(upgrade, zombiesPlayer, triggerData);
                if (player != null) {
                    ZombiesPlayerModifyAttributeEvent event = new ZombiesPlayerModifyAttributeEvent(player,
                        zombiesPlayer, entity, this.attribute, uuid, amount);

                    isBroadcasting = true;
                    zombiesPlayer.getScene().broadcastEvent(event);
                    isBroadcasting = false;

                    if (event.isCancelled()) {
                        return;
                    }

                    amount = event.attributeAmount();
                }

                AttributeInstance instance = entity.getAttribute(attribute);
                instance.removeModifier(uuid);

                // 0 amount modifiers can never do anything
                if (amount != 0) {
                    instance.addModifier(new AttributeModifier(uuid, uuidString, amount, data.operation));
                    targets.put(entity.getUuid(), new WeakReference<>(entity));
                } else {
                    targets.remove(entity.getUuid());
                }
            });
        }

        @Override
        public void clear(@NotNull PlayerUpgrade upgrade, @NotNull ZombiesPlayer zombiesPlayer) {
            targets.values().removeIf(reference -> {
                LivingEntity entity = reference.get();
                if (entity == null) {
                    return true;
                }

                entity.getAttribute(attribute).removeModifier(uuid);
                return true;
            });
        }

        @Override
        public boolean needsTicking() {
            return true;
        }

        @Override
        public void tick() {
            if (interval.advance()) {
                targets.values().removeIf(reference -> reference.refersTo(null));
            }
        }
    }

    @Default("""
        {
          scaling={type='zombies.upgrade.effect.scaling.none'},
          shareKey=null
        }
        """)
    @DataObject
    public record Data(@NotNull String attribute,
        double amount,
        @NotNull AttributeOperation operation,
        @Nullable String shareKey) {

    }
}
