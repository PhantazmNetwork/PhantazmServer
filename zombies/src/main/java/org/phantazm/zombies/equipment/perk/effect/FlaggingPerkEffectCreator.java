package org.phantazm.zombies.equipment.perk.effect;

import com.github.steanky.element.core.annotation.Cache;
import com.github.steanky.element.core.annotation.DataObject;
import com.github.steanky.element.core.annotation.FactoryMethod;
import com.github.steanky.element.core.annotation.Model;
import com.github.steanky.element.core.annotation.document.Description;
import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.NotNull;
import org.phantazm.commons.flag.Flaggable;
import org.phantazm.zombies.player.ZombiesPlayer;

import java.util.Objects;

@Description("An effect that sets a flag on the player for the duration that it is active.")
@Model("zombies.perk.effect.flagging")
@Cache(false)
public class FlaggingPerkEffectCreator implements PerkEffectCreator {
    private final Data data;

    @FactoryMethod
    public FlaggingPerkEffectCreator(@NotNull Data data) {
        this.data = Objects.requireNonNull(data);
    }

    @Override
    public @NotNull PerkEffect forPlayer(@NotNull ZombiesPlayer zombiesPlayer) {
        return new Effect(data, zombiesPlayer.flags());
    }

    private static class Effect implements PerkEffect {
        private final Data data;
        private final Flaggable flaggable;

        private Effect(Data data, Flaggable flaggable) {
            this.data = data;
            this.flaggable = flaggable;
        }

        @Override
        public void start() {
            flaggable.setFlag(data.flag);
        }

        @Override
        public void end() {
            flaggable.clearFlag(data.flag);
        }
    }

    @DataObject
    public record Data(@NotNull @Description("The flag to be set on the player to whom this perk belongs") Key flag) {

    }
}
