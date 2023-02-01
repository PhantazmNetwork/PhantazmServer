package org.phantazm.zombies.equipment.gun.shoot.handler;

import com.github.steanky.element.core.annotation.Cache;
import com.github.steanky.element.core.annotation.DataObject;
import com.github.steanky.element.core.annotation.FactoryMethod;
import com.github.steanky.element.core.annotation.Model;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.phantazm.zombies.coin.ModifierSourceGroups;
import org.phantazm.zombies.coin.PlayerCoins;
import org.phantazm.zombies.coin.Transaction;
import org.phantazm.zombies.equipment.gun.Gun;
import org.phantazm.zombies.equipment.gun.GunState;
import org.phantazm.zombies.equipment.gun.shoot.GunHit;
import org.phantazm.zombies.equipment.gun.shoot.GunShot;
import org.phantazm.zombies.player.ZombiesPlayer;

import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Supplier;

@Model("zombies.gun.shot_handler.give_coins")
@Cache
public class GiveCoinsShotHandler implements ShotHandler {
    private final Data data;
    private final Map<? super UUID, ? extends ZombiesPlayer> playerMap;

    @FactoryMethod
    public GiveCoinsShotHandler(@NotNull Data data, @NotNull Map<? super UUID, ? extends ZombiesPlayer> playerMap) {
        this.data = Objects.requireNonNull(data, "data");
        this.playerMap = Objects.requireNonNull(playerMap, "playerMap");
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

        PlayerCoins coins = player.module().getCoins();

        for (GunHit ignored : shot.regularTargets()) {
            coins.runTransaction(new Transaction(
                    player.module().compositeTransactionModifiers().modifiers(ModifierSourceGroups.MOB_COIN_GAIN),
                    data.normalCoins)).applyIfAffordable(coins);
        }

        for (GunHit ignored : shot.headshotTargets()) {
            coins.runTransaction(new Transaction(
                    player.module().compositeTransactionModifiers().modifiers(ModifierSourceGroups.MOB_COIN_GAIN),
                    data.headshotCoins)).applyIfAffordable(coins);
        }
    }

    @DataObject
    public record Data(int normalCoins, int headshotCoins) {
    }
}
