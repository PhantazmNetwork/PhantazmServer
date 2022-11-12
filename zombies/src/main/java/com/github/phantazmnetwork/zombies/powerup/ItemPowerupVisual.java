package com.github.phantazmnetwork.zombies.powerup;

import com.github.steanky.element.core.annotation.*;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.ItemEntity;
import net.minestom.server.instance.Instance;
import net.minestom.server.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

@Model("zombies.powerup.visual.item")
@Cache(false)
public class ItemPowerupVisual implements PowerupVisual {
    private final Data data;
    private final Instance instance;

    private ItemEntity entity;
    private double baseY;

    @FactoryMethod
    public ItemPowerupVisual(@NotNull Data data,
            @NotNull @Dependency("zombies.dependency.map_object.instance") Instance instance) {
        this.data = Objects.requireNonNull(data, "data");
        this.instance = Objects.requireNonNull(instance, "instance");
    }

    @Override
    public void tick(long time) {
        if (entity == null) {
            return;
        }

        double o = Math.sin((2 * Math.PI * ((time + data.periodOffset) % data.period)) / data.period) * data.amplitude;

        Pos pos = entity.getPosition();
        entity.teleport(new Pos(pos.x(), baseY + o, pos.z()));
    }

    @Override
    public void spawn(double x, double y, double z) {
        Entity oldEntity = this.entity;
        if (oldEntity != null) {
            oldEntity.remove();
        }

        this.entity = new ItemEntity(data.stack);
        this.entity.setInstance(this.instance, new Vec(x, y, z));
        this.baseY = y;
    }

    @Override
    public void despawn() {
        ItemEntity entity = this.entity;

        if (entity != null) {
            entity.remove();
            this.entity = null;
        }
    }

    @DataObject
    public record Data(@NotNull ItemStack stack, long period, long periodOffset, double amplitude) {

    }
}
