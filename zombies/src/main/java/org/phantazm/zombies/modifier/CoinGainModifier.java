package org.phantazm.zombies.modifier;

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
import org.phantazm.commons.DualComponent;
import org.phantazm.commons.InjectionStore;
import org.phantazm.zombies.coin.Transaction;
import org.phantazm.zombies.scene2.ZombiesScene;

@Model("zombies.modifier.coins")
@Cache
public class CoinGainModifier implements DualComponent<ZombiesScene, Modifier> {
    private final Data data;

    @FactoryMethod
    public CoinGainModifier(@NotNull Data data) {
        this.data = data;
    }

    @Override
    public @NotNull Modifier apply(@NotNull InjectionStore injectionStore, @NotNull ZombiesScene scene) {
        return new Impl(scene, data);
    }

    private record Impl(ZombiesScene scene,
        Data data) implements Modifier {
        @Override
        public void apply() {
            scene.map().objects().module().modifierSource().addModifier(data.group,
                Transaction.modifier(data.modifierName, data.modifierAction, data.amount, data.priority));
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
