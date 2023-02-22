package org.phantazm.core.equipment;

import com.github.steanky.element.core.annotation.DataObject;
import com.github.steanky.element.core.annotation.FactoryMethod;
import com.github.steanky.element.core.annotation.Model;
import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.NotNull;
import org.phantazm.core.equipment.UpgradePath;

import java.util.*;

@Model("zombies.map.shop.upgrade_path.linear")
public class LinearUpgradePath implements UpgradePath {
    private final Data data;

    @FactoryMethod
    public LinearUpgradePath(@NotNull Data data) {
        this.data = Objects.requireNonNull(data, "data");
    }

    @Override
    public @NotNull Optional<Key> nextUpgrade(@NotNull Key key) {
        Iterator<Key> iterator = data.upgrades.iterator();
        while (iterator.hasNext()) {
            Key upgrade = iterator.next();
            if (upgrade.equals(key)) {
                if (iterator.hasNext()) {
                    return Optional.of(iterator.next());
                }
            }
        }

        return Optional.empty();
    }

    @DataObject
    public record Data(@NotNull List<Key> upgrades) {

    }
}
