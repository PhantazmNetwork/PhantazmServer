package org.phantazm.mob2.condition;

import org.jetbrains.annotations.NotNull;
import org.phantazm.commons.ExtensionHolder;

import java.util.function.Function;

public interface SkillConditionComponent extends Function<@NotNull ExtensionHolder, @NotNull SkillCondition> {
}
