package org.phantazm.zombies.map.shop.interactor;

import com.github.steanky.element.core.annotation.*;
import com.github.steanky.vector.Vec3D;
import com.github.steanky.vector.Vec3I;
import net.kyori.adventure.sound.Sound;
import net.minestom.server.coordinate.Point;
import net.minestom.server.entity.damage.Damage;
import net.minestom.server.instance.EntityTracker;
import org.jetbrains.annotations.NotNull;
import org.phantazm.core.VecUtils;
import org.phantazm.core.particle.ParticleWrapper;
import org.phantazm.zombies.map.shop.PlayerInteraction;
import org.phantazm.zombies.scene2.ZombiesScene;

import java.util.Random;
import java.util.Set;
import java.util.function.Supplier;

@Model("zombies.map.shop.interactor.mortar_fire")
@Cache(false)
public class MortarFireInteractor implements ShopInteractor {
    private final Data data;
    private final ParticleWrapper particleWrapper;
    private final Supplier<ZombiesScene> zombiesScene;
    private final Random random;

    @FactoryMethod
    public MortarFireInteractor(@NotNull Data data, @Child("particle") ParticleWrapper particleWrapper,
        @NotNull Supplier<ZombiesScene> zombiesScene, @NotNull Random random) {
        this.data = data;
        this.particleWrapper = particleWrapper;
        this.zombiesScene = zombiesScene;
        this.random = random;
    }

    private Sound randomize(Sound sound) {
        return Sound.sound(sound).seed(random.nextLong()).build();
    }

    @Override
    public boolean handleInteraction(@NotNull PlayerInteraction interaction) {
        interaction.player().getPlayer().ifPresent(player -> {
            ZombiesScene scene = zombiesScene.get();
            Vec3I origin = scene.mapSettingsInfo().origin();
            scene.map().roundHandler().currentRound().ifPresent(round -> {
                for (Vec3D location : data.locations) {
                    Vec3D actualLocation = location.add(origin.x(), origin.y(), origin.z());

                    Point point = VecUtils.toPoint(actualLocation);
                    scene.instance().getEntityTracker().nearbyEntities(point, data.radius,
                        EntityTracker.Target.LIVING_ENTITIES, entity -> {
                            if (round.hasMob(entity.getUuid())) {
                                entity.setLastDamageSource(Damage.fromPlayer(player, entity.getHealth()));
                                entity.kill();
                            }
                        });

                    particleWrapper.sendTo(scene.instance(), point);
                    scene.instance().playSound(randomize(data.sound), point);
                }
            });
        });

        return true;
    }

    @DataObject
    public record Data(@NotNull Set<Vec3D> locations,
        @NotNull Sound sound,
        double radius) {
    }
}
