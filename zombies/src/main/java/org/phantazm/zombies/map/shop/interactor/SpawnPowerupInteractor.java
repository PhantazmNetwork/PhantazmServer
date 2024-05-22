package org.phantazm.zombies.map.shop.interactor;

import com.github.steanky.element.core.annotation.*;
import com.github.steanky.vector.Vec3I;
import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.NotNull;
import org.phantazm.zombies.map.shop.PlayerInteraction;
import org.phantazm.zombies.map.shop.Shop;
import org.phantazm.zombies.powerup.PowerupHandler;
import org.phantazm.zombies.scene2.ZombiesScene;

import java.util.function.Supplier;

@Model("zombies.map.shop.interactor.spawn_powerup")
@Cache(false)
public class SpawnPowerupInteractor implements ShopInteractor {
    private final Data data;
    private final Supplier<ZombiesScene> zombiesScene;

    private Shop shop;

    @FactoryMethod
    public SpawnPowerupInteractor(@NotNull Data data, @NotNull Supplier<ZombiesScene> zombiesScene) {
        this.data = data;
        this.zombiesScene = zombiesScene;
    }

    @Override
    public boolean handleInteraction(@NotNull PlayerInteraction interaction) {
        ZombiesScene scene = zombiesScene.get();
        PowerupHandler handler = scene.map().powerupHandler();
        if (!handler.canSpawnType(data.powerup)) {
            return false;
        }

        handler.spawn(data.powerup, shop.mapOrigin().add(data.target.x(), data.target.y(), data.target.z()));
        return true;
    }

    @Override
    public void initialize(@NotNull Shop shop) {
        this.shop = shop;
    }

    @DataObject
    public record Data(@NotNull Key powerup,
        @NotNull Vec3I target) {
    }
}