package org.phantazm.zombies.event.trait;

import net.minestom.server.event.Event;

public interface SettableDamageAmountEvent extends Event {
    float damageAmount();

    void setDamageAmount(float damage);
}
