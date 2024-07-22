package org.phantazm.zombies.event.entity;

import net.minestom.server.attribute.Attribute;
import net.minestom.server.attribute.AttributeModifier;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.event.trait.EntityInstanceEvent;
import org.jetbrains.annotations.NotNull;
import org.phantazm.zombies.event.trait.AttributeEvent;

import java.util.Objects;
import java.util.UUID;

public class EntityAttributeModifierRemoveEvent implements EntityInstanceEvent, AttributeEvent {
    private final LivingEntity target;
    private final Attribute attribute;
    private final AttributeModifier removedModifier;

    public EntityAttributeModifierRemoveEvent(
        @NotNull LivingEntity target,
        @NotNull Attribute attribute,
        @NotNull AttributeModifier removedModifier) {
        this.target = Objects.requireNonNull(target);
        this.attribute = Objects.requireNonNull(attribute);
        this.removedModifier = Objects.requireNonNull(removedModifier);
    }

    @Override
    public @NotNull UUID attributeUuid() {
        return removedModifier.getId();
    }

    public @NotNull Attribute attribute() {
        return attribute;
    }

    @Override
    public float attributeAmount() {
        return (float) removedModifier.getAmount();
    }

    @Override
    public boolean isRemove() {
        return true;
    }

    @Override
    public @NotNull LivingEntity getEntity() {
        return target;
    }

    @Override
    public @NotNull LivingEntity target() {
        return target;
    }
}
