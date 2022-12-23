package org.phantazm.zombies.powerup;

import com.github.steanky.element.core.annotation.DataObject;
import com.github.steanky.element.core.annotation.Dependency;
import com.github.steanky.element.core.annotation.FactoryMethod;
import com.github.steanky.element.core.annotation.Model;
import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.ItemEntity;
import net.minestom.server.instance.Instance;
import net.minestom.server.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.function.Supplier;

@Model("zombies.powerup.visual.item")
public class ItemVisual implements Supplier<PowerupVisual> {
    private final Data data;
    private final Instance instance;

    @FactoryMethod
    public ItemVisual(@NotNull Data data, @NotNull @Dependency Instance instance) {
        this.data = Objects.requireNonNull(data, "data");
        this.instance = Objects.requireNonNull(instance, "instance");
    }

    @Override
    public PowerupVisual get() {
        return new Visual(data, instance);
    }

    @DataObject
    public record Data(@NotNull ItemStack stack,
                       long interval,
                       long period,
                       long periodOffset,
                       double amplitude,
                       double heightOffset) {

    }

    private static class Visual implements PowerupVisual {
        private final Data data;
        private final Instance instance;

        private ItemEntity entity;
        private double baseY;

        private Visual(Data data, Instance instance) {
            this.data = data;
            this.instance = instance;
        }

        @Override
        public void tick(long time) {
            if (entity == null) {
                return;
            }

            long ticks = time / MinecraftServer.TICK_MS;
            if (ticks % data.interval == 0) {
                double o = (Math.sin((2 * Math.PI * ((ticks + data.periodOffset) % data.period)) / data.period) *
                        data.amplitude) + data.heightOffset;
                Pos pos = entity.getPosition();
                entity.teleport(new Pos(pos.x(), baseY + o, pos.z()));
            }
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
    }
}
