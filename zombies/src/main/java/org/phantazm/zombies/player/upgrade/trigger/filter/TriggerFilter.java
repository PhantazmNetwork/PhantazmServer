package org.phantazm.zombies.player.upgrade.trigger.filter;

import org.phantazm.zombies.player.upgrade.trigger.TriggerData;

import java.util.function.Predicate;

public interface TriggerFilter extends Predicate<TriggerData> {
    TriggerFilter NONE = data -> false;
    TriggerFilter ALL = data -> true;
}
