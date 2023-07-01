package org.phantazm.zombies.map;

import com.github.steanky.ethylene.core.collection.ConfigNode;
import com.github.steanky.ethylene.core.collection.LinkedConfigNode;
import com.github.steanky.vector.Vec3D;
import org.jetbrains.annotations.NotNull;

public record LeaderboardInfo(@NotNull Vec3D location, double gap, @NotNull ConfigNode data) {

    public static final LeaderboardInfo DEFAULT = new LeaderboardInfo(Vec3D.ORIGIN, 0.1, new LinkedConfigNode(0));

}
