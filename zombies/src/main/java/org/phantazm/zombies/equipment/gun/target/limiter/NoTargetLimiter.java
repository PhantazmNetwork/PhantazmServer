package org.phantazm.zombies.equipment.gun.target.limiter;

import com.github.steanky.element.core.annotation.Cache;
import com.github.steanky.element.core.annotation.FactoryMethod;
import com.github.steanky.element.core.annotation.Model;
import it.unimi.dsi.fastutil.Pair;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.LivingEntity;
import org.jetbrains.annotations.NotNull;

import java.util.List;

@Model("zombies.gun.target_limiter.none")
@Cache
public class NoTargetLimiter implements TargetLimiter {
    @FactoryMethod
    public NoTargetLimiter() {
    }

    @Override
    public @NotNull List<Pair<? extends LivingEntity, Vec>> limitTargets(@NotNull Pos start,
            @NotNull List<Pair<? extends LivingEntity, Vec>> targets) {
        return targets;
    }
}
