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

@Description("Adds some health to the player.")
@Model("zombies.perk.effect.health")
@Cache(false)
public class ExtraHealthPerkEffectCreator implements PerkEffectCreator {
    private final Data data;

    @FactoryMethod
    public ExtraHealthPerkEffectCreator(@NotNull Data data) {
        this.data = data;
    }

    @Override
    public @NotNull PerkEffect forPlayer(@NotNull ZombiesPlayer zombiesPlayer) {
        return new ModifierPerkEffect(Attribute.MAX_HEALTH, zombiesPlayer, data.additionalHealth,
                AttributeOperation.ADDITION);
    }

    @DataObject
    public record Data(@Description("The amount to add to the player's base health.") float additionalHealth) {

    }
}
