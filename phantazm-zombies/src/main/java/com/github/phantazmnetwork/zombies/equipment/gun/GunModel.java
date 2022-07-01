package com.github.phantazmnetwork.zombies.equipment.gun;

import net.kyori.adventure.key.Keyed;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public interface GunModel extends Keyed {

    @NotNull List<GunLevel> getLevels();

}
