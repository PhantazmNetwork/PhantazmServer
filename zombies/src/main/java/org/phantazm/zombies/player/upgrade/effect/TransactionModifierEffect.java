package org.phantazm.zombies.player.upgrade.effect;

import com.github.steanky.element.core.annotation.Cache;
import com.github.steanky.element.core.annotation.DataObject;
import com.github.steanky.element.core.annotation.FactoryMethod;
import com.github.steanky.element.core.annotation.Model;
import com.github.steanky.ethylene.mapper.annotation.Default;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;
import org.phantazm.commons.InjectionStore;
import org.phantazm.zombies.coin.Transaction;
import org.phantazm.zombies.player.ZombiesPlayer;
import org.phantazm.zombies.player.upgrade.PlayerUpgrade;
import org.phantazm.zombies.player.upgrade.trigger.TriggerData;

@Model("zombies.upgrade.effect.transaction_modifier")
@Cache
public class TransactionModifierEffect implements UpgradeEffectComponent {
    private final Internal instance;

    @FactoryMethod
    public TransactionModifierEffect(@NotNull Data data) {
        this.instance = new Internal(data.modifierGroup,
            Transaction.modifier(data.displayName, data.modifierAction, data.amount, data.priority));
    }

    @Override
    public @NotNull UpgradeEffect apply(@NotNull InjectionStore injectionStore, @NotNull ZombiesPlayer zombiesPlayer) {
        return instance;
    }

    private static final class Internal implements UpgradeEffect {
        private final Key group;
        private final Transaction.Modifier modifier;

        private Internal(Key group, Transaction.Modifier modifier) {
            this.group = group;
            this.modifier = modifier;
        }

        @Override
        public void apply(@NotNull PlayerUpgrade upgrade, @NotNull ZombiesPlayer zombiesPlayer, @NotNull TriggerData triggerData) {
            zombiesPlayer.module().playerTransactionModifiers().addModifier(group, modifier);
        }

        @Override
        public void clear(@NotNull PlayerUpgrade upgrade, @NotNull ZombiesPlayer zombiesPlayer) {
            zombiesPlayer.module().playerTransactionModifiers().removeModifier(group, modifier);
        }
    }

    @Default("""
        {
          displayName='',
          priority=0,
        }
        """)
    @DataObject
    public record Data(
        @NotNull Key modifierGroup,
        @NotNull Component displayName,
        @NotNull Transaction.Modifier.Action modifierAction,
        double amount,
        int priority) {
    }
}
