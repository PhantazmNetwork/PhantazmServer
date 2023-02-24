package org.phantazm.zombies.equipment.perk.effect;

import com.github.steanky.element.core.annotation.Cache;
import com.github.steanky.element.core.annotation.DataObject;
import com.github.steanky.element.core.annotation.FactoryMethod;
import com.github.steanky.element.core.annotation.Model;
import com.github.steanky.element.core.annotation.document.Description;
import net.minestom.server.attribute.AttributeOperation;
import org.jetbrains.annotations.NotNull;
import org.phantazm.zombies.Attributes;
import org.phantazm.zombies.player.ZombiesPlayer;

import java.util.Objects;

@Description("Modifies the player's fire rate by some factor.")
@Model("zombies.perk.effect.fire_rate")
@Cache(false)
public class FireRatePerkEffectCreator implements PerkEffectCreator {
    private final Data data;

    @FactoryMethod
    public FireRatePerkEffectCreator(@NotNull Data data) {
        this.data = Objects.requireNonNull(data, "data");
    }

    @Override
    public @NotNull PerkEffect forPlayer(@NotNull ZombiesPlayer zombiesPlayer) {
        return new ModifierPerkEffect(Attributes.FIRE_RATE_MULTIPLIER, zombiesPlayer, data.rateModifier,
                AttributeOperation.MULTIPLY_BASE);
    }

    @DataObject
    public record Data(@Description("The factor by which to multiply the player's fire rate.") float rateModifier) {

    }
}
