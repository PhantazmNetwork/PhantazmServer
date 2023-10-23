package org.phantazm.zombies.map;

import com.github.steanky.ethylene.core.collection.ConfigNode;
import com.github.steanky.ethylene.core.collection.LinkedConfigNode;
import com.github.steanky.vector.Vec3D;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import org.jetbrains.annotations.NotNull;

public record LeaderboardInfo(
    @NotNull Vec3D location,
    double gap,
    @NotNull Sound clickSound,
    @NotNull ConfigNode data) {

    public static final LeaderboardInfo DEFAULT = new LeaderboardInfo(Vec3D.ORIGIN, 0.1, Sound.sound(Key.key("minecraft:block.lever.click"), Sound.Source.MASTER, 1.0F, 2.0F), new LinkedConfigNode(0));

}
