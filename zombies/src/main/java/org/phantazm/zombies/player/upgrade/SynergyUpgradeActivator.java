package org.phantazm.zombies.player.upgrade;

import com.github.steanky.element.core.annotation.Cache;
import com.github.steanky.element.core.annotation.DataObject;
import com.github.steanky.element.core.annotation.FactoryMethod;
import com.github.steanky.element.core.annotation.Model;
import com.github.steanky.ethylene.mapper.annotation.Default;
import it.unimi.dsi.fastutil.ints.*;
import net.kyori.adventure.key.Key;
import net.minestom.server.tag.Tag;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
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
        return new Internal(zombiesScene, data);
    }

    @Override
    public @NotNull Optional<Key> nextUpgrade(@NotNull Key group, @Nullable Key key) {
        List<Key> list = data.upgradeGroups.get(group);
        if (list == null || list.isEmpty()) return Optional.empty();

        if (key == null) return Optional.of(list.get(0));

        int index = list.indexOf(key);
        if (index < 0 || index >= list.size() - 1) return Optional.empty();

        return Optional.of(list.get(index + 1));
    }

    @Override
    public @NotNull Optional<Key> highestUpgrade(@NotNull Key group, @NotNull ZombiesPlayer zombiesPlayer) {
        List<Key> list = data.upgradeGroups.get(group);
        if (list == null || list.isEmpty()) return Optional.empty();

        ZombiesScene scene = zombiesPlayer.getScene();
        PlayerUpgradeHandler handler = scene.upgradeHandler(zombiesPlayer.getUUID());
        if (handler == null) return Optional.empty();

        for (int i = list.size() - 1; i >= 0; i--) {
            Key key = list.get(i);
            if (handler.isValidUpgrade(key)) return Optional.of(key);
        }

        return Optional.empty();
    }

    @Override
    public boolean hasRequirements(@NotNull Key upgrade, @NotNull Set<Key> activeUpgrades) {
        for (Map.Entry<Key, List<Key>> entry : data.upgradeGroups.entrySet()) {
            List<Key> keyList = entry.getValue();
            int idx = keyList.indexOf(upgrade);
            if (idx == -1) continue;

            for (int i = idx - 1; i >= 0; i--) {
                if (!activeUpgrades.contains(keyList.get(i))) return false;
            }

            return true;
        }

        return true;
    }

    private static class Internal implements UpgradeActivator {
        private final ZombiesScene zombiesScene;
        private final Data data;
        private final Set<Key> allSynergies;

        private Internal(ZombiesScene zombiesScene, Data data) {
            this.zombiesScene = zombiesScene;
            this.data = data;

            Set<Key> allSynergies = new HashSet<>(data.synergies.size());
            for (Synergy synergy : data.synergies.values()) {
                allSynergies.add(synergy.synergy);
            }

            this.allSynergies = Set.copyOf(allSynergies);
        }

        @Override
        public void hook() {
            zombiesScene.sceneNode().addListener(EquipmentPostAddEvent.class, this::handleAddEquipment);
        }

        private void refresh0(ZombiesPlayer zombiesPlayer) {
            UUID uuid = zombiesPlayer.getUUID();
            PlayerUpgradeHandler upgradeHandler = zombiesScene.upgradeHandler(uuid);
            if (upgradeHandler == null) {
                return;
            }

            InventoryAccess access = zombiesPlayer.module().getInventoryAccessRegistry().getAccess(InventoryKeys.ALIVE_ACCESS);
            InventoryObjectGroup group = access.groups().get(data.inventoryGroup);
            if (group == null) {
                return;
            }

            IntList slots = new IntArrayList(group.getSlots());
            slots.sort(IntComparators.NATURAL_COMPARATOR);

            Set<Key> activeSynergies = new HashSet<>();
            for (int i = 0; i < slots.size() - 1; i++) {
                InventoryObject first = access.profile().getInventoryObjectSafe(slots.getInt(i));
                if (!(first instanceof Equipment firstEquipment)) {
                    continue;
                }

                for (int j = i + 1; j < slots.size(); j++) {
                    InventoryObject second = access.profile().getInventoryObjectSafe(slots.getInt(j));
                    if (!(second instanceof Equipment secondEquipment)) {
                        continue;
                    }

                    Synergy synergy = data.synergies.get(new SynergyKey(firstEquipment.key(), secondEquipment.key()));
                    if (synergy == null) {
                        continue;
                    }

                    if (synergy.requiredTag == null || ZombiesTagUtils.sceneLocalTags(zombiesPlayer)
                        .getTag(Tag.Boolean(synergy.requiredTag).defaultValue(false))) {
                        activeSynergies.add(synergy.synergy);
                    }
                }
            }

            for (Key otherSynergy : allSynergies) {
                if (!activeSynergies.contains(otherSynergy)) {
                    upgradeHandler.deactivateUpgrade(otherSynergy);
                }
            }

            for (Key active : activeSynergies) {
                upgradeHandler.activateUpgrade(active);
            }
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

    @Default("""
        {
          requiredTag=null
        }
        """)
    public record Synergy(@NotNull Key synergy,
        String requiredTag) {
    }

    @DataObject
    public record Data(@NotNull Key inventoryGroup,
        @NotNull Map<Key, List<Key>> upgradeGroups,
        @NotNull Map<SynergyKey, Synergy> synergies) {

    }
}
