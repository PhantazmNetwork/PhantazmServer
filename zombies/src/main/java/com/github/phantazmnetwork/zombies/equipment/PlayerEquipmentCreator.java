package com.github.phantazmnetwork.zombies.equipment;

import com.github.phantazmnetwork.core.player.PlayerView;
import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

@FunctionalInterface
public interface PlayerEquipmentCreator {

    @NotNull <TEquipment extends Equipment> Optional<TEquipment> createEquipment(@NotNull PlayerView playerView,
            @NotNull Key equipmentKey);

}
