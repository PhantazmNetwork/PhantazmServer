package org.phantazm.zombies.event.trait;

import net.minestom.server.attribute.Attribute;
import net.minestom.server.event.Event;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public interface AttributeEvent extends Event, LivingTargetEvent {
    @NotNull UUID attributeUuid();

    @NotNull Attribute attribute();

    double attributeAmount();

    boolean isRemove();
}
