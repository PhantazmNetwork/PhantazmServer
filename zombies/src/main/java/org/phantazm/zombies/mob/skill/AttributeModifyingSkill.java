package org.phantazm.zombies.mob.skill;

import com.github.steanky.element.core.annotation.*;
import net.minestom.server.attribute.Attribute;
import net.minestom.server.attribute.AttributeModifier;
import net.minestom.server.attribute.AttributeOperation;
import net.minestom.server.entity.LivingEntity;
import org.jetbrains.annotations.NotNull;
import org.phantazm.mob.PhantazmMob;
import org.phantazm.mob.skill.Skill;
import org.phantazm.mob.target.TargetSelector;
import org.phantazm.zombies.Attributes;
import org.phantazm.zombies.map.objects.MapObjects;
import org.phantazm.zombies.player.ZombiesPlayer;

import java.util.*;

@Model("mob.skill.attribute_modifying")
@Cache(false)
public class AttributeModifyingSkill implements Skill {
    private final Data data;
    private final UUID uuid;
    private final String uuidString;
    private final Attribute attribute;
    private final TargetSelector<?> targetSelector;

    private final Map<? super UUID, ? extends ZombiesPlayer> playerMap;

    private final Set<LivingEntity> affectedEntities;

    @FactoryMethod
    public AttributeModifyingSkill(@NotNull Data data, @NotNull @Child("selector") TargetSelector<?> targetSelector,
        @NotNull MapObjects mapObjects) {
        this.data = data;
        this.uuid = UUID.randomUUID();
        this.uuidString = this.uuid.toString();
        this.attribute = Objects.requireNonNullElse(Attribute.fromKey(data.attribute), Attributes.NIL);
        this.targetSelector = targetSelector;
        this.playerMap = mapObjects.module().playerMap();
        this.affectedEntities = Collections.newSetFromMap(new WeakHashMap<>());
    }

    @Override
    public void use(@NotNull PhantazmMob self) {
        Optional<?> targetOptional = targetSelector.selectTarget(self);
        if (targetOptional.isEmpty()) {
            return;
        }

        Object target = targetOptional.get();
        if (target instanceof LivingEntity entity) {
            applyToEntity(entity);
        } else if (target instanceof Iterable<?> iterable) {
            for (Object object : iterable) {
                if (object instanceof LivingEntity livingEntity) {
                    applyToEntity(livingEntity);
                }
            }
        }
    }

    private void applyToEntity(LivingEntity entity) {
        ZombiesPlayer zombiesPlayer = playerMap.get(entity.getUuid());

        if (zombiesPlayer != null) {
            zombiesPlayer.cancellableAttribute(attribute,
                new AttributeModifier(uuid, uuidString, data.amount, data.attributeOperation));
            affectedEntities.add(entity);
            return;
        }

        entity.getAttribute(attribute)
            .addModifier(new AttributeModifier(uuid, uuidString, data.amount, data.attributeOperation));
        affectedEntities.add(entity);
    }

    private void removeFromEntity(LivingEntity livingEntity) {
        ZombiesPlayer zombiesPlayer = playerMap.get(livingEntity.getUuid());

        if (zombiesPlayer != null) {
            zombiesPlayer.removeCancellable(uuid);
            return;
        }

        livingEntity.getAttribute(attribute).removeModifier(uuid);
    }

    @Override
    public void end(@NotNull PhantazmMob self) {
        for (LivingEntity entity : affectedEntities) {
            removeFromEntity(entity);
        }

        affectedEntities.clear();
    }

    @DataObject
    public record Data(
        @NotNull String attribute,
        float amount,
        @NotNull AttributeOperation attributeOperation,
        @NotNull @ChildPath("selector") String selector) {
    }
}
