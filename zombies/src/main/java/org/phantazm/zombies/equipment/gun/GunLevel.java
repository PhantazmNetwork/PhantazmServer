package org.phantazm.zombies.equipment.gun;

import com.github.steanky.element.core.annotation.*;
import net.kyori.adventure.key.Key;
import net.minestom.server.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;
import org.phantazm.core.equipment.UpgradeNode;
import org.phantazm.zombies.equipment.gun.effect.GunEffect;
import org.phantazm.zombies.equipment.gun.reload.ReloadTester;
import org.phantazm.zombies.equipment.gun.shoot.ShootTester;
import org.phantazm.zombies.equipment.gun.shoot.fire.Firer;
import org.phantazm.zombies.equipment.gun.visual.GunStackMapper;

import java.util.Collection;
import java.util.Objects;
import java.util.Set;

/**
 * An individual gun level of a gun.
 *
 * @param data            The extra {@link Data} for this level
 * @param stats           The gun's {@link GunStats}
 * @param shootTester     The gun's {@link ShootTester}
 * @param reloadTester    The gun's {@link ReloadTester}
 * @param firer           The gun's {@link Firer}
 * @param activateEffects The gun's {@link GunEffect}s that are invoked when the gun level becomes active
 * @param shootEffects    The gun's {@link GunEffect}s that are invoked when the gun is shot
 * @param reloadEffects   The gun's {@link GunEffect}s that are invoked when the gun begins reloading
 * @param tickEffects     The gun's {@link GunEffect}s that are invoked every tick
 * @param noAmmoEffects   The gun's {@link GunEffect}s that are invoked when the gun has no ammo
 * @param gunStackMappers The gun's {@link GunStackMapper}s that produce the visual {@link ItemStack} representation of
 *                        the gun
 */
@Model("zombies.gun.level")
@Cache(false)
public record GunLevel(
    @NotNull Data data,
    @NotNull @Child("stats") GunStats stats,
    @NotNull @Child("shootTester") ShootTester shootTester,
    @NotNull @Child("reloadTester") ReloadTester reloadTester,
    @NotNull @Child("firer") Firer firer,
    @NotNull @Child("activateEffects") Collection<GunEffect> activateEffects,
    @NotNull @Child("shootEffects") Collection<GunEffect> shootEffects,
    @NotNull @Child("reloadEffects") Collection<GunEffect> reloadEffects,
    @NotNull @Child("tickEffects") Collection<GunEffect> tickEffects,
    @NotNull @Child("noAmmoEffects") Collection<GunEffect> noAmmoEffects,
    @NotNull @Child("gunStackMappers") Collection<GunStackMapper> gunStackMappers)
    implements UpgradeNode {

    /**
     * Creates a {@link GunLevel}.
     *
     * @param data            The extra {@link Data} for this level
     * @param stats           The gun's {@link GunStats}
     * @param shootTester     The gun's {@link ShootTester}
     * @param reloadTester    The gun's {@link ReloadTester}
     * @param firer           The gun's {@link Firer}
     * @param activateEffects The gun's {@link GunEffect}s that are invoked when the gun level becomes active
     * @param shootEffects    The gun's {@link GunEffect}s that are invoked when the gun is shot
     * @param reloadEffects   The gun's {@link GunEffect}s that are invoked when the gun begins reloading
     * @param tickEffects     The gun's {@link GunEffect}s that are invoked every tick
     * @param noAmmoEffects   The gun's {@link GunEffect}s that are invoked when the gun has no ammo
     * @param gunStackMappers The gun's {@link GunStackMapper}s that produce the visual {@link ItemStack} representation
     *                        of the gun
     */
    @FactoryMethod
    public GunLevel {
        Objects.requireNonNull(data);
        Objects.requireNonNull(stats);
        Objects.requireNonNull(shootTester);
        Objects.requireNonNull(reloadTester);
        Objects.requireNonNull(firer);
        verifyCollection(activateEffects, "activateEffects");
        verifyCollection(shootEffects, "shootEffects");
        verifyCollection(reloadEffects, "reloadEffects");
        verifyCollection(tickEffects, "tickEffects");
        verifyCollection(noAmmoEffects, "noAmmoEffects");
        verifyCollection(gunStackMappers, "gunStackMappers");
    }

    private static void verifyCollection(@NotNull Collection<?> collection, @NotNull String name) {
        Objects.requireNonNull(collection, name);
        for (Object element : collection) {
            Objects.requireNonNull(element, name + " element");
        }
    }

    @Override
    public @Unmodifiable
    @NotNull Set<Key> upgrades() {
        return data.upgrades();
    }

    @DataObject
    public record Data(
        @NotNull Key key,
        @NotNull ItemStack stack,
        @NotNull Set<Key> upgrades) {
    }

}
