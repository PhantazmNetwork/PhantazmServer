package org.phantazm.zombies.event.entity;

import net.minestom.server.attribute.Attribute;
import net.minestom.server.attribute.AttributeModifier;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.event.trait.EntityInstanceEvent;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class EntityAttributeWearOffEvent implements EntityInstanceEvent {
    private final LivingEntity target;
    private final Attribute attribute;
    private final AttributeModifier removedModifier;

    public EntityAttributeWearOffEvent(
        @NotNull LivingEntity target,
        @NotNull Attribute attribute,
        @NotNull AttributeModifier removedModifier) {
        this.target = Objects.requireNonNull(target);
        this.attribute = Objects.requireNonNull(attribute);
        this.removedModifier = Objects.requireNonNull(removedModifier);
    }

    @Override
    public @NotNull LivingEntity getEntity() {
        return target;
    }

    public @NotNull Attribute attribute() {
        return attribute;
    }

    public @NotNull AttributeModifier removedModifier() {
        return removedModifier;
    }
}
