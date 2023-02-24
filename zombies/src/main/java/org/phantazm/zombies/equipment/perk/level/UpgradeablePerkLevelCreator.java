package org.phantazm.zombies.equipment.perk.level;

import com.github.steanky.element.core.annotation.*;
import com.github.steanky.element.core.annotation.document.Description;
import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.NotNull;
import org.phantazm.zombies.equipment.perk.effect.PerkEffect;
import org.phantazm.zombies.equipment.perk.effect.PerkEffectCreator;
import org.phantazm.zombies.equipment.perk.equipment.PerkEquipmentCreator;
import org.phantazm.zombies.player.ZombiesPlayer;

import java.util.*;

@Description("""
        Upgradeable perk level.
                
        Each level consists of:
        * Allowable upgrade keys
        * A single "perk equipment", which controls the visual and any interactive effects of the perk
        * Any number of "perk effects", which control the persistent, passive effects granted by having the perk
        """)
@Model("zombies.perk.level.upgradeable")
@Cache(false)
public class UpgradeablePerkLevelCreator implements PerkLevelCreator {
    private final Data data;
    private final PerkEquipmentCreator equipment;
    private final Collection<PerkEffectCreator> effects;

    @FactoryMethod
    public UpgradeablePerkLevelCreator(@NotNull Data data, @NotNull @Child("equipment") PerkEquipmentCreator equipment,
            @NotNull @Child("perk_effects") Collection<PerkEffectCreator> effects) {
        this.data = Objects.requireNonNull(data, "data");
        this.equipment = Objects.requireNonNull(equipment, "equipment");
        this.effects = List.copyOf(effects);
    }

    @Override
    public @NotNull PerkLevel forPlayer(@NotNull ZombiesPlayer zombiesPlayer) {
        PerkEffect[] perkEffects = new PerkEffect[effects.size()];
        Iterator<PerkEffectCreator> iterator = effects.iterator();
        for (int i = 0; i < perkEffects.length; i++) {
            perkEffects[i] = iterator.next().forPlayer(zombiesPlayer);
        }

        return new BasicPerkLevel(data.upgrades, equipment.forPlayer(zombiesPlayer), List.of(perkEffects));
    }

    @DataObject
    public record Data(@NotNull @Description("Possible upgrades for this level") Set<Key> upgrades,
                       @NotNull @Description("The equipment controlling this perk's visuals") @ChildPath(
                               "equipment") String equipment,
                       @NotNull @Description("The perk effect(s) which are applied for this level") @ChildPath(
                               "perk_effects") String perkEffects) {
    }
}
