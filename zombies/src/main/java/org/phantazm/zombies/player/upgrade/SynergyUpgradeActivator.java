package org.phantazm.zombies.player.upgrade;

import com.github.steanky.element.core.annotation.Cache;
import com.github.steanky.element.core.annotation.DataObject;
import com.github.steanky.element.core.annotation.FactoryMethod;
import com.github.steanky.element.core.annotation.Model;
import com.github.steanky.ethylene.mapper.annotation.Default;
import it.unimi.dsi.fastutil.ints.*;
import net.kyori.adventure.key.Key;
import net.minestom.server.tag.Tag;
import net.minestom.server.tag.TagHandler;
import org.jetbrains.annotations.NotNull;
import org.phantazm.commons.InjectionStore;
import org.phantazm.core.equipment.Equipment;
import org.phantazm.core.event.equipment.EquipmentPostAddEvent;
import org.phantazm.core.inventory.InventoryAccess;
import org.phantazm.core.inventory.InventoryObject;
import org.phantazm.core.inventory.InventoryObjectGroup;
import org.phantazm.zombies.ZombiesTagUtils;
import org.phantazm.zombies.player.ZombiesPlayer;
import org.phantazm.zombies.player.state.InventoryKeys;
import org.phantazm.zombies.scene2.ZombiesScene;

import java.util.*;

@Model("zombies.upgrade.activator.synergy")
@Cache
public class SynergyUpgradeActivator implements UpgradeActivatorComponent {
    private final Data data;

    @FactoryMethod
    public SynergyUpgradeActivator(@NotNull Data data) {
        this.data = Objects.requireNonNull(data);
    }

    @Override
    public @NotNull UpgradeActivator apply(@NotNull InjectionStore injectionStore, @NotNull ZombiesScene zombiesScene) {
        return new Internal(zombiesScene, data, this);
    }

    @Override
    public boolean mayPurchase(@NotNull Key upgrade, @NotNull TagHandler handler, boolean isSynergy) {
        if (isSynergy) {
            for (Map.Entry<SynergyKey, Synergy> entry : data.synergies.entrySet()) {
                Synergy value = entry.getValue();
                if (value.synergy.equals(upgrade)) {
                    return handler.getTag(purchaseTag(value.firstUpgrade)) && handler.getTag(purchaseTag(value.secondUpgrade));
                }
            }

            return true;
        }

        for (Map.Entry<Key, List<Key>> entry : data.upgradeGroups.entrySet()) {
            List<Key> keyList = entry.getValue();
            int idx = keyList.indexOf(upgrade);
            if (idx == -1) continue;

            for (int i = idx - 1; i >= 0; i--) {
                if (!handler.getTag(purchaseTag(keyList.get(i)))) return false;
            }

            return true;
        }

        return true;
    }

    @Override
    public @NotNull Tag<Boolean> purchaseTag(@NotNull Key upgrade) {
        return Tag.Boolean(upgrade.value() + data.purchasedTagSuffix).defaultValue(false);
    }

    private static class Internal implements UpgradeActivator {
        private final ZombiesScene zombiesScene;
        private final Data data;
        private final UpgradeActivatorComponent component;

        private final Set<Key> allSynergies;
        private final Set<Key> allTiers;

        private Internal(ZombiesScene zombiesScene, Data data, UpgradeActivatorComponent component) {
            this.zombiesScene = zombiesScene;
            this.data = data;
            this.component = component;

            Set<Key> allSynergies = new HashSet<>(data.synergies.size());
            for (Synergy synergy : data.synergies.values()) {
                allSynergies.add(synergy.synergy);
            }

            Set<Key> allTiers = new HashSet<>();
            for (Map.Entry<Key, List<Key>> entry : data.upgradeGroups.entrySet()) {
                allTiers.addAll(entry.getValue());
            }

            this.allSynergies = Set.copyOf(allSynergies);
            this.allTiers = Set.copyOf(allTiers);
        }

        @Override
        public void hook() {
            zombiesScene.sceneNode().addListener(EquipmentPostAddEvent.class, this::handleAddEquipment);
        }

        private void refresh0(ZombiesPlayer zombiesPlayer) {
            UUID uuid = zombiesPlayer.getUUID();

            PlayerUpgradeHandler upgradeHandler = zombiesScene.upgradeHandler(uuid);
            if (upgradeHandler == null) return;

            InventoryAccess access = zombiesPlayer.module().getInventoryAccessRegistry().getAccess(InventoryKeys.ALIVE_ACCESS);
            InventoryObjectGroup group = access.groups().get(data.inventoryGroup);
            if (group == null) return;

            IntList slots = new IntArrayList(group.getSlots());
            slots.sort(IntComparators.NATURAL_COMPARATOR);

            Set<Key> activeSynergies = new HashSet<>(3);
            Set<Key> activeTiers = new HashSet<>();

            TagHandler localTags = ZombiesTagUtils.sceneLocalTags(zombiesPlayer);
            for (int i = 0; i < slots.size(); i++) {
                InventoryObject first = access.profile().getInventoryObjectSafe(slots.getInt(i));
                if (!(first instanceof Equipment firstEquipment)) continue;

                List<Key> tiers = data.upgradeGroups.get(firstEquipment.key());
                if (tiers == null) continue;

                for (Key tier : tiers) {
                    Tag<Boolean> purchasedTag = component.purchaseTag(tier);
                    if (localTags.getTag(purchasedTag)) activeTiers.add(tier);
                }

                for (int j = i + 1; j < slots.size(); j++) {
                    InventoryObject second = access.profile().getInventoryObjectSafe(slots.getInt(j));
                    if (!(second instanceof Equipment secondEquipment)) continue;

                    Synergy synergy = data.synergies.get(new SynergyKey(firstEquipment.key(), secondEquipment.key()));
                    if (synergy == null) continue;

                    Tag<Boolean> purchasedTag = component.purchaseTag(synergy.synergy);
                    if (localTags.getTag(purchasedTag.defaultValue(false))) {
                        activeSynergies.add(synergy.synergy);
                    }
                }
            }

            for (Key otherSynergy : allSynergies) {
                if (!activeSynergies.contains(otherSynergy)) upgradeHandler.deactivateUpgrade(otherSynergy);
            }

            for (Key otherTier : allTiers) {
                if (!activeTiers.contains(otherTier)) upgradeHandler.deactivateUpgrade(otherTier);
            }

            for (Key active : activeSynergies) upgradeHandler.activateUpgrade(active);
            for (Key active : activeTiers) upgradeHandler.activateUpgrade(active);
        }

        @Override
        public void refresh(@NotNull ZombiesPlayer zombiesPlayer) {
            refresh0(zombiesPlayer);
        }

        private void handleAddEquipment(EquipmentPostAddEvent event) {
            ZombiesPlayer zombiesPlayer = zombiesScene.getPlayer(event.getPlayer().getUuid());
            if (zombiesPlayer == null || zombiesPlayer.hasQuit()) return;
            refresh0(zombiesPlayer);
        }
    }

    public record SynergyKey(@NotNull Key first,
        @NotNull Key second) {
        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }

            if (!(obj instanceof SynergyKey other)) {
                return false;
            }

            return (first.equals(other.first) && second.equals(other.second)) ||
                (first.equals(other.second) && second.equals(other.first));
        }

        @Override
        public int hashCode() {
            return first.hashCode() + second.hashCode();
        }
    }

    public record Synergy(@NotNull Key synergy,
        @NotNull Key firstUpgrade,
        @NotNull Key secondUpgrade) {
    }

    @DataObject
    @Default("""
        {
          purchasedTagSuffix='_purchased'
        }
        """)
    public record Data(@NotNull Key inventoryGroup,
        @NotNull Map<Key, List<Key>> upgradeGroups,
        @NotNull Map<SynergyKey, Synergy> synergies,
        @NotNull String purchasedTagSuffix) {

    }
}
