package org.phantazm.mob.target;

import com.github.steanky.element.core.annotation.Cache;
import com.github.steanky.element.core.annotation.FactoryMethod;
import com.github.steanky.element.core.annotation.Model;
import net.minestom.server.entity.LivingEntity;

@Model("mob.selector.last_hit_entity")
@Cache(false)
public class LastHitEntitySelector extends LastHitSelector<LivingEntity> {
    @FactoryMethod
    public LastHitEntitySelector() {
    }
}
