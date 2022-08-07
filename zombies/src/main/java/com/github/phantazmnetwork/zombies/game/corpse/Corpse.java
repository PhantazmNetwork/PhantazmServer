package com.github.phantazmnetwork.zombies.game.corpse;

import com.github.phantazmnetwork.core.hologram.Hologram;
import com.github.phantazmnetwork.core.time.TickFormatter;
import com.github.phantazmnetwork.zombies.game.player.ZombiesPlayer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.entity.Entity;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class Corpse {

    private final Hologram hologram;

    private final Entity corpseEntity;

    private final TickFormatter tickFormatter;

    public Corpse(@NotNull Hologram hologram, @NotNull Entity corpseEntity, @NotNull TickFormatter tickFormatter) {
        this.hologram = Objects.requireNonNull(hologram, "hologram");
        this.corpseEntity = Objects.requireNonNull(corpseEntity, "corpseEntity");
        this.tickFormatter = Objects.requireNonNull(tickFormatter, "tickFormatter");
    }

    public void start() {
        hologram.add(Component.text("------", NamedTextColor.YELLOW));
        hologram.add(Component.empty());
        hologram.add(Component.empty());
        hologram.add(Component.text("------", NamedTextColor.YELLOW));
        corpseEntity.setPose(Entity.Pose.SLEEPING);
    }

    public void deathTick(long time, long ticksUntilDeath) {
        hologram.set(1, Component.text("dying", NamedTextColor.RED));
        hologram.set(2, tickFormatter.format(ticksUntilDeath));
    }

    public void reviveTick(long time, @NotNull ZombiesPlayer reviver, long ticksUntilRevive) {
        hologram.set(1, Component.text("reviving", NamedTextColor.GREEN));
        hologram.set(2, tickFormatter.format(ticksUntilRevive));
    }

    public void disable() {
        hologram.clear();
    }

    public void remove() {
        disable();
        corpseEntity.remove();
    }

}
