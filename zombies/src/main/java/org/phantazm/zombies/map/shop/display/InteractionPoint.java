package org.phantazm.zombies.map.shop.display;

import com.github.steanky.element.core.annotation.Cache;
import com.github.steanky.element.core.annotation.DataObject;
import com.github.steanky.element.core.annotation.FactoryMethod;
import com.github.steanky.element.core.annotation.Model;
import com.github.steanky.vector.Vec3D;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.metadata.other.ArmorStandMeta;
import org.jetbrains.annotations.NotNull;
import org.phantazm.zombies.map.shop.Shop;

@Model("zombies.map.shop.display.interaction_point")
@Cache(false)
public class InteractionPoint implements ShopDisplay {
    private final Data data;

    private Entity entity;

    @FactoryMethod
    public InteractionPoint(@NotNull Data data) {
        this.data = data;
    }

    @Override
    public void initialize(@NotNull Shop shop) {
        Entity oldEntity = this.entity;
        if (oldEntity != null) {
            this.entity = null;
            oldEntity.remove();
        }

        Entity armorStand = new Entity(EntityType.ARMOR_STAND);
        ArmorStandMeta meta = (ArmorStandMeta)armorStand.getEntityMeta();
        meta.setHasNoGravity(true);
        meta.setInvisible(true);
        meta.setHasNoBasePlate(true);

        armorStand.setInstance(shop.instance(), shop.center()
                .add(data.offset.x(), data.offset.y() - (armorStand.getBoundingBox().height() / 2), data.offset.z()));
        this.entity = armorStand;
    }

    @Override
    public void destroy(@NotNull Shop shop) {
        Entity entity = this.entity;
        if (entity != null) {
            entity.remove();
        }
    }

    @DataObject
    public record Data(@NotNull Vec3D offset) {
    }
}
