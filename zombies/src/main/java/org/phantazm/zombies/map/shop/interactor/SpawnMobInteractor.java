package org.phantazm.zombies.map.shop.interactor;

import com.github.steanky.element.core.annotation.Cache;
import com.github.steanky.element.core.annotation.DataObject;
import com.github.steanky.element.core.annotation.FactoryMethod;
import com.github.steanky.element.core.annotation.Model;
import com.github.steanky.vector.Vec3I;
import net.kyori.adventure.key.Key;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import org.jetbrains.annotations.NotNull;
import org.phantazm.mob2.MobSpawner;
import org.phantazm.zombies.map.shop.PlayerInteraction;
import org.phantazm.zombies.map.shop.Shop;

@Model("zombies.map.shop.interactor.spawn_mob")
@Cache(false)
public class SpawnMobInteractor extends InteractorBase<SpawnMobInteractor.Data> {
    private Shop shop;

    @FactoryMethod
    public SpawnMobInteractor(@NotNull Data data) {
        super(data);
    }

    @Override
    public boolean handleInteraction(@NotNull PlayerInteraction interaction) {
        Shop shop = this.shop;
        if (shop == null) return false;

        Point target = shop.mapOrigin().add(data.coordinate.x(), data.coordinate.y(), data.coordinate.z());

        MobSpawner spawner = interaction.player().getScene().map().objects().mobSpawner();
        if (!spawner.canSpawn(data.mob)) return false;

        spawner.spawn(data.mob, interaction.player().getScene().instance(), Pos.fromPoint(target));
        return true;
    }

    @Override
    public void initialize(@NotNull Shop shop) {
        this.shop = shop;
    }

    @DataObject
    public record Data(@NotNull Vec3I coordinate,
        @NotNull Key mob) {
    }
}
