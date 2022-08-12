package com.github.phantazmnetwork.zombies.game.map.shop.predicate;

import com.github.phantazmnetwork.commons.Prioritized;
import com.github.phantazmnetwork.core.inventory.InventoryObjectGroup;
import com.github.phantazmnetwork.core.player.PlayerView;
import com.github.phantazmnetwork.zombies.equipment.Equipment;
import com.github.phantazmnetwork.zombies.equipment.PlayerEquipmentCreator;
import com.github.phantazmnetwork.zombies.equipment.Upgradable;
import com.github.phantazmnetwork.zombies.game.map.ZombiesMap;
import com.github.phantazmnetwork.zombies.game.map.shop.PlayerInteraction;
import com.github.phantazmnetwork.zombies.game.player.ZombiesPlayer;
import com.github.steanky.element.core.annotation.ElementData;
import com.github.steanky.element.core.annotation.ElementDependency;
import com.github.steanky.element.core.annotation.ElementModel;
import com.github.steanky.element.core.annotation.FactoryMethod;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.kyori.adventure.key.Key;
import net.minestom.server.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.Optional;
import java.util.Set;

@ElementModel("zombies.map.shop.predicate.equipment_cost")
public class EquipmentCostPredicate extends PredicateBase<EquipmentCostPredicate.Data> {
    private final ZombiesMap map;

    @FactoryMethod
    public EquipmentCostPredicate(@NotNull Data data,
            @NotNull @ElementDependency("zombies.dependency.map") ZombiesMap map) {
        super(data);
        this.map = Objects.requireNonNull(map, "map");
    }

    @Override
    public boolean canInteract(@NotNull PlayerInteraction interaction) {
        ZombiesPlayer zombiesPlayer = interaction.getPlayer();
        PlayerView playerView = zombiesPlayer.getPlayerView();
        Optional<Player> playerOptional = playerView.getPlayer();

        if (playerOptional.isPresent()) {
            Player player = playerOptional.get();
            Optional<Equipment> equipmentOptional = zombiesPlayer.getHeldEquipment();

            if (equipmentOptional.isPresent()) {
                Equipment equipment = equipmentOptional.get();
                if (equipment instanceof Upgradable upgradable) {
                    Set<Key> keys = upgradable.getSuggestedUpgrades();
                    if (keys.isEmpty()) {
                        //TODO: no upgrade message
                        return false;
                    }

                    if (keys.size() == 1) {
                        return canPurchase(player, zombiesPlayer, keys.iterator().next(), equipment);
                    }

                    //TODO: handle multiple upgrade paths
                }
            }
            else {
                PlayerEquipmentCreator playerEquipmentCreator = map.equipmentCreator();

                //TODO: use hasEquipment(Key)
                Optional<Equipment> optionalEquipment =
                        playerEquipmentCreator.createEquipment(playerView, data.equipment);
                if (optionalEquipment.isEmpty()) {
                    //TODO: log missing equipment somehow (send message to player?)
                    return false;
                }

                InventoryObjectGroup group =
                        zombiesPlayer.getProfileSwitcher().getCurrentProfile().getGroup(data.equipmentGroup);

                //TODO: full group message
                return !group.isFull();
            }
        }

        return false;
    }

    private boolean canPurchase(Player player, ZombiesPlayer zombiesPlayer, Key upgrade, Equipment current) {
        //TODO: determine purchasability
        return false;
    }

    @ElementData
    public record Data(int priority,
                       @NotNull Key equipment,
                       @NotNull Object2IntMap<Key> upgradeCosts,
                       @NotNull Key equipmentGroup) implements Prioritized {
    }
}
