package org.phantazm.zombies.equipment.gun.shoot.handler;

import com.github.steanky.element.core.annotation.*;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.minestom.server.entity.Entity;
import org.jetbrains.annotations.NotNull;
import org.phantazm.zombies.equipment.gun.Gun;
import org.phantazm.zombies.equipment.gun.GunState;
import org.phantazm.zombies.equipment.gun.audience.AudienceProvider;
import org.phantazm.zombies.equipment.gun.shoot.GunHit;
import org.phantazm.zombies.equipment.gun.shoot.GunShot;

import java.util.Collection;
import java.util.Objects;
import java.util.UUID;

/**
 * A {@link ShotHandler} that provides feedback to an {@link Audience}.
 */
@Model("zombies.gun.shot_handler.message")
public class MessageShotHandler implements ShotHandler {

    private final Data data;
    private final AudienceProvider audienceProvider;

    /**
     * Creates a {@link MessageShotHandler}.
     *
     * @param data             The {@link Data} for this {@link MessageShotHandler}
     * @param audienceProvider The {@link AudienceProvider} for this {@link MessageShotHandler}
     */
    @FactoryMethod
    public MessageShotHandler(@NotNull Data data,
            @NotNull @Child("audience_provider") AudienceProvider audienceProvider) {
        this.data = Objects.requireNonNull(data, "data");
        this.audienceProvider = Objects.requireNonNull(audienceProvider, "audienceProvider");
    }

    @Override
    public void handle(@NotNull Gun gun, @NotNull GunState state, @NotNull Entity attacker,
            @NotNull Collection<UUID> previousHits, @NotNull GunShot shot) {
        audienceProvider.provideAudience().ifPresent(audience -> {
            for (GunHit ignored : shot.regularTargets()) {
                audience.sendMessage(data.message());
            }
            for (GunHit ignored : shot.headshotTargets()) {
                audience.sendMessage(data.headshotMessage());
            }
        });
    }

    @Override
    public void tick(@NotNull GunState state, long time) {

    }

    /**
     * Data for a {@link MessageShotHandler}.
     *
     * @param audienceProviderPath A path to the {@link MessageShotHandler}'s {@link AudienceProvider}
     * @param message              The message to send for regular hits
     * @param headshotMessage      The message to send for headshots
     */
    @DataObject
    public record Data(@NotNull @ChildPath("audience_provider") String audienceProviderPath,
                       @NotNull Component message,
                       @NotNull Component headshotMessage) {

        public Data {
            Objects.requireNonNull(audienceProviderPath, "audienceProviderPath");
            Objects.requireNonNull(message, "message");
            Objects.requireNonNull(headshotMessage, "headshotMessage");
        }

    }


}
