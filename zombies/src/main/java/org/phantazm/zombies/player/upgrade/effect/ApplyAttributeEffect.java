package org.phantazm.zombies.player.upgrade.effect;

import com.github.steanky.element.core.annotation.*;
import com.github.steanky.ethylene.mapper.annotation.Default;
import net.minestom.server.attribute.Attribute;
import net.minestom.server.attribute.AttributeInstance;
import net.minestom.server.attribute.AttributeModifier;
import net.minestom.server.attribute.AttributeOperation;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.entity.Player;
import net.minestom.server.event.trait.CancellableEvent;
import net.minestom.server.tag.Tag;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.phantazm.commons.InjectionStore;
import org.phantazm.zombies.Attributes;
import org.phantazm.zombies.event.player.ZombiesPlayerModifyAttributeEvent;
import org.phantazm.zombies.player.ZombiesPlayer;
import org.phantazm.zombies.player.upgrade.PlayerUpgrade;
import org.phantazm.zombies.player.upgrade.selector.Selector;
import org.phantazm.zombies.player.upgrade.selector.SelectorComponent;
import org.phantazm.zombies.player.upgrade.trigger.TriggerData;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.Deque;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedDeque;

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
        private final Tag<Integer> levelTag;

        private final Deque<Reference<Player>> targetedPlayers;

        private Internal(Data data, Selector selector) {
            this.data = data;
            this.selector = selector;
            this.attribute = Attributes.get(data.attribute);
            this.uuid = UUID.randomUUID();
            this.uuidString = this.uuid.toString();
            this.levelTag = data.levelTag == null ? null : Tag.Integer(data.levelTag).defaultValue(1);

            this.targetedPlayers = new ConcurrentLinkedDeque<>();
        }

        @Override
        public void apply(@NotNull PlayerUpgrade upgrade, @NotNull ZombiesPlayer zombiesPlayer,
            @NotNull TriggerData triggerData) {
            Player player = zombiesPlayer.getPlayer().orElse(null);


            selector.select(upgrade, zombiesPlayer, triggerData).forType(LivingEntity.class, entity -> {
                double amount = levelTag == null ? data.amount : data.amount * entity.getTag(levelTag);
                if (player != null) {
                    CancellableEvent event = new ZombiesPlayerModifyAttributeEvent(player, zombiesPlayer, entity,
                        this.attribute, uuid, amount);
                    zombiesPlayer.getScene().broadcastEvent(event);
                    if (event.isCancelled()) {
                        return;
                    }
                }

                AttributeInstance instance = entity.getAttribute(attribute);
                instance.removeModifier(uuid);
                instance.addModifier(new AttributeModifier(uuid, uuidString, amount, data.operation));

                if (entity instanceof Player playerTarget) {
                    targetedPlayers.add(new WeakReference<>(playerTarget));
                }
            });
        }

        @Override
        public void clear(@NotNull PlayerUpgrade upgrade, @NotNull ZombiesPlayer zombiesPlayer) {
            targetedPlayers.removeIf(reference -> {
                Player player = reference.get();
                if (player == null) {
                    return true;
                }

                player.getAttribute(attribute).removeModifier(uuid);
                return true;
            });
        }

        @Override
        public boolean needsTicking() {
            return true;
        }

        @Override
        public void tick() {
            targetedPlayers.removeIf(playerReference -> playerReference.refersTo(null));
        }
    }

    @Default("""
        {
          levelTag=null
        }
        """)
    @DataObject
    public record Data(@NotNull String attribute,
        double amount,
        @NotNull AttributeOperation operation,
        @Nullable String levelTag) {

    }
}
