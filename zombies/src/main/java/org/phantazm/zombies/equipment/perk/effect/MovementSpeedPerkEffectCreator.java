package org.phantazm.zombies.equipment.perk.effect;

import com.github.steanky.element.core.annotation.Cache;
import com.github.steanky.element.core.annotation.DataObject;
import com.github.steanky.element.core.annotation.FactoryMethod;
import com.github.steanky.element.core.annotation.Model;
import com.github.steanky.element.core.annotation.document.Description;
import net.minestom.server.attribute.Attribute;
import net.minestom.server.attribute.AttributeOperation;
import org.jetbrains.annotations.NotNull;
import org.phantazm.zombies.player.ZombiesPlayer;

import java.util.Objects;

@Description("Modifies the player's movement speed by some factor.")
@Model("zombies.perk.effect.movement_speed")
@Cache(false)
public class MovementSpeedPerkEffectCreator implements PerkEffectCreator {
    private final Data data;

    @FactoryMethod
    public MovementSpeedPerkEffectCreator(@NotNull Data data) {
        this.data = Objects.requireNonNull(data, "data");
    }

    @Override
    public @NotNull PerkEffect forPlayer(@NotNull ZombiesPlayer zombiesPlayer) {
        return new ModifierPerkEffect(Attribute.MOVEMENT_SPEED, zombiesPlayer, data.speedModifier,
                AttributeOperation.MULTIPLY_BASE);
    }

    @DataObject
    public record Data(@Description("The factor by which to multiply the player's speed.") float speedModifier) {

    }
}
