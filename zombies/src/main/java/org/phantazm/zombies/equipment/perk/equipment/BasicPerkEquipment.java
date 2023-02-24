package org.phantazm.zombies.equipment.perk.equipment;

import com.github.steanky.element.core.annotation.*;
import com.github.steanky.element.core.annotation.document.Description;
import net.kyori.adventure.key.Key;
import net.minestom.server.item.ItemStack;
import org.jetbrains.annotations.NotNull;
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
@Model("zombies.perk.equipment")
@Cache(false)
public class BasicPerkEquipment implements PerkEquipmentCreator {
    private final Key equipmentKey;
    private final PerkInteractorCreator interactor;
    private final PerkVisualCreator visual;

    @FactoryMethod
    public BasicPerkEquipment(@NotNull Key equipmentKey, @NotNull @Child("interactor") PerkInteractorCreator interactor,
            @NotNull @Child("visual") PerkVisualCreator visual) {
        this.equipmentKey = Objects.requireNonNull(equipmentKey, "equipmentKey");
        this.interactor = Objects.requireNonNull(interactor, "interactor");
        this.visual = Objects.requireNonNull(visual, "visual");
    }

    @Override
    public @NotNull PerkEquipment forPlayer(@NotNull ZombiesPlayer zombiesPlayer) {
        PerkInteractor perkInteractor = interactor.forPlayer(zombiesPlayer);
        PerkVisual perkVisual = visual.forPlayer(zombiesPlayer);
        return new Equipment(equipmentKey, perkInteractor, perkVisual);
    }

    private static class Equipment implements PerkEquipment {
        private final Key equipmentKey;
        private final PerkInteractor interactor;
        private final PerkVisual visual;

        private Equipment(Key equipmentKey, PerkInteractor interactor, PerkVisual visual) {
            this.equipmentKey = equipmentKey;
            this.interactor = interactor;
            this.visual = visual;
        }

        @Override
        public void setSelected(boolean selected) {
            interactor.setSelected(selected);
        }

        @Override
        public void rightClick() {
            interactor.rightClick();
        }

        @Override
        public void leftClick() {
            interactor.leftClick();
        }

        @Override
        public @NotNull Key key() {
            return equipmentKey;
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
    }

    @DataObject
    public record Data(@NotNull @ChildPath("interactor") @Description(
            "The interactor, which handles player actions") String interactor,
                       @NotNull @ChildPath("visual") @Description(
                               "The visual, which controls how the perk appears") String visual) {
    }
}
