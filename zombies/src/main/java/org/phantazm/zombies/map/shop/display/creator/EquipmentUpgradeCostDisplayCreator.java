package org.phantazm.zombies.map.shop.display.creator;

import com.github.steanky.element.core.annotation.*;
import com.github.steanky.vector.Vec3D;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.minestom.server.coordinate.Vec;
import org.jetbrains.annotations.NotNull;
import org.phantazm.core.VecUtils;
import org.phantazm.core.equipment.Equipment;
import org.phantazm.core.equipment.Upgradable;
import org.phantazm.core.equipment.UpgradePath;
import org.phantazm.core.hologram.ViewableHologram;
import org.phantazm.zombies.coin.Transaction;
import org.phantazm.zombies.map.shop.Shop;
import org.phantazm.zombies.map.shop.display.HologramDisplayBase;
import org.phantazm.zombies.map.shop.display.ShopDisplay;
import org.phantazm.zombies.player.ZombiesPlayer;

import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

@Model("zombies.map.shop.display.player.equipment_cost")
@Cache(false)
public class EquipmentUpgradeCostDisplayCreator implements PlayerDisplayCreator {
    private final Data data;
    private final UpgradePath upgradePath;

    @FactoryMethod
    public EquipmentUpgradeCostDisplayCreator(@NotNull Data data,
            @NotNull @Child("upgrade_path") UpgradePath upgradePath) {
        this.data = Objects.requireNonNull(data);
        this.upgradePath = Objects.requireNonNull(upgradePath);
    }

    @Override
    public @NotNull ShopDisplay forPlayer(@NotNull ZombiesPlayer zombiesPlayer) {
        return new Display(data, zombiesPlayer, upgradePath);
    }

    private static class Display extends HologramDisplayBase {
        private final Data data;
        private final ZombiesPlayer zombiesPlayer;
        private final UpgradePath upgradePath;

        private long updateTicks;

        private Display(@NotNull Data data, @NotNull ZombiesPlayer zombiesPlayer, @NotNull UpgradePath upgradePath) {
            super(new ViewableHologram(Vec.ZERO, 0, player -> player.getUuid().equals(zombiesPlayer.getUUID())));
            this.data = Objects.requireNonNull(data);
            this.zombiesPlayer = Objects.requireNonNull(zombiesPlayer);
            this.upgradePath = Objects.requireNonNull(upgradePath);
            this.updateTicks = -1;
        }

        private Optional<Integer> computeCost() {
            boolean foundEquipment = false;
            for (Equipment equipment : zombiesPlayer.module().getEquipmentHandler().getEquipment(data.groupKey)) {
                if (!equipment.key().equals(data.equipmentKey)) {
                    continue;
                }

                foundEquipment = true;
                if (equipment instanceof Upgradable upgradable) {
                    Optional<Key> levelOptional = upgradePath.nextUpgrade(upgradable.currentLevel());
                    if (levelOptional.isPresent()) {
                        Key level = levelOptional.get();
                        Integer cost = data.upgradeCosts.get(level);

                        if (cost != null) {
                            return Optional.of(applyModifiers(cost));
                        }
                    }
                }
            }

            if (!foundEquipment) {
                return Optional.of(applyModifiers(data.baseCost));
            }

            return Optional.empty();
        }

        private int applyModifiers(int cost) {
            Collection<Transaction.Modifier> modifiers =
                    zombiesPlayer.module().compositeTransactionModifiers().modifiers(data.costModifier);

            return -zombiesPlayer.module().getCoins().runTransaction(new Transaction(modifiers, -cost)).change();
        }

        private void updateCostDisplay() {
            Optional<Integer> costOptional = computeCost();
            if (costOptional.isPresent()) {
                TagResolver costPlaceholder = Placeholder.component("cost", Component.text(costOptional.get()));
                Component text = MiniMessage.miniMessage().deserialize(data.format, costPlaceholder);
                if (hologram.isEmpty()) {
                    hologram.add(text);
                }
                else {
                    hologram.set(0, text);
                }
            }
        }

        @Override
        public void initialize(@NotNull Shop shop) {
            hologram.setInstance(shop.instance(), shop.center().add(VecUtils.toPoint(data.position)));
            hologram.clear();

            updateCostDisplay();
        }

        @Override
        public void tick(long time) {
            ++updateTicks;
            if (updateTicks >= data.updateInterval) {
                updateCostDisplay();
            }
        }
    }

    @DataObject
    public record Data(@NotNull Vec3D position,
                       @NotNull String format,
                       @NotNull Key equipmentKey,
                       @NotNull Key groupKey,
                       int baseCost,
                       @NotNull Map<Key, Integer> upgradeCosts,
                       @NotNull Key costModifier,
                       int updateInterval,
                       @NotNull @ChildPath("upgrade_path") String upgradePath) {
    }
}
