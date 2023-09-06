package org.phantazm.zombies.equipment.perk.equipment;

import com.github.steanky.element.core.annotation.*;
import com.github.steanky.element.core.annotation.document.Description;
import net.kyori.adventure.key.Key;
import net.minestom.server.entity.Entity;
import net.minestom.server.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.phantazm.commons.InjectionStore;
import org.phantazm.commons.Namespaces;
import org.phantazm.zombies.equipment.perk.equipment.interactor.PerkInteractor;
import org.phantazm.zombies.equipment.perk.equipment.interactor.PerkInteractorCreator;
import org.phantazm.zombies.equipment.perk.equipment.visual.PerkVisual;
import org.phantazm.zombies.equipment.perk.equipment.visual.PerkVisualCreator;
import org.phantazm.zombies.player.ZombiesPlayer;

import java.util.Objects;

@Description("""
    Standard perk equipment.
            
    This element consists of:
    * An "interactor" that handles player interactions, like right-clicking, left-clicking, or selecting
    * A "visual" that controls how the perk looks
    """)
@Model("zombies.perk.equipment.basic")
@Cache(false)
public class BasicPerkEquipmentCreator implements PerkEquipmentCreator {
    private final PerkInteractorCreator interactor;
    private final PerkVisualCreator visual;

    @FactoryMethod
    public BasicPerkEquipmentCreator(@NotNull @Child("interactor") PerkInteractorCreator interactor,
        @NotNull @Child("visual") PerkVisualCreator visual) {
        this.interactor = Objects.requireNonNull(interactor);
        this.visual = Objects.requireNonNull(visual);
    }

    @Override
    public @NotNull PerkEquipment forPlayer(@NotNull ZombiesPlayer zombiesPlayer, @NotNull InjectionStore injectionStore) {
        PerkInteractor perkInteractor = interactor.forPlayer(zombiesPlayer, injectionStore);
        PerkVisual perkVisual = visual.forPlayer(zombiesPlayer, injectionStore);
        return new Equipment(perkInteractor, perkVisual);
    }

    private static class Equipment implements PerkEquipment {
        private final PerkInteractor interactor;
        private final PerkVisual visual;

        private Equipment(PerkInteractor interactor, PerkVisual visual) {
            this.interactor = interactor;
            this.visual = visual;
        }

        @Override
        public void setSelected(boolean selected) {
            interactor.setSelected(selected);
        }

        @Override
        public void rightClick() {
            visual.rightClick(interactor.rightClick());
        }

        @Override
        public void leftClick() {
            visual.leftClick(interactor.leftClick());
        }

        @Override
        public @NotNull Key key() {
            return Key.key(Namespaces.PHANTAZM, "null");
        }

        @Override
        public void tick(long time) {
            visual.tick(time);
        }

        @Override
        public @NotNull ItemStack getItemStack() {
            return visual.computeItemStack();
        }

        @Override
        public boolean shouldRedraw() {
            return visual.shouldCompute();
        }

        @Override
        public void attack(@NotNull Entity target) {
            interactor.attack(target);
        }
    }

    @DataObject
    public record Data(
        @NotNull @ChildPath("interactor") @Description(
            "The interactor, which handles player actions") String interactor,
        @NotNull @ChildPath("visual") @Description(
            "The visual, which controls how the perk appears") String visual) {
    }
}
