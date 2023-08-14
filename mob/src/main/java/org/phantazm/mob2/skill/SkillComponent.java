package org.phantazm.mob2.skill;

import org.jetbrains.annotations.NotNull;
import org.phantazm.commons.InjectionStore;

import java.util.function.Function;

public interface SkillComponent extends Function<@NotNull InjectionStore, @NotNull Skill> {

}
