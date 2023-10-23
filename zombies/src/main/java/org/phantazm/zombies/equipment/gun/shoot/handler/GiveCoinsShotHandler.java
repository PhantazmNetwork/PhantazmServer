package org.phantazm.zombies.equipment.gun.shoot.handler;

import com.github.steanky.element.core.annotation.Cache;
import com.github.steanky.element.core.annotation.DataObject;
import com.github.steanky.element.core.annotation.FactoryMethod;
import com.github.steanky.element.core.annotation.Model;
import net.kyori.adventure.text.Component;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.LivingEntity;
import org.jetbrains.annotations.NotNull;
import org.phantazm.core.player.PlayerView;
import org.phantazm.mob2.Mob;
import org.phantazm.zombies.ExtraNodeKeys;
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
    private final Map<PlayerView, ZombiesPlayer> playerMap;
    private final MapObjects mapObjects;

    @FactoryMethod
    public GiveCoinsShotHandler(@NotNull Data data, @NotNull Map<PlayerView, ZombiesPlayer> playerMap,
        @NotNull MapObjects mapObjects) {
        this.data = Objects.requireNonNull(data);
        this.playerMap = Objects.requireNonNull(playerMap);
        this.mapObjects = Objects.requireNonNull(mapObjects);
    }

    @Override
    public void tick(@NotNull GunState state, long time) {

    }

    @Override
    public void handle(@NotNull Gun gun, @NotNull GunState state, @NotNull Entity attacker,
        @NotNull Collection<UUID> previousHits, @NotNull GunShot shot) {
        UUID attackerId = attacker.getUuid();
        ZombiesPlayer player = playerMap.get(PlayerView.lookup(attackerId));
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
            for (GunHit hit : shot.regularTargets()) {
                change += isInstaKill && vulnerableToInstakill(hit.entity()) ? data.instaKillCoins : data.normalCoins;
            }
        }

        if (!shot.headshotTargets().isEmpty()) {
            displays.add(
                Component.text((isInstaKill ? "Insta Kill " : "Critical Hit ") + shot.headshotTargets().size() + "x"));
            for (GunHit hit : shot.headshotTargets()) {
                change += isInstaKill && vulnerableToInstakill(hit.entity()) ? data.instaKillCoins : data.headshotCoins;
            }
        }

        coins.runTransaction(new Transaction(modifiers, displays, change)).applyIfAffordable(coins);
    }

    private boolean vulnerableToInstakill(LivingEntity livingEntity) {
        if (!(livingEntity instanceof Mob mob)) {
            return true;
        }

        return !mob.data().extra().getBooleanOrDefault(false, ExtraNodeKeys.RESIST_INSTAKILL);
    }

    @DataObject
    public record Data(int normalCoins,
        int headshotCoins,
        int instaKillCoins) {
    }
}
