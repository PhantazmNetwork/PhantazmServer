package org.phantazm.zombies.equipment.gun.effect;

import com.github.steanky.element.core.annotation.Cache;
import com.github.steanky.element.core.annotation.DataObject;
import com.github.steanky.element.core.annotation.FactoryMethod;
import com.github.steanky.element.core.annotation.Model;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import org.jetbrains.annotations.NotNull;
import org.phantazm.core.player.PlayerView;
import org.phantazm.zombies.equipment.gun.GunState;

@Model("zombies.gun.effect.recoil")
@Cache(false)
public class RecoilEffect implements GunEffect {
    private final Data data;
    private final PlayerView playerView;

    @FactoryMethod
    public RecoilEffect(@NotNull Data data, @NotNull PlayerView playerView) {
        this.data = data;
        this.playerView = playerView;
    }

    @Override
    public void apply(@NotNull GunState state) {
        playerView.getPlayer().ifPresent(player -> {
            Pos position = player.getPosition();

            Vec lookDirection = position.direction();
            Vec knockbackDirection = lookDirection.mul(-1);
            Pos direction = position.withDirection(knockbackDirection);

            double rad = Math.toRadians(direction.yaw());
            player.takeKnockback((float)data.knockbackStrength, true, Math.sin(rad), -Math.cos(rad));
        });
    }

    @Override
    public void tick(@NotNull GunState state, long time) {

    }

    @DataObject
    public record Data(double knockbackStrength) {
    }
}
