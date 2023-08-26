package org.phantazm.zombies.equipment.perk.level;

import net.kyori.adventure.key.Key;
import net.minestom.server.entity.Entity;
import net.minestom.server.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;
import org.phantazm.commons.Namespaces;
import org.phantazm.zombies.equipment.perk.effect.PerkEffect;
import org.phantazm.zombies.equipment.perk.equipment.PerkEquipment;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public class BasicPerkLevel implements PerkLevel {
    private final Set<Key> upgrades;
    private final PerkEquipment equipment;
    private final Collection<PerkEffect> effects;

    public BasicPerkLevel(@NotNull Set<Key> upgrades, @NotNull PerkEquipment equipment,
        @NotNull Collection<PerkEffect> effects) {
        this.upgrades = Set.copyOf(upgrades);
        this.equipment = Objects.requireNonNull(equipment);
        this.effects = List.copyOf(effects);
    }

    @Override
    public void start() {
        for (PerkEffect effect : effects) {
            effect.start();
        }

        equipment.start();
    }

    @Override
    public void tick(long time) {
        for (PerkEffect effect : effects) {
            effect.tick(time);
        }

        equipment.tick(time);
    }

    @Override
    public void end() {
        for (PerkEffect effect : effects) {
            effect.end();
        }

        equipment.end();
    }

    @Override
    public void setSelected(boolean selected) {
        equipment.setSelected(selected);
    }

    @Override
    public void rightClick() {
        equipment.rightClick();
    }

    @Override
    public void leftClick() {
        equipment.leftClick();
    }

    @Override
    public void attack(@NotNull Entity target) {
        equipment.attack(target);
    }

    @Override
    public @NotNull Key key() {
        return Key.key(Namespaces.PHANTAZM, "null");
    }

    @Override
    public @Unmodifiable
    @NotNull Set<Key> upgrades() {
        return upgrades;
    }

    @Override
    public @NotNull ItemStack getItemStack() {
        return equipment.getItemStack();
    }

    @Override
    public boolean shouldRedraw() {
        return equipment.shouldRedraw();
    }
}
