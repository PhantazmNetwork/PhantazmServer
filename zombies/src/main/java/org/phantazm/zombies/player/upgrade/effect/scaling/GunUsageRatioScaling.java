package org.phantazm.zombies.player.upgrade.effect.scaling;

import com.github.steanky.element.core.annotation.Cache;
import com.github.steanky.element.core.annotation.DataObject;
import com.github.steanky.element.core.annotation.FactoryMethod;
import com.github.steanky.element.core.annotation.Model;
import com.github.steanky.ethylene.mapper.annotation.Default;
import net.kyori.adventure.key.Key;
import net.minestom.server.entity.Player;
import net.minestom.server.tag.Tag;
import org.jetbrains.annotations.NotNull;
import org.phantazm.commons.InjectionStore;
import org.phantazm.core.TagUtils;
import org.phantazm.core.inventory.InventoryAccess;
import org.phantazm.core.inventory.InventoryObject;
import org.phantazm.zombies.ScalingFormulae;
import org.phantazm.zombies.equipment.gun.Gun;
import org.phantazm.zombies.event.trait.GunEvent;
import org.phantazm.zombies.player.ZombiesPlayer;
import org.phantazm.zombies.player.state.InventoryKeys;
import org.phantazm.zombies.player.upgrade.PlayerUpgrade;
import org.phantazm.zombies.player.upgrade.trigger.TriggerData;

import java.util.*;

@Model("zombies.upgrade.effect.scaling.gun_usage_ratio")
@Cache
public class GunUsageRatioScaling implements ScalingComponent {
    private final Data data;

    @FactoryMethod
    public GunUsageRatioScaling(@NotNull Data data) {
        this.data = data;
    }

    @Override
    public @NotNull Scaling apply(@NotNull InjectionStore injectionStore, @NotNull ZombiesPlayer zombiesPlayer) {
        return new Internal(data);
    }

    private static final class Internal implements Scaling {
        private final Data data;

        private final boolean largerNumberIsStart;
        private final double largerNumber;
        private final double smallerNumber;

        private Internal(Data data) {
            this.data = data;

            largerNumber = Math.max(data.start, data.end);
            smallerNumber = Math.min(data.start, data.end);
            largerNumberIsStart = data.start > data.end;
        }


        @Override
        public double getMultiplier(@NotNull PlayerUpgrade upgrade, @NotNull ZombiesPlayer zombiesPlayer, @NotNull TriggerData triggerData) {
            if (!(triggerData.raw() instanceof GunEvent gunEvent)) {
                return 1.0;
            }

            Optional<Player> optPlayer = zombiesPlayer.getPlayer();
            if (optPlayer.isEmpty()) {
                return 1.0;
            }

            Player player = optPlayer.get();
            Gun shotWith = gunEvent.gun();

            if (!data.eligibleWeapons.contains(shotWith.key())) {
                return 1.0;
            }

            int shotAmount = retrieveGunShotAmount(shotWith.shotTag(), player, zombiesPlayer);
            double percentage = 1.0;

            // will mess up the math if not over 0 (also it shouldn't be possible to be under 0)
            if (shotAmount <= 0) {
                return 1.0;
            }

            // taken from the gun with currently highest # of shots,
            int baselineGunShots = 0;
            Key baselineGun = null;

            if (data.onlyHotbar) {
                InventoryAccess access = zombiesPlayer.module().getInventoryAccessRegistry()
                    .getAccess(InventoryKeys.ALIVE_ACCESS);

                for (InventoryObject object : access.profile().objects()) {
                    if (!(object instanceof Gun gun)) {
                        continue;
                    }

                    if (!data.eligibleWeapons.contains(gun.key())) {
                        continue;
                    }

                    int currentGunShotAmount = retrieveGunShotAmount(((Gun) gun).shotTag(), player, zombiesPlayer);
                    if (currentGunShotAmount > baselineGunShots) {
                        baselineGun = gun.key();
                        baselineGunShots = currentGunShotAmount;
                    }
                }

                if (baselineGun == null || baselineGunShots == 0) {
                    return 1.0;
                }

                percentage = (double) shotAmount / baselineGunShots;

            } else {
                for (Key definedKey : data.eligibleWeapons) {
                    int dynamicGunShots = retrieveGunShotAmount(Gun.shotTagFromKey(definedKey), player, zombiesPlayer);
                    if (dynamicGunShots > baselineGunShots) {
                        baselineGun = definedKey;
                        baselineGunShots = dynamicGunShots;
                    }
                }

                if (baselineGun == null || baselineGunShots == 0) {
                    return 1.0;
                }

                percentage = (double) shotAmount / baselineGunShots;
            }
            return ScalingFormulae.computeMultiplier(data.returnZeroOutsideRange, percentage, largerNumberIsStart, largerNumber,
                smallerNumber, data.startMultiplier, data.endMultiplier);
        }

        private int retrieveGunShotAmount(Tag<Integer> gunShotTag, Player player, ZombiesPlayer zombiesPlayer) {
            int timesShot = TagUtils.sceneLocalTags(player, zombiesPlayer.getScene()).getTag(gunShotTag);
            return timesShot;
        }

    }

    @Default("""
        {
          start=1.0,
          end=0.0,
          returnZeroOutsideRange=false
        }
        """
    )
    @DataObject
    public record Data(
        double start,
        double end,
        double startMultiplier,
        double endMultiplier,
        boolean returnZeroOutsideRange,
        boolean onlyHotbar,
        @NotNull Set<Key> eligibleWeapons
    ){}
}
