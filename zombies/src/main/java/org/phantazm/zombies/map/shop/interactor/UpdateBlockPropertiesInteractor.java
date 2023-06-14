package org.phantazm.zombies.map.shop.interactor;

import com.github.steanky.element.core.annotation.Cache;
import com.github.steanky.element.core.annotation.DataObject;
import com.github.steanky.element.core.annotation.FactoryMethod;
import com.github.steanky.element.core.annotation.Model;
import com.github.steanky.vector.Vec3I;
import net.minestom.server.coordinate.Point;
import net.minestom.server.instance.block.Block;
import org.jetbrains.annotations.NotNull;
import org.phantazm.zombies.map.shop.PlayerInteraction;
import org.phantazm.zombies.map.shop.Shop;

import java.util.Map;

@Model("zombies.map.shop.interactor.update_block_properties")
@Cache(false)
public class UpdateBlockPropertiesInteractor extends InteractorBase<UpdateBlockPropertiesInteractor.Data> {
    private Shop shop;

    @FactoryMethod
    public UpdateBlockPropertiesInteractor(@NotNull Data data) {
        super(data);
    }

    @Override
    public boolean handleInteraction(@NotNull PlayerInteraction interaction) {
        Shop shop = this.shop;
        if (shop == null) {
            return false;
        }

        Point target = shop.mapOrigin().add(data.coordinate.x(), data.coordinate.y(), data.coordinate.z());

        Block block = shop.instance().getBlock(target).withProperties(data.properties);
        shop.instance().setBlock(target, block);
        return true;
    }

    @Override
    public void initialize(@NotNull Shop shop) {
        this.shop = shop;
    }

    @DataObject
    public record Data(@NotNull Vec3I coordinate, @NotNull Map<String, String> properties) {
    }
}
