package org.phantazm.zombies.equipment.gun.effect;

import com.github.steanky.element.core.annotation.*;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.TitlePart;
import org.jetbrains.annotations.NotNull;
import org.phantazm.commons.chat.MessageWithDestination;
import org.phantazm.zombies.equipment.gun.GunState;
import org.phantazm.zombies.equipment.gun.audience.AudienceProvider;

import java.util.Objects;

@Model("zombies.gun.effect.alert_no_ammo")
@Cache(false)
public class AlertNoAmmoEffect implements GunEffect {

    private final Data data;

    private final AudienceProvider audienceProvider;

    private boolean hadNoAmmo = false;

    @FactoryMethod
    public AlertNoAmmoEffect(@NotNull Data data,
        @NotNull @Child("audience_provider") AudienceProvider audienceProvider) {
        this.data = Objects.requireNonNull(data);
        this.audienceProvider = Objects.requireNonNull(audienceProvider);
    }

    @Override
    public void apply(@NotNull GunState state) {
        if (state.isMainEquipment()) {
            if (state.ammo() == 0) {
                displayMessage(data.message.component());
                hadNoAmmo = true;
            }
        } else {
            displayMessage(Component.empty());
            hadNoAmmo = false;
        }
    }

    private void displayMessage(Component component) {
        audienceProvider.provideAudience().ifPresent(audience -> {
            switch (data.message().destination()) {
                case TITLE -> audience.sendTitlePart(TitlePart.TITLE, component);
                case SUBTITLE -> audience.sendTitlePart(TitlePart.SUBTITLE, component);
                case ACTION_BAR -> audience.sendActionBar(component);
            }
        });
    }

    @Override
    public void tick(@NotNull GunState state, long time) {
        if (state.isMainEquipment() && hadNoAmmo && state.ammo() > 0) {
            displayMessage(Component.empty());
            hadNoAmmo = false;
        }
    }

    @DataObject
    public record Data(@NotNull @ChildPath("audience_provider") String audienceProvider,
        @NotNull MessageWithDestination message) {
    }

}
