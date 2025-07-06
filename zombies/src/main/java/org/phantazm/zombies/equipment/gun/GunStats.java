package org.phantazm.zombies.equipment.gun;

import com.github.steanky.element.core.annotation.Cache;
import com.github.steanky.element.core.annotation.DataObject;
import com.github.steanky.element.core.annotation.FactoryMethod;
import com.github.steanky.element.core.annotation.Model;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.LivingEntity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.phantazm.core.AttributeUtils;
import org.phantazm.zombies.Attributes;
import org.phantazm.zombies.equipment.gun.shoot.handler.ShotHandler;

import java.util.Objects;

/**
 * A gun's generic stats. These stats only pertain to shooting, not aspects such as damage or knockback. These are
 * handled by individual {@link ShotHandler}s.
 */
@Model("zombies.gun.stats")
@Cache
public final class GunStats {
    private final Data data;

    @FactoryMethod
    public GunStats(@NotNull Data data) {
        this.data = Objects.requireNonNull(data);
    }

    public long shootSpeed() {
        return data.shootSpeed();
    }

    public long reloadSpeed(@Nullable Entity shooter) {
        if (!(shooter instanceof LivingEntity livingShooter)) {
            return data.reloadSpeed;
        }

        return Math.round(AttributeUtils.computeWithBase(data.reloadSpeed(),
            livingShooter.getAttribute(Attributes.RELOAD_DELAY)));
    }

    public int maxAmmo(@Nullable Entity shooter) {
        if (!(shooter instanceof LivingEntity livingShooter)) {
            return data.maxAmmo;
        }

        return Math.round(AttributeUtils.computeWithBase(data.maxAmmo,
            livingShooter.getAttribute(Attributes.MAX_AMMO)));
    }

    public int maxClip(@Nullable Entity shooter) {
        if (!(shooter instanceof LivingEntity livingShooter)) {
            return data.maxClip;
        }

        return Math.round(AttributeUtils.computeWithBase(data.maxClip(),
            livingShooter.getAttribute(Attributes.MAX_CLIP)));
    }

    public int shots() {
        return data.shots();
    }

    public long shotInterval() {
        return data.shotInterval();
    }

    /**
     * Underlying data for the {@link GunStats}.
     *
     * @param shootSpeed   The gun's shoot speed
     * @param reloadSpeed  The gun's reload speed
     * @param maxAmmo      The gun's max ammo
     * @param maxClip      The gun's max clip
     * @param shots        The gun's shots per clip
     * @param shotInterval The interval between gun fire
     */
    @DataObject
    public record Data(long shootSpeed,
        long reloadSpeed,
        int maxAmmo,
        int maxClip,
        int shots,
        long shotInterval) {

    }


}
