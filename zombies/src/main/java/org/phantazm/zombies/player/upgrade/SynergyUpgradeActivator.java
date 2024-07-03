package org.phantazm.zombies.player.upgrade;

import com.github.steanky.element.core.annotation.Cache;
import com.github.steanky.element.core.annotation.DataObject;
import com.github.steanky.element.core.annotation.FactoryMethod;
import com.github.steanky.element.core.annotation.Model;
import it.unimi.dsi.fastutil.ints.IntSet;
import net.kyori.adventure.key.Key;
import net.minestom.server.tag.Tag;
import org.jetbrains.annotations.NotNull;
import org.phantazm.commons.InjectionStore;
import org.phantazm.core.TagUtils;
import org.phantazm.core.equipment.Equipment;
import org.phantazm.core.event.equipment.EquipmentAddEvent;
import org.phantazm.core.inventory.InventoryAccess;
import org.phantazm.core.inventory.InventoryObject;
import org.phantazm.core.inventory.InventoryObjectGroup;
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
            zombiesScene.sceneNode().addListener(EquipmentAddEvent.class, this::handleAddEquipment);
        }

        private void handleAddEquipment(EquipmentAddEvent event) {
            UUID uuid = event.getPlayer().getUuid();
            PlayerUpgradeHandler upgradeHandler = zombiesScene.upgradeHandler(uuid);
            if (upgradeHandler == null) {
                return;
            }

            InventoryAccess access = event.accessRegistry().getAccess(InventoryKeys.ALIVE_ACCESS);
            InventoryObjectGroup group = access.groups().get(data.group);
            if (group == null) {
                return;
            }

            IntSet slots = group.getSlots();
            Set<Key> activeSynergies = new HashSet<>();
            for (int i = 0; i < slots.size() - 1; i++) {
                InventoryObject first = access.profile().getInventoryObjectSafe(i);
                if (!(first instanceof Equipment firstEquipment)) {
                    continue;
                }

                for (int j = i + 1; j < slots.size(); j++) {
                    InventoryObject second = access.profile().getInventoryObjectSafe(j);
                    if (!(second instanceof Equipment secondEquipment)) {
                        continue;
                    }

                    Synergy synergy = data.synergies.get(new SynergyKey(firstEquipment.key(), secondEquipment.key()));
                    if (synergy == null) {
                        continue;
                    }

                    if (synergy.requiredTag == null || TagUtils.sceneLocalTags(event.getPlayer(), zombiesScene)
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
        String requiredTag) {
    }

    @DataObject
    public record Data(@NotNull Key group,
        @NotNull Map<SynergyKey, Synergy> synergies) {

    }
}
