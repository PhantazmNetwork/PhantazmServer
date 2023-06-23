package org.phantazm.core.npc.settings;

import com.github.steanky.element.core.annotation.Cache;
import com.github.steanky.element.core.annotation.DataObject;
import com.github.steanky.element.core.annotation.FactoryMethod;
import com.github.steanky.element.core.annotation.Model;
import com.github.steanky.ethylene.core.ConfigElement;
import com.github.steanky.ethylene.core.ConfigPrimitive;
import com.github.steanky.ethylene.core.collection.ConfigList;
import com.github.steanky.ethylene.mapper.annotation.Default;
import net.kyori.adventure.text.Component;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EquipmentSlot;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.entity.metadata.EntityMeta;
import net.minestom.server.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.function.Consumer;

@Model("npc.entity.settings")
@Cache
public class BasicEntitySettings implements Consumer<Entity> {
    private final Data data;

    @FactoryMethod
    public BasicEntitySettings(@NotNull Data data) {
        this.data = data;
    }

    @Override
    public void accept(Entity entity) {
        EntityMeta meta = entity.getEntityMeta();

        if (!data.displayName.equals(Component.empty())) {
            meta.setCustomNameVisible(true);
            meta.setCustomName(data.displayName);
        }

        meta.setOnFire(data.onFire);
        meta.setPose(data.pose);

        if (entity instanceof LivingEntity livingEntity) {
            for (Map.Entry<EquipmentSlot, ItemStack> entry : data.equipment.entrySet()) {
                livingEntity.setEquipment(entry.getKey(), entry.getValue());
            }
        }
    }

    @DataObject
    public record Data(@NotNull Component displayName,
                       boolean onFire,
                       @NotNull Entity.Pose pose,
                       @NotNull Map<EquipmentSlot, ItemStack> equipment) {
        @Default("displayName")
        public static ConfigElement defaultDisplayName() {
            return ConfigPrimitive.of("");
        }

        @Default("onFire")
        public static ConfigElement defaultOnFire() {
            return ConfigPrimitive.of(false);
        }

        @Default("pose")
        public static ConfigElement defaultPose() {
            return ConfigPrimitive.of("STANDING");
        }

        @Default("equipment")
        public static ConfigElement defaultEquipment() {
            return ConfigList.of();
        }
    }
}
