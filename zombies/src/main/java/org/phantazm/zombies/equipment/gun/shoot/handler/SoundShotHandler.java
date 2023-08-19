package org.phantazm.zombies.equipment.gun.shoot.handler;

import com.github.steanky.element.core.annotation.*;
import net.kyori.adventure.sound.Sound;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Entity;
import org.jetbrains.annotations.NotNull;
import org.phantazm.zombies.equipment.gun.Gun;
import org.phantazm.zombies.equipment.gun.GunState;
import org.phantazm.zombies.equipment.gun.audience.AudienceProvider;
import org.phantazm.zombies.equipment.gun.shoot.GunHit;
import org.phantazm.zombies.equipment.gun.shoot.GunShot;

import java.util.*;

/**
 * A {@link ShotHandler} that plays a {@link Sound}.
 */
@Model("zombies.gun.shot_handler.sound")
@Cache(false)
public class SoundShotHandler implements ShotHandler {
    private final Data data;
    private final AudienceProvider audienceProvider;

    /**
     * Creates a {@link SoundShotHandler}.
     *
     * @param data             The {@link SoundShotHandler}'s {@link Data}
     * @param audienceProvider The {@link SoundShotHandler}'s {@link AudienceProvider}
     */
    @FactoryMethod
    public SoundShotHandler(@NotNull Data data,
        @NotNull @Child("audience_provider") AudienceProvider audienceProvider) {
        this.data = Objects.requireNonNull(data);
        this.audienceProvider = Objects.requireNonNull(audienceProvider);
    }

    @Override
    public void handle(@NotNull Gun gun, @NotNull GunState state, @NotNull Entity attacker,
        @NotNull Collection<UUID> previousHits, @NotNull GunShot shot) {
        audienceProvider.provideAudience().ifPresent(audience -> {
            Set<UUID> played = Collections.newSetFromMap(new IdentityHashMap<>(shot.regularTargets().size()));
            for (GunHit hit : shot.regularTargets()) {
                if (played.add(hit.entity().getUuid())) {
                    Pos pos = data.atShooter ? attacker.getPosition():hit.entity().getPosition();
                    audience.playSound(data.sound(), pos.x(), pos.y(), pos.z());
                }
            }

            played.clear();
            for (GunHit hit : shot.headshotTargets()) {
                if (played.add(hit.entity().getUuid())) {
                    Pos pos = data.atShooter ? attacker.getPosition():hit.entity().getPosition();
                    audience.playSound(data.headshotSound(), pos.x(), pos.y(), pos.z());
                }
            }
        });
    }

    @Override
    public void tick(@NotNull GunState state, long time) {

    }

    /**
     * Data for a {@link SoundShotHandler}.
     *
     * @param audienceProvider A path to the {@link SoundShotHandler}'s {@link AudienceProvider}
     * @param sound            The sound to play for regular targets
     * @param headshotSound    The sound to play for headshots
     */
    @DataObject
    public record Data(@NotNull @ChildPath("audience_provider") String audienceProvider,
        @NotNull Sound sound,
        @NotNull Sound headshotSound,
        boolean atShooter) {
    }
}
