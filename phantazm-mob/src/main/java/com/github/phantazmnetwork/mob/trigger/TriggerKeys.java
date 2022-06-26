package com.github.phantazmnetwork.mob.trigger;

import net.kyori.adventure.key.Key;
import net.minestom.server.event.entity.EntityDamageEvent;
import net.minestom.server.event.trait.EntityEvent;

import java.util.Map;

public class TriggerKeys {

    public final static Key DAMAGE_TRIGGER = Key.key("phantazm", "trigger_damage");

    public final static Map<Key, Class<? extends EntityEvent>> TRIGGER_EVENT_MAP = Map.of(
            DAMAGE_TRIGGER, EntityDamageEvent.class
    );

}
