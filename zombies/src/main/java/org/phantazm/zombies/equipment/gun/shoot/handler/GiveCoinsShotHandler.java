package org.phantazm.zombies.equipment.gun.shoot.handler;

import com.github.steanky.element.core.annotation.Cache;
import com.github.steanky.element.core.annotation.DataObject;
import com.github.steanky.element.core.annotation.FactoryMethod;
import com.github.steanky.element.core.annotation.Model;
import net.kyori.adventure.text.Component;
import net.minestom.server.entity.Entity;
import org.jetbrains.annotations.NotNull;
import org.phantazm.zombies.Flags;
import org.phantazm.zombies.coin.ModifierSourceGroups;
import org.phantazm.zombies.coin.PlayerCoins;
import org.phantazm.zombies.coin.Transaction;
import org.phantazm.zombies.equipment.gun.Gun;
import org.phantazm.zombies.equipment.gun.GunState;
import org.phantazm.zombies.equipment.gun.shoot.GunHit;
import org.phantazm.zombies.equipment.gun.shoot.GunShot;
import org.phantazm.zombies.map.objects.MapObjects;
import org.phantazm.zombies.player.ZombiesPlayer;

import java.util.*;

@Model("zombies.gun.shot_handler.give_coins")
@Cache(false)
public class GiveCoinsShotHandler implements ShotHandler {
    private final Data data;
    private final Map<? super UUID, ? extends ZombiesPlayer> playerMap;
    private final MapObjects mapObjects;

    @FactoryMethod
    public GiveCoinsShotHandler(@NotNull Data data, @NotNull Map<? super UUID, ? extends ZombiesPlayer> playerMap,
            @NotNull MapObjects mapObjects) {
        this.data = Objects.requireNonNull(data, "data");
        this.playerMap = Objects.requireNonNull(playerMap, "playerMap");
        this.mapObjects = Objects.requireNonNull(mapObjects, "mapObjects");
    }

    @Override
    public void tick(@NotNull GunState state, long time) {

    }

    @Override
    public void handle(@NotNull Gun gun, @NotNull GunState state, @NotNull Entity attacker,
            @NotNull Collection<UUID> previousHits, @NotNull GunShot shot) {
        UUID attackerId = attacker.getUuid();
        ZombiesPlayer player = playerMap.get(attackerId);
        if (player == null) {
            return;
        }

        boolean isInstaKill = mapObjects.module().flags().hasFlag(Flags.INSTA_KILL);

        PlayerCoins coins = player.module().getCoins();
        Collection<Transaction.Modifier> modifiers =
                player.module().compositeTransactionModifiers().modifiers(ModifierSourceGroups.MOB_COIN_GAIN);

        int change = 0;

        Collection<Component> displays = new ArrayList<>(2);
        if (!shot.regularTargets().isEmpty()) {
            displays.add(Component.text((isInstaKill ? "Insta Kill " : "") + shot.regularTargets().size() + "x"));
            for (GunHit ignored : shot.regularTargets()) {
                change += isInstaKill ? data.instaKillCoins : data.normalCoins;
            }
        }

        if (!shot.headshotTargets().isEmpty()) {
            displays.add(Component.text((isInstaKill ? "Insta Kill " : "Critical Hit ") + shot.headshotTargets().size() + "x"));
            for (GunHit ignored : shot.headshotTargets()) {
                change += isInstaKill ? data.instaKillCoins : data.normalCoins;
            }
        }

        coins.runTransaction(new Transaction(modifiers, displays, change)).applyIfAffordable(coins);
    }

    @DataObject
    public record Data(int normalCoins, int headshotCoins, int instaKillCoins) {
    }
}
