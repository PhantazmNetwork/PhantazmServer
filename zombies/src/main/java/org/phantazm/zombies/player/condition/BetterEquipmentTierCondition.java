package org.phantazm.zombies.player.condition;

import com.github.steanky.element.core.annotation.Cache;
import com.github.steanky.element.core.annotation.DataObject;
import com.github.steanky.element.core.annotation.FactoryMethod;
import com.github.steanky.element.core.annotation.Model;
import net.kyori.adventure.key.Key;
import net.minestom.server.entity.Player;
import net.minestom.server.inventory.PlayerInventory;
import net.minestom.server.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.phantazm.zombies.Tags;
import org.phantazm.zombies.player.ZombiesPlayer;

import java.util.Optional;
import java.util.function.Predicate;

@Model("zombies.player.condition.better_equipment")
@Cache(false)
public class BetterEquipmentTierCondition implements Predicate<ZombiesPlayer> {
    private final Data data;

    @FactoryMethod
    public BetterEquipmentTierCondition(@NotNull Data data) {
        this.data = data;
    }

    @Override
    public boolean test(ZombiesPlayer zombiesPlayer) {
        if (zombiesPlayer.module().getEquipmentHandler().hasEquipment(data.groupKey, data.equipmentKey)) {
            return true;
        }

        Optional<Player> playerOptional = zombiesPlayer.getPlayer();
        if (playerOptional.isEmpty()) {
            return false;
        }

        Player player = playerOptional.get();
        PlayerInventory inventory = player.getInventory();
        ItemStack stack = data.slot == -1 ? inventory.getItemInMainHand() : inventory.getItemStack(data.slot);
        int existingTier = stack.getTag(Tags.ARMOR_TIER);
        return existingTier > data.tier;
    }

    // TODO: don't require group key as well as slot
    @DataObject
    public record Data(@NotNull Key groupKey, @NotNull Key equipmentKey, int slot,
        int tier) {
    }
}
