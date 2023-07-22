package org.phantazm.zombies.map.shop.predicate;

import com.github.steanky.element.core.annotation.Cache;
import com.github.steanky.element.core.annotation.DataObject;
import com.github.steanky.element.core.annotation.FactoryMethod;
import com.github.steanky.element.core.annotation.Model;
import net.minestom.server.entity.Player;
import net.minestom.server.inventory.PlayerInventory;
import net.minestom.server.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.phantazm.zombies.Tags;
import org.phantazm.zombies.map.shop.PlayerInteraction;
import org.phantazm.zombies.map.shop.Shop;

import java.util.Optional;

@Model("zombies.map.shop.predicate.equipment_tier")
@Cache(false)
public class EquipmentTierPredicate implements ShopPredicate {
    private final Data data;

    @FactoryMethod
    public EquipmentTierPredicate(@NotNull Data data) {
        this.data = data;
    }

    @Override
    public boolean canInteract(@NotNull PlayerInteraction interaction, @NotNull Shop shop) {
        Optional<Player> playerOptional = interaction.player().getPlayer();
        if (playerOptional.isEmpty()) {
            return false;
        }

        Player player = playerOptional.get();
        PlayerInventory inventory = player.getInventory();
        ItemStack stack = data.slot == -1 ? inventory.getItemInMainHand() : inventory.getItemStack(data.slot);
        int existingTier = stack.getTag(Tags.ARMOR_TIER);
        return existingTier < data.tier;
    }

    @DataObject
    public record Data(int slot, int tier) {
    }
}
