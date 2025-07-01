package org.phantazm.mob2.selector;

import com.github.steanky.element.core.annotation.Cache;
import com.github.steanky.element.core.annotation.FactoryMethod;
import com.github.steanky.element.core.annotation.Model;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.damage.Damage;
import org.jetbrains.annotations.NotNull;
import org.phantazm.core.Target;
import org.phantazm.mob2.Mob;

@Model("mob.selector.last_attacker")
@Cache
public class LastAttackerSelector implements SelectorComponent {
    private static final Selector INSTANCE = new Selector() {
        @Override
        public @NotNull Target select(@NotNull Mob mob) {
            Damage damage = mob.getLastDamageSource();
            if (damage == null) return Target.NONE;

            Entity attacker = damage.getAttacker();
            if (attacker == null) return Target.NONE;

            return Target.entities(attacker);
        }
    };

    @FactoryMethod
    public LastAttackerSelector() {
    }

    @Override
    public @NotNull Selector get() {
        return INSTANCE;
    }
}
