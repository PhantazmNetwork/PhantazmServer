package org.phantazm.mob2;

import org.phantazm.commons.InjectionStore;

import java.util.function.Function;

public interface SkillComponent extends Function<InjectionStore, Skill> {
    InjectionStore.Key<Mob> MOB_KEY = InjectionStore.key(Mob.class);
}
