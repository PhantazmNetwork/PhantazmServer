package org.phantazm.zombies.equipment.perk.effect;

import com.github.steanky.element.core.annotation.Cache;
import com.github.steanky.element.core.annotation.DataObject;
import com.github.steanky.element.core.annotation.FactoryMethod;
import com.github.steanky.element.core.annotation.Model;
import com.github.steanky.ethylene.core.ConfigElement;
import com.github.steanky.ethylene.core.ConfigPrimitive;
import com.github.steanky.ethylene.mapper.annotation.Default;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;
import org.phantazm.commons.InjectionStore;
import org.phantazm.zombies.coin.Transaction;
import org.phantazm.zombies.player.ZombiesPlayer;

@Model("zombies.perk.effect.transaction_modifier")
@Cache(false)
public class TransactionModifierPerkEffectCreator implements PerkEffectCreator {
    private final Data data;

    @FactoryMethod
    public TransactionModifierPerkEffectCreator(@NotNull Data data) {
        this.data = data;
    }

    @Override
    public @NotNull PerkEffect forPlayer(@NotNull ZombiesPlayer zombiesPlayer, @NotNull InjectionStore injectionStore) {
        return new Effect(data, zombiesPlayer);
    }

    private static class Effect implements PerkEffect {
        private final Data data;
        private final ZombiesPlayer zombiesPlayer;
        private final Transaction.Modifier modifier;

        private Effect(Data data, ZombiesPlayer zombiesPlayer) {
            this.data = data;
            this.zombiesPlayer = zombiesPlayer;
            this.modifier = Transaction.modifier(data.modifierName, data.modifierAction, data.amount, data.priority);
        }

        @Override
        public void start() {
            zombiesPlayer.module().playerTransactionModifiers().addModifier(data.group, modifier);
        }

        @Override
        public void end() {
            zombiesPlayer.module().playerTransactionModifiers().removeModifier(data.group, modifier);
        }
    }

    @DataObject
    public record Data(
        @NotNull Key group,
        @NotNull Component modifierName,
        @NotNull Transaction.Modifier.Action modifierAction,
        double amount,
        int priority) {
        @Default("priority")
        public static @NotNull ConfigElement priorityDefault() {
            return ConfigPrimitive.of(0);
        }
    }
}
