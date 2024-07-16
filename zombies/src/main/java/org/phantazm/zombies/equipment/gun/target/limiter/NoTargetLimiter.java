package org.phantazm.zombies.equipment.gun.target.limiter;

import com.github.steanky.element.core.annotation.Cache;
import com.github.steanky.element.core.annotation.FactoryMethod;
import com.github.steanky.element.core.annotation.Model;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Entity;
import org.jetbrains.annotations.NotNull;
import org.phantazm.zombies.equipment.gun.Gun;
import org.phantazm.zombies.equipment.gun.shoot.GunHit;

import java.util.List;

@Model("zombies.gun.target_limiter.none")
@Cache
public class NoTargetLimiter implements TargetLimiter {
    @FactoryMethod
    public NoTargetLimiter() {
    }

    @Override
    public @NotNull List<GunHit> limitTargets(@NotNull Entity shooter, @NotNull Gun gun, @NotNull Pos start,
        @NotNull List<GunHit> targets) {
        return targets;
    }
}
