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
import org.jetbrains.annotations.NotNull;
import org.phantazm.commons.DualComponent;
import org.phantazm.commons.InjectionStore;
import org.phantazm.core.event.equipment.EquipmentAddEvent;
import org.phantazm.zombies.scene2.ZombiesScene;

import java.util.Objects;
import java.util.Set;

@Model("zombies.modifier.equipment_restricting")
@Cache
public class EquipmentRestrictingModifier implements DualComponent<ZombiesScene, Modifier> {
    private final Data data;

    @FactoryMethod
    public EquipmentRestrictingModifier(@NotNull Data data) {
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
            scene.addListener(EquipmentAddEvent.class, event -> {
                //if blacklist == true, and equipment is in the list, it will be cancelled, otherwise not
                //if whitelist (blacklist == false, default), and equipment is NOT in the list, it will be cancelled
                if (data.blacklist == data.equipment.contains(event.equipment().key())) {
                    event.setCancelled(true);
                }
            });
        }
    }

    @DataObject
    public record Data(
        boolean blacklist,
        @NotNull Set<Key> equipment) {
        @Default("blacklist")
        public static @NotNull ConfigElement defaultBlacklist() {
            return ConfigPrimitive.of(false);
        }

        @Default("equipment")
        public static @NotNull ConfigElement defaultEquipment() {
            return ConfigList.of();
        }
    }
}
