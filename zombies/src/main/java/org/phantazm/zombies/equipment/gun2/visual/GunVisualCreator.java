package org.phantazm.zombies.equipment.gun2.visual;

import net.minestom.server.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.phantazm.commons.InjectionStore;
import org.phantazm.zombies.equipment.perk.equipment.visual.PerkVisual;
import org.phantazm.zombies.equipment.perk.equipment.visual.PerkVisualCreator;
import org.phantazm.zombies.player.PlayerComponent;
import org.phantazm.zombies.player.ZombiesPlayer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;

public class GunVisualCreator implements PerkVisualCreator {

    private final Data data;

    private final Collection<PlayerComponent<GunStackMapper>> stackMapperComponents;

    public GunVisualCreator(@NotNull Data data, @NotNull Collection<PlayerComponent<GunStackMapper>> stackMapperComponents) {
        this.data = Objects.requireNonNull(data, "data");
        this.stackMapperComponents = Objects.requireNonNull(stackMapperComponents, "stackMapperComponent");
    }

    @Override
    public @NotNull PerkVisual forPlayer(@NotNull ZombiesPlayer zombiesPlayer, @NotNull InjectionStore injectionStore) {
        Collection<GunStackMapper> stackMappers = new ArrayList<>(stackMapperComponents.size());
        for (PlayerComponent<GunStackMapper> component : stackMapperComponents) {
            stackMappers.add(component.forPlayer(zombiesPlayer, injectionStore));
        }
        return new Visual(data.base(), stackMappers);
    }

    public record Data(@NotNull ItemStack base) {

    }

    private static class Visual implements PerkVisual {

        private final ItemStack base;

        private final Collection<GunStackMapper> stackMappers;

        private boolean dirty = true;

        public Visual(ItemStack base, Collection<GunStackMapper> stackMappers) {
            this.base = base;
            this.stackMappers = stackMappers;
        }

        @Override
        public @NotNull ItemStack computeItemStack() {
            ItemStack stack = base;
            for (GunStackMapper stackMapper : stackMappers) {
                stack = stackMapper.apply(stack);
            }

            dirty = false;
            return stack;
        }

        @Override
        public boolean shouldCompute() {
            return dirty;
        }

        @Override
        public void leftClick(boolean success) {

        }

        @Override
        public void rightClick(boolean success) {

        }

        @Override
        public void tick(long time) {
            dirty = true;
        }
    }

}
