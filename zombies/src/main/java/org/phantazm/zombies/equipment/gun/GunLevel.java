package org.phantazm.zombies.equipment.gun;

import com.github.steanky.element.core.annotation.*;
import net.kyori.adventure.key.Key;
import net.minestom.server.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;
import org.phantazm.zombies.equipment.gun.effect.GunEffect;
import org.phantazm.zombies.equipment.gun.reload.ReloadTester;
import org.phantazm.zombies.equipment.gun.shoot.ShootTester;
import org.phantazm.zombies.equipment.gun.shoot.fire.Firer;
import org.phantazm.zombies.equipment.gun.visual.GunStackMapper;
import org.phantazm.zombies.upgrade.UpgradeNode;

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
 * @param gunStackMappers The gun's {@link GunStackMapper}s that produce the visual {@link ItemStack} representation of the gun
 */
@Model("zombies.gun.level")
public record GunLevel(@NotNull Data data,
                       @NotNull @Child("stats") GunStats stats,
                       @NotNull @Child("shoot_tester") ShootTester shootTester,
                       @NotNull @Child("reload_tester") ReloadTester reloadTester,
                       @NotNull @Child("firer") Firer firer,
                       @NotNull @Child("activate_effects") Collection<GunEffect> activateEffects,
                       @NotNull @Child("shoot_effects") Collection<GunEffect> shootEffects,
                       @NotNull @Child("reload_effects") Collection<GunEffect> reloadEffects,
                       @NotNull @Child("tick_effects") Collection<GunEffect> tickEffects,
                       @NotNull @Child("no_ammo_effects") Collection<GunEffect> noAmmoEffects,
                       @NotNull @Child("gun_stack_mappers") Collection<GunStackMapper> gunStackMappers)
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
     * @param gunStackMappers The gun's {@link GunStackMapper}s that produce the visual {@link ItemStack} representation of the gun
     */
    @FactoryMethod
    public GunLevel {
        Objects.requireNonNull(data, "data");
        Objects.requireNonNull(stats, "stats");
        Objects.requireNonNull(shootTester, "shootTester");
        Objects.requireNonNull(reloadTester, "reloadTester");
        Objects.requireNonNull(firer, "firer");
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
    public @Unmodifiable @NotNull Set<Key> upgrades() {
        return data.upgrades();
    }

    @DataObject
    public record Data(@NotNull Key key,
                       @NotNull @ChildPath("stats") String statsPath,
                       @NotNull @ChildPath("shoot_tester") String shootTesterPath,
                       @NotNull @ChildPath("reload_tester") String reloadTesterPath,
                       @NotNull @ChildPath("firer") String firerPath,
                       @NotNull @ChildPath("activate_effects") Collection<String> activateEffectPaths,
                       @NotNull @ChildPath("shoot_effects") Collection<String> shootEffectPaths,
                       @NotNull @ChildPath("reload_effects") Collection<String> reloadEffectPaths,
                       @NotNull @ChildPath("tick_effects") Collection<String> tickEffectPaths,
                       @NotNull @ChildPath("no_ammo_effects") Collection<String> noAmmoEffectPaths,
                       @NotNull @ChildPath("gun_stack_mappers") Collection<String> gunStackMapperPaths,
                       @NotNull ItemStack stack,
                       @NotNull Set<Key> upgrades) {

        public Data {
            Objects.requireNonNull(key, "key");
            Objects.requireNonNull(statsPath, "statsPath");
            Objects.requireNonNull(shootTesterPath, "shootTesterPath");
            Objects.requireNonNull(reloadTesterPath, "reloadTesterPath");
            Objects.requireNonNull(firerPath, "firerPath");
            verifyCollection(activateEffectPaths, "activateEffectPaths");
            verifyCollection(shootEffectPaths, "shootEffectPaths");
            verifyCollection(reloadEffectPaths, "reloadEffectPaths");
            verifyCollection(tickEffectPaths, "tickEffectPaths");
            verifyCollection(noAmmoEffectPaths, "noAmmoEffectPaths");
            verifyCollection(gunStackMapperPaths, "gunStackMapperPaths");
            Objects.requireNonNull(stack, "stack");
            verifyCollection(upgrades, "upgrades");
        }

    }

}
