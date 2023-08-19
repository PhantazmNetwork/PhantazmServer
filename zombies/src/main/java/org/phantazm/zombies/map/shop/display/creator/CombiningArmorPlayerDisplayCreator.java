package org.phantazm.zombies.map.shop.display.creator;

import com.github.steanky.element.core.annotation.Cache;
import com.github.steanky.element.core.annotation.DataObject;
import com.github.steanky.element.core.annotation.FactoryMethod;
import com.github.steanky.element.core.annotation.Model;
import com.github.steanky.vector.Vec3D;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.*;
import net.minestom.server.entity.metadata.other.ArmorStandMeta;
import net.minestom.server.item.ItemStack;
import net.minestom.server.utils.Direction;
import org.jetbrains.annotations.NotNull;
import org.phantazm.zombies.map.shop.Shop;
import org.phantazm.zombies.map.shop.display.ShopDisplay;
import org.phantazm.zombies.player.ZombiesPlayer;

import java.util.Map;
import java.util.Optional;

@Model("zombies.map.shop.display.player.combining_armor")
@Cache(false)
public class CombiningArmorPlayerDisplayCreator implements PlayerDisplayCreator {
    private final Data data;

    @FactoryMethod
    public CombiningArmorPlayerDisplayCreator(@NotNull Data data) {
        this.data = data;
    }

    @Override
    public @NotNull ShopDisplay forPlayer(@NotNull ZombiesPlayer zombiesPlayer) {
        return new Display(data, zombiesPlayer);
    }

    public record TieredStack(@NotNull ItemStack stack,
        int tier) {
    }

    @DataObject
    public record Data(
        @NotNull Direction face,
        boolean small,
        @NotNull Vec3D offset,
        @NotNull Map<EquipmentSlot, TieredStack> items) {
    }

    private static class Display implements ShopDisplay {
        private final Data data;
        private final ZombiesPlayer zombiesPlayer;

        private LivingEntity armorStand;
        private int ticks;

        public Display(Data data, @NotNull ZombiesPlayer zombiesPlayer) {
            this.data = data;
            this.zombiesPlayer = zombiesPlayer;
        }

        @Override
        public void tick(long time) {
            LivingEntity armorStand = this.armorStand;
            if (armorStand == null) {
                return;
            }

            if (ticks++ % 10 != 0) {
                return;
            }

            Optional<Player> playerOptional = zombiesPlayer.getPlayer();
            if (playerOptional.isEmpty()) {
                return;
            }

            Player player = playerOptional.get();
            for (EquipmentSlot slot : EquipmentSlot.armors()) {
                ItemStack existingStack = player.getEquipment(slot);
                TieredStack ourStack = data.items.get(slot);

                armorStand.setEquipment(slot, ourStack == null ? existingStack : ourStack.stack);
            }
        }

        @Override
        public void initialize(@NotNull Shop shop) {
            Entity oldArmorStand = this.armorStand;
            if (oldArmorStand != null) {
                this.armorStand = null;
                oldArmorStand.remove();
            }

            LivingEntity armorStand = new LivingEntity(EntityType.ARMOR_STAND);
            armorStand.updateViewableRule(player -> player.getUuid().equals(zombiesPlayer.getUUID()));
            ArmorStandMeta meta = (ArmorStandMeta) armorStand.getEntityMeta();
            meta.setSmall(data.small);
            meta.setInvisible(true);
            meta.setHasNoBasePlate(true);
            meta.setHasNoGravity(true);

            for (Map.Entry<EquipmentSlot, TieredStack> entry : data.items.entrySet()) {
                armorStand.setEquipment(entry.getKey(), entry.getValue().stack);
            }

            armorStand.setInstance(shop.instance(),
                Pos.fromPoint(shop.center().add(data.offset.x(), data.offset.y(), data.offset.z()))
                    .withDirection(new Vec(data.face.normalX(), data.face.normalY(), data.face.normalZ())));

            this.armorStand = armorStand;
        }

        @Override
        public void destroy(@NotNull Shop shop) {
            Entity armorStand = this.armorStand;
            if (armorStand != null) {
                armorStand.remove();
                this.armorStand = null;
            }
        }
    }
}
