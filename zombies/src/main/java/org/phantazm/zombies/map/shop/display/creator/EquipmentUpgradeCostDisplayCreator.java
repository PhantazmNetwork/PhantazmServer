package org.phantazm.zombies.map.shop.display.creator;

import com.github.steanky.element.core.annotation.*;
import com.github.steanky.vector.Vec3D;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.minestom.server.MinecraftServer;
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

@Model("zombies.map.shop.display.creator.equipment_cost")
@Cache(false)
public class EquipmentUpgradeCostDisplayCreator implements PlayerDisplayCreator {
    private final Data data;
    private final UpgradePath upgradePath;

    @FactoryMethod
    public EquipmentUpgradeCostDisplayCreator(@NotNull Data data,
            @NotNull @Child("upgrade_path") UpgradePath upgradePath) {
        this.data = Objects.requireNonNull(data, "data");
        this.upgradePath = Objects.requireNonNull(upgradePath, "upgradePath");
    }

    @Override
    public @NotNull ShopDisplay forPlayer(@NotNull ZombiesPlayer zombiesPlayer) {
        return new Display(data, zombiesPlayer, upgradePath);
    }

    private static class Display extends HologramDisplayBase {
        private final Data data;
        private final ZombiesPlayer zombiesPlayer;
        private final UpgradePath upgradePath;

        private long lastUpdate;

        private Display(@NotNull Data data, @NotNull ZombiesPlayer zombiesPlayer, @NotNull UpgradePath upgradePath) {
            super(new ViewableHologram(Vec.ZERO, 0, player -> player.getUuid().equals(zombiesPlayer.getUUID())));
            this.data = Objects.requireNonNull(data, "data");
            this.zombiesPlayer = Objects.requireNonNull(zombiesPlayer, "zombiesPlayer");
            this.upgradePath = Objects.requireNonNull(upgradePath, "upgradePath");
            this.lastUpdate = -1;
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
                Component text =
                        MiniMessage.miniMessage().deserialize(String.format(data.formatString, costOptional.get()));
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
            hologram.setInstance(shop.getInstance(), shop.computeAbsolutePosition(VecUtils.toPoint(data.position())));
            hologram.clear();

            updateCostDisplay();
        }

        @Override
        public void tick(long time) {
            if (lastUpdate == -1) {
                lastUpdate = time;
                return;
            }

            long ticksSinceUpdate = (time - lastUpdate) / MinecraftServer.TICK_MS;
            if (ticksSinceUpdate >= data.updateInterval) {
                updateCostDisplay();
            }
        }
    }

    @DataObject
    public record Data(@NotNull Vec3D position,
                       @NotNull String formatString,
                       @NotNull Key equipmentKey,
                       @NotNull Key groupKey,
                       int baseCost,
                       @NotNull Map<Key, Integer> upgradeCosts,
                       @NotNull Key costModifier,
                       int updateInterval,
                       @NotNull @ChildPath("upgrade_path") String upgradePath) {
    }
}
