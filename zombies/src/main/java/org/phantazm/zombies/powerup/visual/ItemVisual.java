package org.phantazm.zombies.powerup.visual;

import com.github.steanky.element.core.annotation.Cache;
import com.github.steanky.element.core.annotation.DataObject;
import com.github.steanky.element.core.annotation.FactoryMethod;
import com.github.steanky.element.core.annotation.Model;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.ItemEntity;
import net.minestom.server.instance.Instance;
import net.minestom.server.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.phantazm.zombies.scene.ZombiesScene;

@Model("zombies.powerup.visual.item")
@Cache(false)
public class ItemVisual implements PowerupVisualComponent {
    private final Data data;

    @FactoryMethod
    public ItemVisual(@NotNull Data data) {
        this.data = data;
    }

    @Override
    public @NotNull PowerupVisual apply(@NotNull ZombiesScene scene) {
        return new Visual(data, scene.instance());
    }

    @DataObject
    public record Data(
        @NotNull ItemStack stack,
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
        private long ticks = 0;

        private Visual(Data data, Instance instance) {
            this.data = data;
            this.instance = instance;
        }

        @Override
        public void tick(long time) {
            if (entity == null) {
                return;
            }

            ++ticks;
            if (ticks >= data.interval) {
                double o = (Math.sin((2 * Math.PI * ((ticks + data.periodOffset) % data.period)) / data.period) *
                    data.amplitude) + data.heightOffset;
                Pos pos = entity.getPosition();
                entity.teleport(new Pos(pos.x(), baseY + o, pos.z()));
                ticks = 0;
            }
        }

        @Override
        public void spawn(double x, double y, double z) {
            Entity oldEntity = this.entity;
            if (oldEntity != null) {
                oldEntity.remove();
            }

            this.entity = new ItemEntity(data.stack);
            this.entity.setNoGravity(true);
            this.entity.setPickable(false);
            this.entity.setMergeable(false);
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
