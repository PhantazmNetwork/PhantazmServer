package org.phantazm.zombies.player.upgrade;

import com.github.steanky.element.core.annotation.Cache;
import com.github.steanky.element.core.annotation.DataObject;
import com.github.steanky.element.core.annotation.FactoryMethod;
import com.github.steanky.element.core.annotation.Model;
import com.github.steanky.ethylene.mapper.annotation.Default;
import net.minestom.server.attribute.AttributeInstance;
import net.minestom.server.attribute.AttributeModifier;
import net.minestom.server.attribute.AttributeOperation;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.event.EventListener;
import org.jetbrains.annotations.NotNull;
import org.phantazm.commons.InjectionStore;
import org.phantazm.core.tick.Activable;
import org.phantazm.zombies.Attributes;
import org.phantazm.zombies.event.equipment.EntityDamageByGunEvent;
import org.phantazm.zombies.player.ZombiesPlayer;

import java.util.UUID;

@Model("zombies.upgrade.scaling_headshot_damage")
@Cache
public class ScalingHeadshotDamageUpgrade implements PlayerUpgradeComponent {
    private final Data data;

    @FactoryMethod
    public ScalingHeadshotDamageUpgrade(@NotNull Data data) {
        this.data = data;
    }

    @Override
    public @NotNull PlayerUpgrade apply(@NotNull InjectionStore injectionStore, @NotNull ZombiesPlayer player) {
        return new Internal(player, data);
    }

    private static class Internal extends GuardedPlayerUpgrade {
        private Internal(ZombiesPlayer zombiesPlayer, Data data) {
            super(Activable.threadsafeWrapper(new Activable() {
                private final EventListener<EntityDamageByGunEvent> event = EventListener.builder(EntityDamageByGunEvent.class)
                    .handler(this::handleMobShot).build();
                private final UUID attributeUUID = UUID.randomUUID();
                private final String name = attributeUUID.toString();

                private int currentLevel;

                @Override
                public void start() {
                    zombiesPlayer.getScene().sceneNode().addListener(event);
                }

                @Override
                public void end() {
                    zombiesPlayer.getScene().sceneNode().removeListener(event);
                }

                private void handleMobShot(EntityDamageByGunEvent event) {
                    if (!event.shooter().getUuid().equals(zombiesPlayer.getUUID())) {
                        return;
                    }

                    // cast is safe as we know the shooter is our player
                    LivingEntity shooter = (LivingEntity) event.shooter();

                    if (event.isHeadshot()) {
                        currentLevel = Math.min(currentLevel + data.levelIncrement, data.maxLevel);
                    } else {
                        currentLevel = Math.max(currentLevel - data.levelDecrement, 0);
                    }

                    float actualModifier = (float) (currentLevel * data.levelFactor);

                    AttributeInstance instance = shooter.getAttribute(Attributes.GUN_DAMAGE);
                    instance.removeModifier(attributeUUID);
                    instance.addModifier(new AttributeModifier(attributeUUID, name, actualModifier, data.operation));
                }
            }), zombiesPlayer);
        }

        @Override
        public boolean needsTicking() {
            return false;
        }
    }

    @Default("""
        {
          operation='MULTIPLY_BASE'
        }
        """)
    @DataObject
    public record Data(int maxLevel,
        int levelIncrement,
        int levelDecrement,
        double levelFactor,
        AttributeOperation operation) {

    }
}
