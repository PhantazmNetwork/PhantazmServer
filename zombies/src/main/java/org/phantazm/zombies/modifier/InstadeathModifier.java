package org.phantazm.zombies.modifier;

import com.github.steanky.element.core.annotation.Cache;
import com.github.steanky.element.core.annotation.DataObject;
import com.github.steanky.element.core.annotation.FactoryMethod;
import com.github.steanky.element.core.annotation.Model;
import com.github.steanky.ethylene.core.ConfigElement;
import com.github.steanky.ethylene.core.ConfigPrimitive;
import com.github.steanky.ethylene.core.collection.ConfigList;
import com.github.steanky.ethylene.mapper.annotation.Default;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.minestom.server.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.phantazm.commons.InjectionStore;
import org.phantazm.zombies.event.player.OpenDoorEvent;
import org.phantazm.zombies.event.player.ZombiesPlayerDamageEvent;
import org.phantazm.zombies.scene2.ZombiesScene;

import java.util.Objects;
import java.util.Set;

@Model("zombies.modifier.insta_death")
@Cache
public class InstadeathModifier extends ModifierComponentBase {
    private final Data data;

    @FactoryMethod
    public InstadeathModifier(@NotNull Data data) {
        super(data.key, data.displayName, data.displayItem, data.ordinal, data.exclusiveModifiers);
        this.data = Objects.requireNonNull(data);
    }

    @Override
    public @NotNull Modifier apply(@NotNull InjectionStore injectionStore, @NotNull ZombiesScene scene) {
        return new Impl(data, scene);
    }

    private record Impl(Data data,
        ZombiesScene scene) implements Modifier {
        @Override
        public void apply() {
            scene.addListener(ZombiesPlayerDamageEvent.class, event -> event.setShouldDie(true));
        }
    }

    @DataObject
    public record Data(int ordinal,
        @NotNull Key key,
        @Nullable Component displayName,
        @NotNull ItemStack displayItem,
        @NotNull Set<Key> exclusiveModifiers) {
        @Default("displayName")
        public static @NotNull ConfigElement defaultDisplayName() {
            return ConfigPrimitive.NULL;
        }

        @Default("exclusiveModifiers")
        public static @NotNull ConfigElement defaultExclusiveModifiers() {
            return ConfigList.of();
        }
    }
}
