package org.phantazm.zombies.equipment.perk.equipment.interactor;

import com.github.steanky.element.core.annotation.Cache;
import com.github.steanky.element.core.annotation.DataObject;
import com.github.steanky.element.core.annotation.FactoryMethod;
import com.github.steanky.element.core.annotation.Model;
import com.github.steanky.element.core.annotation.document.Description;
import com.github.steanky.ethylene.core.ConfigElement;
import com.github.steanky.ethylene.core.ConfigPrimitive;
import com.github.steanky.ethylene.mapper.annotation.Default;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.damage.Damage;
import org.jetbrains.annotations.NotNull;
import org.phantazm.mob2.Mob;
import org.phantazm.zombies.ExtraNodeKeys;
import org.phantazm.zombies.Flags;
import org.phantazm.zombies.Tags;
import org.phantazm.zombies.coin.ModifierSourceGroups;
import org.phantazm.zombies.coin.PlayerCoins;
import org.phantazm.zombies.coin.Transaction;
import org.phantazm.zombies.map.Flaggable;
import org.phantazm.zombies.map.objects.MapObjects;
import org.phantazm.zombies.player.ZombiesPlayer;

import java.util.Collection;
import java.util.Objects;
import java.util.Optional;

@Description("""
    Interactor capable of hitting a mob in a Zombies game. Supports variable knockback, damage, and armor bypassing
    capabilities.
    """)
@Model("zombies.perk.interactor.melee")
@Cache(false)
public class MeleeInteractorCreator implements PerkInteractorCreator {
    private final Data data;
    private final Flaggable mapFlags;

    @FactoryMethod
    public MeleeInteractorCreator(@NotNull Data data, @NotNull MapObjects mapObjects) {
        this.data = Objects.requireNonNull(data);
        this.mapFlags = mapObjects.module().flags();
    }

    @Override
    public @NotNull PerkInteractor forPlayer(@NotNull ZombiesPlayer zombiesPlayer) {
        return new Interactor(data, zombiesPlayer, mapFlags);
    }

    @DataObject
    public record Data(
        @Description("The damage it does on a successful hit") float damage,
        @Description("The amount of knockback the weapon deals; 0.4 is the vanilla knockback from an " +
            "unarmed hand") float knockback,
        @Description("The number of coins to give on a successful hit.") int coins,
        @Description("The number of coins to give when instakill is active.") int instaKillCoins,
        @Description("Whether damage from this weapon should bypass enemy armor") boolean bypassArmor) {
        @Default("instaKillCoins")
        public static @NotNull ConfigElement defaultInstaKillCoins() {
            return ConfigPrimitive.of(50);
        }

        @Default("bypassArmor")
        public static @NotNull ConfigElement defaultBypassArmor() {
            return ConfigPrimitive.of(false);
        }
    }

    private record Interactor(Data data,
        ZombiesPlayer zombiesPlayer,
        Flaggable mapFlags) implements PerkInteractor {
        private Interactor(@NotNull Data data, @NotNull ZombiesPlayer zombiesPlayer, Flaggable mapFlags) {
            this.data = Objects.requireNonNull(data);
            this.zombiesPlayer = Objects.requireNonNull(zombiesPlayer);
            this.mapFlags = mapFlags;
        }

        @Override
        public boolean setSelected(boolean selected) {
            return false;
        }

        @Override
        public boolean leftClick() {
            return false;
        }

        @Override
        public boolean rightClick() {
            return false;
        }

        @Override
        public boolean attack(@NotNull Entity target) {
            if (!(target instanceof LivingEntity livingEntity)) {
                return false;
            }

            Optional<Player> playerOptional = zombiesPlayer.getPlayer();
            if (playerOptional.isEmpty()) {
                return false;
            }

            if (!(livingEntity instanceof Mob hitMob)) {
                return false;
            }

            Player player = playerOptional.get();
            Pos playerPosition = player.getPosition();

            boolean isInstaKill;
            if ((mapFlags.hasFlag(Flags.INSTA_KILL) || zombiesPlayer.flags().hasFlag(Flags.INSTA_KILL)) &&
                (!hitMob.data().extra().getBooleanOrDefault(false, ExtraNodeKeys.RESIST_INSTAKILL))) {
                livingEntity.setTag(Tags.LAST_HIT_BY, player.getUuid());
                livingEntity.kill();
                isInstaKill = true;
            } else {
                double angle = playerPosition.yaw() * (Math.PI / 180);
                livingEntity.damage(Damage.fromPlayer(player, data.damage), data.bypassArmor);
                livingEntity.takeKnockback(data.knockback, Math.sin(angle), -Math.cos(angle));
                isInstaKill = false;
            }

            PlayerCoins coins = zombiesPlayer.module().getCoins();
            Collection<Transaction.Modifier> modifiers = zombiesPlayer.module().compositeTransactionModifiers()
                .modifiers(ModifierSourceGroups.MOB_COIN_GAIN);

            coins.runTransaction(new Transaction(modifiers, isInstaKill ? data.instaKillCoins : data.coins))
                .applyIfAffordable(coins);
            return true;
        }
    }
}
