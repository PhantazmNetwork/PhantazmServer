package org.phantazm.zombies.event.mob;

import net.minestom.server.attribute.Attribute;
import net.minestom.server.attribute.AttributeModifier;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.event.Event;
import org.jetbrains.annotations.NotNull;
import org.phantazm.zombies.equipment.perk.effect.shot.ShotEffect;

import java.util.Objects;

public class MobAttributeWearOffEvent implements Event {
    private final ShotEffect cause;
    private final LivingEntity target;
    private final Attribute attribute;
    private final AttributeModifier removedModifier;

    public MobAttributeWearOffEvent(
        @NotNull ShotEffect cause,
        @NotNull LivingEntity target,
        @NotNull Attribute attribute,
        @NotNull AttributeModifier removedModifier) {
        this.cause = Objects.requireNonNull(cause);
        this.target = Objects.requireNonNull(target);
        this.attribute = Objects.requireNonNull(attribute);
        this.removedModifier = Objects.requireNonNull(removedModifier);
    }

    public @NotNull ShotEffect cause() {
        return cause;
    }

    public @NotNull LivingEntity target() {
        return target;
    }

    public @NotNull Attribute attribute() {
        return attribute;
    }

    public @NotNull AttributeModifier removedModifier() {
        return removedModifier;
    }
}
